package com.gycss.app.data.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

/**
 * Enum to define user roles within the application.
 */
enum class Role {
    SENIOR, VOLUNTEER, ADMIN
}

/**
 * A unified data class to represent both Senior and Volunteer users.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: Role? = null,
    val phone: String? = null,
    val address: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // Senior-specific fields
    val age: Int = 0,
    val bloodGroup: String? = null,
    val emergencyContacts: List<String> = emptyList(),
    val lastCheckIn: Long = System.currentTimeMillis(),

    // Volunteer-specific fields
    val occupation: String? = null,
    val bio: String? = null,
    val skills: String? = null,
    val idProofUrl: String? = null,
    val rating: Float = 5.0f,
    val helpCount: Int = 0,
    
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    
    // Location for both
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Represents a request for help made by a Senior.
 */
data class HelpRequest(
    val requestId: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val volunteerId: String? = null,
    val volunteerName: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "Pending", 
    val timestamp: Long = System.currentTimeMillis()
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

/**
 * Represents an SOS alert triggered by a Senior.
 */
data class EmergencyAlert(
    val alertId: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val status: String = "Pending",
    val location: GeoPoint? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val assignedVolunteerId: String? = null,
    val assignedVolunteerName: String? = null
)

/**
 * Represents an event created for seniors and volunteers.
 */
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val location: String = "",
    val category: String = "",
    val createdBy: String = "",
    val status: String = "UPCOMING" // UPCOMING, COMPLETED, CANCELLED
)

/**
 * Represents a volunteer's application to an event.
 */
data class EventApplication(
    val id: String = "",
    val eventId: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a review given by a Senior to a Volunteer.
 */
data class Review(
    val id: String = "",
    val volunteerId: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MedicalRecord(
    val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val doctorName: String = "",
    val seniorId: String = ""
)

data class MedicationReminder(
    val id: String = "",
    val medName: String = "",
    val time: String = "",
    val instruction: String = "",
    val seniorId: String = "",
    val isActive: Boolean = true
)
