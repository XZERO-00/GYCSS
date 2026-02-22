package com.gycss.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gycss.app.data.model.MedicalRecord
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalRecordRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getMedicalRecords(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("medicalRecords")
        .orderBy("date", Query.Direction.DESCENDING)

    suspend fun addMedicalRecord(userId: String, record: MedicalRecord): Result<Unit> {
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document()
            docRef.set(record.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMedicalRecord(userId: String, recordId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("medicalRecords")
                .document(recordId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
