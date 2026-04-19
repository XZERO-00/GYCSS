package com.gycss.app.ui.senior

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.model.MedicationReminder
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityMedicationRemindersBinding
import com.gycss.app.service.ReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MedicationRemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationRemindersBinding
    private var remindersListener: ListenerRegistration? = null

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        startListeningForReminders()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabAddReminder.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun startListeningForReminders() {
        val userId = auth.currentUser?.uid ?: return
        remindersListener?.remove()
        remindersListener = FirestoreRepository.getMedicationReminders(userId) { reminders ->
            runOnUiThread {
                updateUI(reminders)
            }
        }
    }

    private fun updateUI(reminders: List<MedicationReminder>) {
        binding.llRemindersContainer.removeAllViews()
        if (reminders.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_reminders), Toast.LENGTH_SHORT).show()
            return
        }

        reminders.forEach { reminder ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_reminder, binding.llRemindersContainer, false) as MaterialCardView
            card.findViewById<TextView>(R.id.tv_med_name).text = reminder.medName
            card.findViewById<TextView>(R.id.tv_med_time).text = reminder.time
            card.findViewById<TextView>(R.id.tv_med_instruction).text = reminder.instruction
            binding.llRemindersContainer.addView(card)
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_med_name)
        val etTime = dialogView.findViewById<EditText>(R.id.et_med_time)
        val etInstruction = dialogView.findViewById<EditText>(R.id.et_med_instruction)

        // Set localized hints if needed, though they are likely in layout
        etName.hint = getString(R.string.med_name)
        etTime.hint = getString(R.string.med_time)
        etInstruction.hint = getString(R.string.med_instruction)

        etTime.isFocusable = false
        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val timeString = String.format("%02d:%02d", hour, minute)
                etTime.setText(timeString)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_medication_reminder))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.btn_add)) { _, _ ->
                val name = etName.text.toString().trim()
                val time = etTime.text.toString().trim()
                val instruction = etInstruction.text.toString().trim()

                if (name.isNotEmpty() && time.isNotEmpty()) {
                    saveReminder(name, time, instruction)
                } else {
                    Toast.makeText(this, getString(R.string.name_time_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun saveReminder(name: String, time: String, instruction: String) {
        val userId = auth.currentUser?.uid ?: return
        val newReminder = MedicationReminder(
            medName = name,
            time = time,
            instruction = instruction,
            seniorId = userId,
            isActive = true
        )

        FirestoreRepository.addMedicationReminder(newReminder, onSuccess = {
            Toast.makeText(this, getString(R.string.reminder_added), Toast.LENGTH_SHORT).show()
            scheduleAlarm(this, newReminder)
        }, onFailure = {
            Toast.makeText(this, getString(R.string.error_adding_reminder, it.message), Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        remindersListener?.remove()
    }

    companion object {
        fun scheduleAlarm(context: Context, reminder: MedicationReminder) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("MED_NAME", reminder.medName)
                putExtra("INSTRUCTION", reminder.instruction)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val timeParts = reminder.time.split(":")
            if (timeParts.size != 2) return
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
