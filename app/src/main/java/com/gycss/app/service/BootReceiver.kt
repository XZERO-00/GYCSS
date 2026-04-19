package com.gycss.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.data.local.PreferenceManager
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.ui.senior.MedicationRemindersActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms(context)
            checkAndStartVoiceSOS(context)
        }
    }

    private fun checkAndStartVoiceSOS(context: Context) {
        if (preferenceManager.isVoiceSosEnabled()) {
            val voiceIntent = Intent(context, VoiceSOSService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(voiceIntent)
            } else {
                context.startService(voiceIntent)
            }
        }
    }

    private fun rescheduleAllAlarms(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        FirestoreRepository.getMedicationReminders(userId) { reminders ->
            reminders.forEach { reminder ->
                if (reminder.isActive) {
                    MedicationRemindersActivity.scheduleAlarm(context, reminder)
                }
            }
        }
    }
}
