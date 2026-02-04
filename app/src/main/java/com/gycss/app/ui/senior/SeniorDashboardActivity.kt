package com.gycss.app.ui.senior

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.model.SOSAlert
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivitySeniorDashboardBinding
import com.gycss.app.service.AmbulanceCallReceiver
import com.gycss.app.ui.common.VolunteersListActivity
import com.gycss.app.ui.login.LoginActivity
import com.gycss.app.ui.senior.profile.ProfileActivity
import com.gycss.app.ui.senior.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class SeniorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeniorDashboardBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var toggle: ActionBarDrawerToggle
    private var alertUpdateListener: ListenerRegistration? = null
    
    @Inject
    lateinit var auth: FirebaseAuth

    private val SOS_TIMEOUT_MS = 180000L // 3 minutes
    private val ALARM_REQUEST_CODE = 1001

    private var volumeClickCount = 0
    private var lastClickTime = 0L
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            if (level != -1 && level < 15) {
                Toast.makeText(context, getString(R.string.low_battery_alert), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeniorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setupToolbar()
        setupWelcomeMessage()
        setupSOSButton()
        setupServicesButtons()
        setupShakeDetection()
        setupBottomNavigation()
        setupDrawerNavigation()
        setupBatteryMonitoring()
        createNotificationChannel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false 
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupDrawerNavigation() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_volunteers -> {
                    val intent = Intent(this, VolunteersListActivity::class.java)
                    intent.putExtra("LEADERBOARD_MODE", false)
                    startActivity(intent)
                }
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    
    private fun setupWelcomeMessage() {
        val user = auth.currentUser
        val name = user?.displayName ?: "Senior"
        binding.tvWelcomeName.text = getString(R.string.hello_user, name)
    }

    private fun setupSOSButton() {
        binding.btnSos.setOnClickListener {
            triggerSOS()
        }
    }

    private fun setupBatteryMonitoring() {
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW))
    }
    
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 1000) {
                    volumeClickCount++
                } else {
                    volumeClickCount = 1
                }
                lastClickTime = currentTime

                if (volumeClickCount >= 3) {
                    volumeClickCount = 0
                    triggerSOS()
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun setupShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        sensorManager?.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                lastAcceleration = currentAcceleration
                currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = currentAcceleration - lastAcceleration
                acceleration = acceleration * 0.9f + delta
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun triggerSOS() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(1000)
        }
        
        checkLocationPermissionAndSendSOS()
    }

    private fun checkLocationPermissionAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                sendSOSAlert(location)
            }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                checkLocationPermissionAndSendSOS()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_needed), Toast.LENGTH_SHORT).show()
            }
        }

    private fun sendSOSAlert(location: Location?) {
        val lat = location?.latitude ?: 0.0
        val lon = location?.longitude ?: 0.0
        val user = auth.currentUser
        
        val alert = SOSAlert(
            seniorId = user?.uid ?: "unknown",
            seniorName = user?.displayName ?: "Senior",
            latitude = lat,
            longitude = lon,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )

        FirestoreRepository.sendSOS(alert, onSuccess = { alertId ->
            runOnUiThread {
                Toast.makeText(this, getString(R.string.sos_sent_msg), Toast.LENGTH_LONG).show()
                startListeningForUpdates(alertId)
                scheduleAmbulanceCall()
            }
        }, onFailure = {
            runOnUiThread {
                Toast.makeText(this, getString(R.string.sos_failed_msg), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun scheduleAmbulanceCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AmbulanceCallReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + SOS_TIMEOUT_MS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelAmbulanceCall() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AmbulanceCallReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun startListeningForUpdates(alertId: String) {
        alertUpdateListener?.remove()
        alertUpdateListener = FirestoreRepository.listenForAlertUpdates(alertId) { updatedAlert ->
            if (updatedAlert.status == "ASSIGNED") {
                runOnUiThread {
                    cancelAmbulanceCall()
                    showHelpOnWayNotification(updatedAlert.assignedVolunteerName ?: "A volunteer")
                    showVolunteerOnWayPopUp(updatedAlert.assignedVolunteerName ?: "A volunteer")
                }
                alertUpdateListener?.remove()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SOS Updates"
            val descriptionText = "Notifications for SOS help status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("sos_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showHelpOnWayNotification(volunteerName: String) {
        val builder = NotificationCompat.Builder(this, "sos_channel")
            .setSmallIcon(R.drawable.ic_sos)
            .setContentTitle(getString(R.string.help_on_way_title))
            .setContentText(getString(R.string.help_on_way_message, volunteerName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2001, builder.build())
    }

    private fun showVolunteerOnWayPopUp(volunteerName: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.help_on_way_title))
            .setMessage(getString(R.string.help_on_way_message, volunteerName))
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun setupServicesButtons() {
        binding.btnMedicalRecords.setOnClickListener {
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
        }
        
        binding.btnReminders.setOnClickListener {
            startActivity(Intent(this, MedicationRemindersActivity::class.java))
        }

        binding.btnGrocery.setOnClickListener {
            requestAssistance("Grocery")
        }
        binding.btnMedicine.setOnClickListener {
            requestAssistance("Medicine Pickup")
        }
        binding.btnUtilities.setOnClickListener {
            requestAssistance("Utilities")
        }
        binding.btnVolunteersList.setOnClickListener {
            val intent = Intent(this, VolunteersListActivity::class.java)
            intent.putExtra("LEADERBOARD_MODE", false)
            startActivity(intent)
        }
        
        binding.btnWellness.setOnClickListener {
            Toast.makeText(this, "Status Updated: I am OK!", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestAssistance(type: String) {
        val intent = Intent(this, RequestAssistanceActivity::class.java)
        intent.putExtra("REQUEST_TYPE", type)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}
        alertUpdateListener?.remove()
    }
}
