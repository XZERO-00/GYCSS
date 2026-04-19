package com.gycss.app.data.repository

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.gycss.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Updates the user profile in Firestore.
     */
    suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a user profile by UID.
     */
    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the FCM token for targeted notifications.
     */
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update("fcmToken", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all volunteers.
     */
    suspend fun getAllVolunteers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "VOLUNTEER")
                .get().await()
            val volunteers = snapshot.toObjects(User::class.java)
            Result.success(volunteers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches the total number of users registered in the system.
     */
    suspend fun getTotalUserCount(): Result<Long> {
        return try {
            val query = firestore.collection("users").count()
            val snapshot = query.get(AggregateSource.SERVER).await()
            Result.success(snapshot.count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Securely deletes user account data across all collections to avoid orphans.
     */
    suspend fun deleteUserAccount(userId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // 1. Delete main user doc
            batch.delete(firestore.collection("users").document(userId))
            
            // 2. Note: Subcollections like medicalRecords need manual deletion of each doc
            val medicalRecords = firestore.collection("users").document(userId).collection("medicalRecords").get().await()
            medicalRecords.forEach { batch.delete(it.reference) }

            // 3. Mark help requests as Orphaned/Cancelled instead of deleting to keep history consistency
            val requests = firestore.collection("helpRequests").whereEqualTo("seniorId", userId).get().await()
            requests.forEach { batch.update(it.reference, "status", "UserDeleted") }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
