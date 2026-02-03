package com.gycss.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gycss.app.R
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        val isSos = remoteMessage.data["type"] == "SOS"
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Emergency Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "A senior needs immediate help nearby."

        if (isSos) {
            // Notify the app if it's in the foreground
            val intent = Intent(ACTION_SOS_RECEIVED).apply {
                putExtra("title", title)
                putExtra("body", body)
                putExtra("seniorId", remoteMessage.data["seniorId"])
                putExtra("lat", remoteMessage.data["lat"])
                putExtra("lon", remoteMessage.data["lon"])
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        // Show system tray notification
        sendNotification(title, body, isSos, remoteMessage.data)
    }

    private fun sendNotification(title: String, messageBody: String, isSos: Boolean, data: Map<String, String>) {
        val intent = Intent(this, VolunteerDashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isSos) "sos_channel_id" else "default_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_sos)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(if (isSos) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        if (isSos) {
            // Add Accept Action
            val acceptIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = "ACTION_ACCEPT_SOS"
                putExtra("seniorId", data["seniorId"])
            }
            val acceptPendingIntent = PendingIntent.getBroadcast(this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.addAction(R.drawable.ic_check, "Accept", acceptPendingIntent)

            // Add Decline Action
            val declineIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = "ACTION_DECLINE_SOS"
            }
            val declinePendingIntent = PendingIntent.getBroadcast(this, 2, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.addAction(R.drawable.ic_close, "Decline", declinePendingIntent)
            
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
            notificationBuilder.setFullScreenIntent(pendingIntent, true)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (isSos) "Emergency SOS Alerts" else "General Notifications"
            val importance = if (isSos) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                if (isSos) {
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(if (isSos) 911 else 0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val ACTION_SOS_RECEIVED = "com.gycss.app.SOS_RECEIVED"
    }
}