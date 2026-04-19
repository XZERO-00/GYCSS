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
        
        val notificationId = intent.getIntExtra("notificationId", 911)
        notificationManager.cancel(notificationId)

        when (action) {
            "ACTION_ACCEPT_SOS" -> {
                val seniorId = intent.getStringExtra("seniorId")
                val lat = intent.getStringExtra("lat")?.toDoubleOrNull()
                val lon = intent.getStringExtra("lon")?.toDoubleOrNull()
                
                if (lat != null && lon != null) {
                    Toast.makeText(context, "SOS Accepted! Opening Maps...", Toast.LENGTH_LONG).show()
                    
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
                } else {
                    Toast.makeText(context, "Location data missing for SOS", Toast.LENGTH_SHORT).show()
                }
            }
            "ACTION_DECLINE_SOS" -> {
                Toast.makeText(context, "SOS Alert Dismissed.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
