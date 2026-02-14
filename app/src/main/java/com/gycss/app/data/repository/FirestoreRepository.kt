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

    // --- Profile Management ---
    fun saveSeniorProfile(senior: Senior, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("seniors").document(senior.id).set(senior)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getSeniorProfile(id: String, onResult: (Senior?) -> Unit) {
        db.collection("seniors").document(id).get()
            .addOnSuccessListener { document -> onResult(document.toObject(Senior::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun saveVolunteerProfile(volunteer: Volunteer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("volunteers").document(volunteer.id).set(volunteer)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerProfile(id: String, onResult: (Volunteer?) -> Unit) {
        db.collection("volunteers").document(id).get()
            .addOnSuccessListener { document -> onResult(document.toObject(Volunteer::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllVolunteers(onResult: (List<Volunteer>) -> Unit): ListenerRegistration {
        return db.collection("volunteers")
            .orderBy("helpCount", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { onResult(emptyList()); return@addSnapshotListener }
                onResult(snapshots?.toObjects(Volunteer::class.java) ?: emptyList())
            }
    }

    // --- Profile Image Upload ---
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

    // --- SOS & Help Completion Logic ---
    fun sendSOS(alert: SOSAlert, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("sos_alerts").document()
        val alertWithId = alert.copy(id = docRef.id)
        docRef.set(alertWithId).addOnSuccessListener { onSuccess(docRef.id) }.addOnFailureListener { onFailure(it) }
    }

    fun listenForSOSAlerts(onAlertReceived: (SOSAlert) -> Unit): ListenerRegistration {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        return db.collection("sos_alerts")
            .whereEqualTo("status", "PENDING")
            .whereGreaterThan("timestamp", oneHourAgo)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val alert = dc.document.toObject(SOSAlert::class.java)
                        onAlertReceived(alert)
                    }
                }
            }
    }

    fun listenForAlertUpdates(alertId: String, onUpdate: (SOSAlert) -> Unit): ListenerRegistration {
        return db.collection("sos_alerts").document(alertId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                snapshot.toObject(SOSAlert::class.java)?.let { onUpdate(it) }
            }
    }

    fun acceptSOS(alertId: String, volunteerId: String, volunteerName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("sos_alerts").document(alertId)
            .update("status", "ASSIGNED", "assignedVolunteerId", volunteerId, "assignedVolunteerName", volunteerName)
            .addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun completeSOS(alertId: String, volunteerId: String, onSuccess: () -> Unit) {
        val batch = db.batch()
        batch.update(db.collection("sos_alerts").document(alertId), "status", "COMPLETED")
        batch.update(db.collection("volunteers").document(volunteerId), "helpCount", FieldValue.increment(1))
        batch.commit().addOnSuccessListener { onSuccess() }
    }

    // --- Event & Application Management ---
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

    // --- Assistance Management ---
    fun requestAssistance(request: AssistanceRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("assistance_requests").document()
        docRef.set(request.copy(id = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    // --- Reviews ---
    fun submitReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("reviews").document()
        docRef.set(review.copy(id = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerReviews(volunteerId: String, onResult: (List<Review>) -> Unit): ListenerRegistration {
        return db.collection("reviews").whereEqualTo("volunteerId", volunteerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ -> onResult(snapshots?.toObjects(Review::class.java) ?: emptyList()) }
    }

    // --- Medication Reminders ---
    fun getMedicationReminders(seniorId: String, onResult: (List<MedicationReminder>) -> Unit): ListenerRegistration {
        return db.collection("medication_reminders").whereEqualTo("seniorId", seniorId)
            .addSnapshotListener { snapshots, _ -> onResult(snapshots?.toObjects(MedicationReminder::class.java) ?: emptyList()) }
    }

    fun addMedicationReminder(reminder: MedicationReminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("medication_reminders").document()
        docRef.set(reminder.copy(id = docRef.id)).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }
}
