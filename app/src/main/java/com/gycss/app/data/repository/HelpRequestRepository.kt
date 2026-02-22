package com.gycss.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HelpRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Creates a new help request.
     */
    suspend fun createHelpRequest(request: HelpRequest): Result<Unit> {
        return try {
            val docRef = firestore.collection("helpRequests").document()
            docRef.set(request.copy(requestId = docRef.id, status = "Pending", timestamp = System.currentTimeMillis())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing request description/title only if it's still Pending.
     */
    suspend fun editHelpRequest(requestId: String, title: String, description: String): Result<Unit> {
        val docRef = firestore.collection("helpRequests").document(requestId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.getString("status") == "Pending") {
                    transaction.update(docRef, mapOf("title" to title, "description" to description))
                } else {
                    throw Exception("Cannot edit request that is already accepted or completed.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cancels a request only if it's Pending.
     */
    suspend fun cancelHelpRequest(requestId: String): Result<Unit> {
        val docRef = firestore.collection("helpRequests").document(requestId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.getString("status") == "Pending") {
                    transaction.update(docRef, "status", "Cancelled")
                } else {
                    throw Exception("Cannot cancel request that is already accepted.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes all available (Pending) requests using server-side sorting (Index Required).
     */
    fun getAvailableRequests(): Flow<List<HelpRequest>> = callbackFlow {
        val subscription = firestore.collection("helpRequests")
            .whereEqualTo("status", "Pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HelpRequestRepo", "Error fetching available requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(HelpRequest::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Observes tasks assigned to a specific volunteer based on status (Index Required).
     */
    fun getVolunteerTasks(volunteerId: String, statusList: List<String>): Flow<List<HelpRequest>> = callbackFlow {
        val subscription = firestore.collection("helpRequests")
            .whereEqualTo("volunteerId", volunteerId)
            .whereIn("status", statusList)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HelpRequestRepo", "Error fetching volunteer tasks", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(HelpRequest::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Observes help requests for a specific senior (Index Required).
     */
    fun getSeniorRequests(seniorId: String): Flow<List<HelpRequest>> = callbackFlow {
        val subscription = firestore.collection("helpRequests")
            .whereEqualTo("seniorId", seniorId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HelpRequestRepo", "Error fetching senior requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(HelpRequest::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Enforces strict status transitions: Pending -> Accepted -> In Progress -> Completed
     */
    suspend fun updateRequestStatusSecurely(requestId: String, currentStatus: String, nextStatus: String): Result<Unit> {
        val docRef = firestore.collection("helpRequests").document(requestId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val statusOnServer = snapshot.getString("status")
                if (statusOnServer == currentStatus) {
                    transaction.update(docRef, "status", nextStatus)
                } else {
                    throw Exception("Invalid state transition from $statusOnServer to $nextStatus")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptRequestSecurely(requestId: String, volunteer: User): Result<Unit> {
        val docRef = firestore.collection("helpRequests").document(requestId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.getString("status") == "Pending") {
                    transaction.update(docRef, mapOf(
                        "status" to "Accepted",
                        "volunteerId" to volunteer.uid,
                        "volunteerName" to volunteer.name
                    ))
                } else {
                    throw Exception("Request already accepted by another volunteer.")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
