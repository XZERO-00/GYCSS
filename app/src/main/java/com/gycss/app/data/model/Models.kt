package com.gycss.app.data.model

enum class UserType {
    SENIOR, VOLUNTEER, ADMIN
}

data class Senior(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val bloodGroup: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val profileImageUrl: String = "",
    val emergencyContacts: List<String> = emptyList(),
    val medicalHistory: List<MedicalRecord> = emptyList(),
    val lastCheckIn: Long = System.currentTimeMillis()
)

data class Volunteer(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val phone: String = "",
    val email: String = "",
    val occupation: String = "",
    val bio: String = "",
    val skills: String = "",
    val idProofUrl: String = "",
    val profileImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Float = 5.0f,
    val helpCount: Int = 0,
    val isAvailable: Boolean = true
)

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val location: String = "",
    val category: String = "", // e.g., Medical Camp, Social Gathering
    val createdBy: String = "",
    val status: String = "UPCOMING" // UPCOMING, COMPLETED, CANCELLED
)

data class EventApplication(
    val id: String = "",
    val eventId: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)

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

data class SOSAlert(
    val id: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "PENDING", // PENDING, ASSIGNED, COMPLETED
    val assignedVolunteerId: String? = null,
    val assignedVolunteerName: String? = null
)

data class AssistanceRequest(
    val id: String = "",
    val type: String = "",
    val seniorId: String = "",
    val seniorName: String = "",
    val description: String = "",
    val status: String = "PENDING",
    val assignedVolunteerId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
