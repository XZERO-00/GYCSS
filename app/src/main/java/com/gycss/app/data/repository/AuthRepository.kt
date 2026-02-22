package com.gycss.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gycss.app.data.model.Role
import com.gycss.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Registers a new user. If Auth account already exists but Firestore doc is missing,
     * it will attempt to recreate the Firestore document.
     */
    suspend fun registerUser(user: User, password: String): Result<User> {
        return try {
            val uid = try {
                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                authResult.user?.uid
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                // Email already exists in Auth, let's check if we can just update the Firestore doc
                auth.signInWithEmailAndPassword(user.email, password).await().user?.uid
            } ?: throw Exception("Authentication failed")

            val finalUser = user.copy(uid = uid)
            firestore.collection("users").document(uid).set(finalUser).await()
            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs in a user. If profile is missing, returns a specific error code.
     */
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Authentication failed")
            
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) 
                ?: throw Exception("PROFILE_MISSING") // Custom error code
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
