package com.gycss.app.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.gycss.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(user: User, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("AUTH_FAILED")

            val finalUser = user.copy(uid = uid)
            try {
                firestore.collection("users").document(uid).set(finalUser).await()
                Result.success(finalUser)
            } catch (e: Exception) {
                // Rollback Auth if DB fails to maintain consistency
                auth.currentUser?.delete()?.await()
                Result.failure(Exception("DATABASE_ERROR"))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("EMAIL_EXISTS"))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("WEAK_PASSWORD"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("INVALID_EMAIL"))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("NETWORK_ERROR"))
        } catch (e: FirebaseTooManyRequestsException) {
            Result.failure(Exception("TOO_MANY_REQUESTS"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("AUTH_FAILED")
            
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                auth.signOut()
                throw Exception("PROFILE_MISSING")
            }
            
            val user = userDoc.toObject(User::class.java) ?: throw Exception("PROFILE_CORRUPTED")
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("WRONG_CREDENTIALS"))
        } catch (e: FirebaseAuthInvalidUserException) {
            // This can mean user not found or user disabled
            Result.failure(Exception("USER_NOT_FOUND"))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("NETWORK_ERROR"))
        } catch (e: FirebaseTooManyRequestsException) {
            Result.failure(Exception("TOO_MANY_REQUESTS"))
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
