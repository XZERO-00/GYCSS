package com.gycss.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.ui.senior.MedicationRemindersActivity
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms(context)
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
