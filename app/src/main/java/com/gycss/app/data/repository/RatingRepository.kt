package com.gycss.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.gycss.app.data.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Submits a review and updates the volunteer's average rating using a transaction.
     */
    suspend fun submitReview(review: Review): Result<Unit> {
        val volunteerRef = firestore.collection("users").document(review.volunteerId)
        val reviewRef = firestore.collection("reviews").document()

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(volunteerRef)
                val currentRating = snapshot.getDouble("rating") ?: 5.0
                val currentCount = snapshot.getLong("helpCount") ?: 0

                val newCount = currentCount + 1
                val newRating = ((currentRating * currentCount) + review.rating) / newCount

                transaction.set(reviewRef, review.copy(id = reviewRef.id))
                transaction.update(volunteerRef, mapOf(
                    "rating" to newRating.toFloat(),
                    "helpCount" to newCount
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getVolunteerReviews(volunteerId: String) = firestore.collection("reviews")
        .whereEqualTo("volunteerId", volunteerId)
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
}
