package com.gycss.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gycss.app.data.model.EmergencyAlert
import com.gycss.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Sends an SOS alert to Firestore.
     */
    suspend fun sendEmergencyAlert(alert: EmergencyAlert): Result<String> {
        return try {
            val docRef = firestore.collection("emergencyAlerts").document()
            val finalAlert = alert.copy(alertId = docRef.id)
            docRef.set(finalAlert).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancels an SOS alert if it hasn't been accepted yet.
     */
    suspend fun cancelSOS(alertId: String): Result<Unit> {
        val docRef = firestore.collection("emergencyAlerts").document(alertId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.getString("status") == "Pending") {
                    transaction.update(docRef, "status", "Cancelled")
                } else {
                    throw Exception("SOS already accepted by a volunteer.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes changes to a specific emergency alert.
     */
    fun observeAlert(alertId: String): Flow<EmergencyAlert?> = callbackFlow {
        val subscription = firestore.collection("emergencyAlerts").document(alertId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val alert = snapshot?.toObject(EmergencyAlert::class.java)
                trySend(alert)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Observes all pending SOS alerts for volunteers.
     * Note: This requires a composite index on 'status' (Ascending) and 'timestamp' (Descending).
     */
    fun observePendingAlerts(): Flow<List<EmergencyAlert>> = callbackFlow {
        // We use a simpler query first to avoid immediate failure if index is missing, 
        // though the ordering is preferred for the UI.
        val query = firestore.collection("emergencyAlerts")
            .whereEqualTo("status", "Pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("EmergencyRepo", "Error observing pending alerts: ${error.message}")
                if (error.message?.contains("index") == true) {
                    Log.e("EmergencyRepo", "MISSING INDEX: Please create the composite index in Firebase Console.")
                }
                // Try a fallback query without ordering if the primary fails due to index
                return@addSnapshotListener
            }
            val alerts = snapshot?.toObjects(EmergencyAlert::class.java) ?: emptyList()
            trySend(alerts)
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Accepts an SOS alert by a volunteer securely.
     */
    suspend fun acceptAlert(alertId: String, volunteer: User): Result<Unit> {
        val docRef = firestore.collection("emergencyAlerts").document(alertId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.getString("status") == "Pending") {
                    transaction.update(docRef, mapOf(
                        "status" to "Assigned",
                        "assignedVolunteerId" to volunteer.uid,
                        "assignedVolunteerName" to volunteer.name
                    ))
                } else {
                    throw Exception("SOS already accepted by another volunteer.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes an emergency alert and rewards the volunteer.
     */
    suspend fun completeAlert(alertId: String): Result<Unit> {
        val docRef = firestore.collection("emergencyAlerts").document(alertId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val volunteerId = snapshot.getString("assignedVolunteerId")
                
                transaction.update(docRef, "status", "Completed")
                
                if (volunteerId != null) {
                    val volunteerRef = firestore.collection("users").document(volunteerId)
                    transaction.update(volunteerRef, "helpCount", FieldValue.increment(1))
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
