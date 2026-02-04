package com.gycss.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.gycss.app.data.model.AssistanceRequest
import com.gycss.app.data.model.MedicationReminder
import com.gycss.app.data.model.SOSAlert
import com.gycss.app.data.model.Senior
import com.gycss.app.data.model.Volunteer
import com.gycss.app.data.model.Review

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // --- Profile Management ---

    fun saveSeniorProfile(senior: Senior, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("seniors").document(senior.id).set(senior)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getSeniorProfile(id: String, onResult: (Senior?) -> Unit) {
        db.collection("seniors").document(id).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(Senior::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    fun saveVolunteerProfile(volunteer: Volunteer, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("volunteers").document(volunteer.id).set(volunteer)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerProfile(id: String, onResult: (Volunteer?) -> Unit) {
        db.collection("volunteers").document(id).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(Volunteer::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllVolunteers(onResult: (List<Volunteer>) -> Unit): ListenerRegistration {
        return db.collection("volunteers")
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val volunteers = snapshots?.toObjects(Volunteer::class.java) ?: emptyList()
                onResult(volunteers)
            }
    }

    // --- SOS Management ---

    fun sendSOS(alert: SOSAlert, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("sos_alerts").document()
        val alertWithId = alert.copy(id = docRef.id)
        
        docRef.set(alertWithId)
            .addOnSuccessListener { onSuccess(docRef.id) }
            .addOnFailureListener { onFailure(it) }
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
                val alert = snapshot.toObject(SOSAlert::class.java)
                if (alert != null) {
                    onUpdate(alert)
                }
            }
    }

    fun acceptSOS(alertId: String, volunteerId: String, volunteerName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("sos_alerts").document(alertId)
            .update(
                "status", "ASSIGNED",
                "assignedVolunteerId", volunteerId,
                "assignedVolunteerName", volunteerName
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // --- Assistance Management ---

    fun requestAssistance(request: AssistanceRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("assistance_requests").document()
        val requestWithId = request.copy(id = docRef.id)
        docRef.set(requestWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenForAssistanceRequests(onResult: (List<AssistanceRequest>) -> Unit): ListenerRegistration {
        return db.collection("assistance_requests")
            .whereEqualTo("status", "PENDING")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val requests = snapshots?.toObjects(AssistanceRequest::class.java) ?: emptyList()
                onResult(requests)
            }
    }

    // --- Medication Reminders ---

    fun addMedicationReminder(reminder: MedicationReminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("medication_reminders").document()
        val reminderWithId = reminder.copy(id = docRef.id)
        docRef.set(reminderWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getMedicationReminders(seniorId: String, onResult: (List<MedicationReminder>) -> Unit): ListenerRegistration {
        return db.collection("medication_reminders")
            .whereEqualTo("seniorId", seniorId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val reminders = snapshots?.toObjects(MedicationReminder::class.java) ?: emptyList()
                onResult(reminders)
            }
    }

    // --- Reviews ---

    fun submitReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = db.collection("reviews").document()
        val reviewWithId = review.copy(id = docRef.id)
        docRef.set(reviewWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getVolunteerReviews(volunteerId: String, onResult: (List<Review>) -> Unit): ListenerRegistration {
        return db.collection("reviews")
            .whereEqualTo("volunteerId", volunteerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val reviews = snapshots?.toObjects(Review::class.java) ?: emptyList()
                onResult(reviews)
            }
    }
}
