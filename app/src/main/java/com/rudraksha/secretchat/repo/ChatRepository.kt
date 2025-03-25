package com.rudraksha.secretchat.repo

//import android.text.format.DateFormat
//import androidx.lifecycle.get
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.ktx.toObject
//import kotlinx.coroutines.tasks.await
//import java.util.UUID
//import java.util.Date
//import com.google.firebase.Timestamp
//import com.rudraksha.secretchat.data.model.toChatItem
//import kotlin.io.path.exists

// Data Classes
data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
/*

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")
    private val messagesCollection = db.collection("messages")

    // Function to get or create a chat ID
    suspend fun getOrCreateChatId(user1Id: String, user2Id: String): String {
        // Sort user IDs to ensure consistent chat ID regardless of who initiates
        val sortedUserIds = listOf(user1Id, user2Id).sorted()
        val potentialChatId = "${sortedUserIds[0]}_${sortedUserIds[1]}"

        // Check if a chat with this ID already exists
        val chatDocument = chatsCollection.document(potentialChatId).get().await()

        return if (chatDocument.exists()) {
            // Chat exists, return the existing chat ID
            potentialChatId
        } else {
            // Chat doesn't exist, create a new one
            val newChat = Chat(chatId = potentialChatId, participants = sortedUserIds)
            chatsCollection.document(potentialChatId).set(newChat).await()
            potentialChatId
        }
    }

    // Function to send a message
    suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        val messageId = UUID.randomUUID().toString()
        val message = Message(messageId, senderId, content)

        // Add the message to the messages subcollection
        messagesCollection.document(chatId).collection("messages").document(messageId).set(message).await()

        // Update the last message and timestamp in the chat document
        chatsCollection.document(chatId).update(
            mapOf(
                "lastMessage" to content,
                "lastMessageTimestamp" to System.currentTimeMillis()
            )
        ).await()
    }

    // Function to get messages for a chat
    suspend fun getMessages(chatId: String): List<Message> {
        val querySnapshot = messagesCollection.document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<Message>() }
    }

    // Function to get all chats for a user
    suspend fun getChatsForUser(userId: String): List<Chat> {
        val querySnapshot = chatsCollection
            .whereArrayContains("participants", userId)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<Chat>() }
    }
}*/
