package com.gycss.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
     */
    fun observePendingAlerts(): Flow<List<EmergencyAlert>> = callbackFlow {
        val subscription = firestore.collection("emergencyAlerts")
            .whereEqualTo("status", "Pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjects(EmergencyAlert::class.java) ?: emptyList()
                trySend(alerts)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Accepts an SOS alert by a volunteer.
     */
    suspend fun acceptAlert(alertId: String, volunteer: User): Result<Unit> {
        return try {
            firestore.collection("emergencyAlerts").document(alertId)
                .update(
                    mapOf(
                        "status" to "Assigned",
                        "assignedVolunteerId" to volunteer.uid,
                        "assignedVolunteerName" to volunteer.name
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes an emergency alert.
     */
    suspend fun completeAlert(alertId: String): Result<Unit> {
        return try {
            firestore.collection("emergencyAlerts").document(alertId)
                .update("status", "Completed").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
