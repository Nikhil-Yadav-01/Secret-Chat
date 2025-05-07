package com.rudraksha.secretchat.data.model

import kotlinx.serialization.Serializable

// Enums
enum class ChatType { PRIVATE, GROUP, SECRET }

@Serializable
enum class MessageType {
    TEXT,           // Regular text message
    IMAGE,          // Image message
    VIDEO,          // Video message
    FILE,           // File attachment
    AUDIO,          // Voice message
    LOCATION,       // Location sharing
    CONTACT,        // Contact sharing

    // Security and encryption related types
    KEY_EXCHANGE,   // Used for public key exchange
    SECURE_CHANNEL_INIT, // Used for establishing secure channels

    // Special message types
    SELF_DESTRUCT,  // Self-destructing messages
    NOTIFICATION,   // System notifications
    REACTION,       // Message reactions

    // Status messages
    TYPING,         // Typing indicator
    DELIVERY_RECEIPT, // Message delivery confirmation
    READ_RECEIPT,   // Message read confirmation
}