package com.gycss.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gycss.app.data.model.Chat
import com.gycss.app.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Creates a new chat session or returns an existing one.
     */
    suspend fun getOrCreateChat(seniorId: String, volunteerId: String): String {
        val chatId = if (seniorId < volunteerId) "${seniorId}_${volunteerId}" else "${volunteerId}_${seniorId}"
        
        val chatDoc = firestore.collection("chats").document(chatId).get().await()
        if (!chatDoc.exists()) {
            val chat = Chat(
                chatId = chatId,
                participants = listOf(seniorId, volunteerId)
            )
            firestore.collection("chats").document(chatId).set(chat).await()
        }
        return chatId
    }

    /**
     * Sends a message in a specific chat.
     */
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val docRef = firestore.collection("chats").document(chatId)
                .collection("messages").document()
            val finalMessage = message.copy(messageId = docRef.id)
            
            firestore.runBatch { batch ->
                batch.set(docRef, finalMessage)
                batch.update(firestore.collection("chats").document(chatId), 
                    "lastMessage", message.messageText,
                    "lastMessageTimestamp", message.timestamp)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes real-time messages for a specific chat.
     */
    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }
}
