package com.gycss.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel the notification after an action is taken
        notificationManager.cancel(911)

        when (action) {
            "ACTION_ACCEPT_SOS" -> {
                val seniorId = intent.getStringExtra("seniorId")
                Toast.makeText(context, "SOS Accepted! Opening Maps...", Toast.LENGTH_LONG).show()
                
                // Demo: Open Google Maps directions to Rajesh Kumar's location
                val lat = 28.6139
                val lon = 77.2090
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon")
                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                }
            }
            "ACTION_DECLINE_SOS" -> {
                Toast.makeText(context, "SOS Alert Dismissed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}