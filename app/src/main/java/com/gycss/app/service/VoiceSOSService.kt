package com.gycss.app.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import com.gycss.app.R
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.model.EmergencyAlert
import com.gycss.app.data.model.Role
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.EmergencyRepository
import com.gycss.app.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class VoiceSOSService : Service(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceSOSService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "voice_sos_channel"
        
        // Command Keywords
        private val KEYWORDS_ENGLISH = listOf("ambulance", "emergency", "immediately", "help me")
        private val KEYWORDS_HINDI = listOf("एम्बुलेंस", "बचाओ", "मदದ", "ಆಪಾತಕಾಲೀನ")
        private val KEYWORDS_MARATHI = listOf("ॲम्ब्युलन्स", "मदत", "वाचवा")
        private val KEYWORDS_KANNADA = listOf("ಅಂಬುಲೆನ್ಸ್", "ಸಹಾಯ", "ತುರ್ತು")
    }

    @Inject
    lateinit var emergencyRepository: EmergencyRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var isListening = false
    private var isTriggering = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available")
            stopSelf()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { Log.d(TAG, "onReadyForSpeech") }
            override fun onBeginningOfSpeech() { Log.d(TAG, "onBeginningOfSpeech") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d(TAG, "onEndOfSpeech") }

            override fun onError(error: Int) {
                Log.e(TAG, "Speech Error: $error")
                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    speechRecognizer?.cancel()
                }
                // Restart listening after a short delay unless triggering
                if (!isTriggering) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startListening()
                    }, 1000)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let { processSpeechResults(it) }
                if (!isTriggering) startListening()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let { processSpeechResults(it) }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        startListening()
    }

    private fun startListening() {
        if (isTriggering) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferenceManager.getLanguage())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening: ${e.message}")
        }
    }

    private fun processSpeechResults(matches: ArrayList<String>) {
        val allKeywords = KEYWORDS_ENGLISH + KEYWORDS_HINDI + KEYWORDS_MARATHI + KEYWORDS_KANNADA
        
        for (match in matches) {
            val lowerMatch = match.lowercase(Locale.ROOT)
            Log.d(TAG, "Detected: $lowerMatch")
            
            if (allKeywords.any { lowerMatch.contains(it.lowercase()) }) {
                handleSOSIntent()
                break
            }
        }
    }

    private fun handleSOSIntent() {
        if (isTriggering) return
        isTriggering = true
        speechRecognizer?.stopListening()

        speakFeedback("Emergency detected. Triggering SOS in 3 seconds. Say cancel to stop.")
        
        serviceScope.launch {
            delay(4000) // Wait for TTS and user cancellation window
            if (isTriggering) {
                triggerActualSOS()
            }
        }
    }

    private fun triggerActualSOS() {
        serviceScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            if (user.role != Role.SENIOR) return@launch

            if (ActivityCompat.checkSelfPermission(this@VoiceSOSService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Location permission missing")
                return@launch
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@VoiceSOSService)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        sendSOSAlert(user.uid, user.name, it)
                    } ?: run {
                        // Fallback to last known if current is null
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            sendSOSAlert(user.uid, user.name, lastLoc ?: Location("").apply { latitude = 0.0; longitude = 0.0 })
                        }
                    }
                }
        }
    }

    private fun sendSOSAlert(uid: String, name: String, location: Location) {
        val alert = EmergencyAlert(
            seniorId = uid,
            seniorName = name,
            status = "Pending",
            location = GeoPoint(location.latitude, location.longitude),
            timestamp = System.currentTimeMillis()
        )
        
        serviceScope.launch {
            emergencyRepository.sendEmergencyAlert(alert).onSuccess {
                speakFeedback("SOS sent successfully. Volunteers are being notified.")
                isTriggering = false
                startListening()
            }.onFailure {
                speakFeedback("Failed to send SOS. Please use the button.")
                isTriggering = false
                startListening()
            }
        }
    }

    private fun speakFeedback(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SOS_FEEDBACK")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = when (preferenceManager.getLanguage()) {
                "hi" -> Locale("hi", "IN")
                "mr" -> Locale("mr", "IN")
                "kn" -> Locale("kn", "IN")
                else -> Locale.ENGLISH
            }
            tts?.language = lang
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, com.gycss.app.ui.senior.SeniorDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice SOS Active")
            .setContentText("Listening for emergency commands...")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Voice SOS Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isTriggering = false
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }
}
