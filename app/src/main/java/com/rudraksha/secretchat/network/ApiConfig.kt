package com.rudraksha.secretchat.network

/**
 * Server configuration class that provides URLs for local and production environments
 */
object ApiConfig {
    // Local server config (use 10.0.2.2 for emulator or your computer's IP for physical device)
    private const val LOCAL_SERVER = "10.0.2.2:8001"
    
    // Deployed/Production server config
    private const val PRODUCTION_SERVER = "chat-server-h5u5.onrender.com"

    // Set this to true to use local server, false for production
    private const val USE_LOCAL_SERVER = false
    
    // Base URLs
    val BASE_HTTP_URL = if (USE_LOCAL_SERVER)
        "http://$LOCAL_SERVER" else "https://$PRODUCTION_SERVER"
    val BASE_WS_URL = if (USE_LOCAL_SERVER)
        "ws://$LOCAL_SERVER" else "wss://$PRODUCTION_SERVER"
    
    // API Endpoints
    const val LOGIN_ENDPOINT = "/auth/login"
    const val REGISTER_ENDPOINT = "/auth/register"
    const val ANONYMOUS_REGISTER_ENDPOINT = "/auth/register/anonymous"
    const val WEBSOCKET_ENDPOINT = "/ws"
    
    // WebSocket path with authentication parameters
    fun getWebSocketPath(username: String, password: String): String {
        return "$WEBSOCKET_ENDPOINT/chat/$username/$password"
    }
    
    // Extract host from the WebSocket URL
    val wsHost: String
        get() {
            val url = BASE_WS_URL.removePrefix("wss://").removePrefix("ws://")
            return try {
                url.split(":")[0]
            } catch (e: Exception) {
                PRODUCTION_SERVER
            }
        }
    
    // Extract port from the WebSocket URL or use default
    val wsPort: Int
        get() {
            return try {
                val url = BASE_WS_URL.removePrefix("wss://").removePrefix("ws://")
                if (url.contains(":")) {
                    val parts = url.split(":")
                    if (parts.size > 1) {
                        parts[1].toIntOrNull() ?: getDefaultPort()
                    } else {
                        getDefaultPort()
                    }
                } else {
                    getDefaultPort()
                }
            } catch (e: Exception) {
                getDefaultPort()
            }
        }
    
    // Get default port based on protocol
    private fun getDefaultPort(): Int {
        return if (BASE_WS_URL.startsWith("wss")) 443
               else if (BASE_WS_URL.startsWith("ws")) 80
               else if (USE_LOCAL_SERVER) 8001
               else 443
    }
} 