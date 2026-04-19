package com.gycss.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.gycss.app.data.model.*

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ... (other methods remain the same)

    fun getAllVolunteers(onResult: (List<User>) -> Unit): ListenerRegistration {
        return db.collection("users")
            .whereEqualTo("role", "VOLUNTEER")
            .orderBy("helpCount", Query.Direction.DESCENDING) // Server-side sorting restored
            .addSnapshotListener { snapshots, e ->
                if (e != null) { 
                    onResult(emptyList())
                    return@addSnapshotListener 
                }
                val volunteers = snapshots?.toObjects(User::class.java) ?: emptyList()
                onResult(volunteers)
            }
    }

    // ... (rest of the file remains the same)
    
    fun saveSeniorProfile(senior: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(senior.uid).set(senior)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getSeniorProfile(id: String, onResult: (User?) -> Unit) {
        db.collection("users").document(id).get()
            .addOnSuccessListener { document -> onResult(document.toObject(User::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun saveVolunteerProfile(volunteer: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(volunteer.uid).set(volunteer)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerProfile(id: String, onResult: (User?) -> Unit) {
        db.collection("users").document(id).get()
            .addOnSuccessListener { document -> onResult(document.toObject(User::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun uploadProfileImage(userId: String, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val ref = storage.reference.child("profile_images/$userId.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun sendSOS(alert: EmergencyAlert, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("emergencyAlerts").document()
        val alertWithId = alert.copy(alertId = docRef.id)
        docRef.set(alertWithId).addOnSuccessListener { onSuccess(docRef.id) }.addOnFailureListener { onFailure(it) }
    }

    fun listenForSOSAlerts(onAlertReceived: (EmergencyAlert) -> Unit): ListenerRegistration {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        return db.collection("emergencyAlerts")
            .whereEqualTo("status", "Pending")
            .whereGreaterThan("timestamp", oneHourAgo)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val alert = dc.document.toObject(EmergencyAlert::class.java)
                        onAlertReceived(alert)
                    }
                }
            }
    }

    fun listenForAlertUpdates(alertId: String, onUpdate: (EmergencyAlert) -> Unit): ListenerRegistration {
        return db.collection("emergencyAlerts").document(alertId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                snapshot.toObject(EmergencyAlert::class.java)?.let { onUpdate(it) }
            }
    }

    fun acceptSOS(alertId: String, volunteerId: String, volunteerName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("emergencyAlerts").document(alertId)
            .update(mapOf(
                "status" to "Assigned",
                "assignedVolunteerId" to volunteerId,
                "assignedVolunteerName" to volunteerName
            ))
            .addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun completeSOS(alertId: String, volunteerId: String, onSuccess: () -> Unit) {
        val batch = db.batch()
        batch.update(db.collection("emergencyAlerts").document(alertId), "status", "Completed")
        batch.update(db.collection("users").document(volunteerId), "helpCount", FieldValue.increment(1))
        batch.commit().addOnSuccessListener { onSuccess() }
    }

    fun getEvents(onResult: (List<Event>) -> Unit): ListenerRegistration {
        return db.collection("events")
            .whereEqualTo("status", "UPCOMING")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                onResult(snapshots?.toObjects(Event::class.java) ?: emptyList())
            }
    }

    fun applyToEvent(application: EventApplication, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("event_applications").document()
        db.collection("event_applications")
            .whereEqualTo("eventId", application.eventId)
            .whereEqualTo("volunteerId", application.volunteerId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    docRef.set(application.copy(id = docRef.id))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    onFailure(Exception("Already applied"))
                }
            }
    }

    fun getVolunteerApplications(volunteerId: String, onResult: (List<EventApplication>) -> Unit): ListenerRegistration {
        return db.collection("event_applications")
            .whereEqualTo("volunteerId", volunteerId)
            .addSnapshotListener { snapshots, _ ->
                onResult(snapshots?.toObjects(EventApplication::class.java) ?: emptyList())
            }
    }

    fun cancelApplication(applicationId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("event_applications").document(applicationId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun requestAssistance(request: HelpRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("helpRequests").document()
        docRef.set(request.copy(requestId = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun submitReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("reviews").document()
        docRef.set(review.copy(id = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerReviews(volunteerId: String, onResult: (List<Review>) -> Unit): ListenerRegistration {
        return db.collection("reviews").whereEqualTo("volunteerId", volunteerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ -> onResult(snapshots?.toObjects(Review::class.java) ?: emptyList()) }
    }

    fun getMedicationReminders(seniorId: String, onResult: (List<MedicationReminder>) -> Unit): ListenerRegistration {
        return db.collection("medication_reminders").whereEqualTo("seniorId", seniorId)
            .addSnapshotListener { snapshots, _ -> onResult(snapshots?.toObjects(MedicationReminder::class.java) ?: emptyList()) }
    }

    fun addMedicationReminder(reminder: MedicationReminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("medication_reminders").document()
        docRef.set(reminder.copy(id = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun getHealthVitals(seniorId: String, onResult: (List<HealthVital>) -> Unit): ListenerRegistration {
        return db.collection("health_vitals")
            .whereEqualTo("seniorId", seniorId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                onResult(snapshots?.toObjects(HealthVital::class.java) ?: emptyList())
            }
    }

    fun addHealthVital(vital: HealthVital, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("health_vitals").document()
        docRef.set(vital.copy(id = docRef.id))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
