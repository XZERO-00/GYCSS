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

        val type = remoteMessage.data["type"]
        val isSos = type == "SOS"
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: if (isSos) "Emergency Alert" else "GYCSS Notification"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: if (isSos) "A senior needs immediate help nearby." else ""

        if (isSos) {
            val intent = Intent(ACTION_SOS_RECEIVED).apply {
                putExtra("title", title)
                putExtra("body", body)
                putExtra("seniorId", remoteMessage.data["seniorId"])
                putExtra("lat", remoteMessage.data["lat"])
                putExtra("lon", remoteMessage.data["lon"])
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        sendNotification(title, body, type, remoteMessage.data)
    }

    private fun sendNotification(title: String, messageBody: String, type: String?, data: Map<String, String>) {
        val intent = Intent(this, VolunteerDashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (type) {
            "SOS" -> CHANNEL_SOS
            "CHAT" -> CHANNEL_CHAT
            else -> CHANNEL_GENERAL
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_sos)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(type != "SOS")
            .setOngoing(type == "SOS")
            .setPriority(if (type == "SOS") NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        if (type == "SOS") {
            val notificationId = 911
            
            val acceptIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = "ACTION_ACCEPT_SOS"
                putExtra("seniorId", data["seniorId"])
                putExtra("lat", data["lat"])
                putExtra("lon", data["lon"])
                putExtra("notificationId", notificationId)
            }
            val acceptPendingIntent = PendingIntent.getBroadcast(this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.addAction(R.drawable.ic_check, "Accept", acceptPendingIntent)

            val declineIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = "ACTION_DECLINE_SOS"
                putExtra("notificationId", notificationId)
            }
            val declinePendingIntent = PendingIntent.getBroadcast(this, 2, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.addAction(R.drawable.ic_close, "Decline", declinePendingIntent)
            
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
            notificationBuilder.setFullScreenIntent(pendingIntent, true)
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannels(notificationManager)
            notificationManager.notify(notificationId, notificationBuilder.build())
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannels(notificationManager)
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sosChannel = NotificationChannel(CHANNEL_SOS, "Emergency SOS Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null) 
            }

            val chatChannel = NotificationChannel(CHANNEL_CHAT, "Chat Messages", NotificationManager.IMPORTANCE_DEFAULT)
            val generalChannel = NotificationChannel(CHANNEL_GENERAL, "General Updates", NotificationManager.IMPORTANCE_LOW)

            notificationManager.createNotificationChannels(listOf(sosChannel, chatChannel, generalChannel))
        }
    }

    override fun onNewToken(token: String) {
        // Here we should update the token in Firestore for the current user
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val ACTION_SOS_RECEIVED = "com.gycss.app.SOS_RECEIVED"
        
        const val CHANNEL_SOS = "sos_channel"
        const val CHANNEL_CHAT = "chat_channel"
        const val CHANNEL_GENERAL = "general_channel"
    }
}
