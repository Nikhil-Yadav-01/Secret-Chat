# SecretChat Server Configuration

## Overview
This package contains the networking components for the SecretChat application, including WebSocket communication and REST API services.

## Configuration
Server configuration settings are managed through the `ApiConfig` class, which allows switching between local development and production server environments.

To change the server settings:

1. Open `ApiConfig.kt`
2. Update the `LOCAL_SERVER` value for your local development environment
3. Update the `PRODUCTION_SERVER` value for your deployed server
4. Set `USE_LOCAL_SERVER` to `true` for local development or `false` for production

## Components

### ApiConfig
Contains server URLs and endpoints for both local and production environments.

### AuthApiService
Handles authentication-related API calls (login, register, anonymous registration).

### WebSocketManager
Manages real-time WebSocket communication, including:
- Connection and reconnection logic
- Message sending and receiving
- File transfer functionality

## Usage
The WebSocketManager can be initialized with either username/password authentication or token-based authentication:

```kotlin
// With username/password
val wsManager = WebSocketManager(username = "user123", password = "password123")

// With JWT token
val wsManager = WebSocketManager(token = "your-jwt-token")

// Connect
wsManager.connect()

// Listen for messages
wsManager.setOnDataReceivedListener { data ->
    // Handle received data
}

// Send message
wsManager.sendData(someWebSocketData)

// Disconnect when done
wsManager.disconnect()
``` 