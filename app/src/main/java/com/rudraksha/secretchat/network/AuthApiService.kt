package com.rudraksha.secretchat.network

import android.util.Log
import com.rudraksha.secretchat.data.model.AuthResponse
import com.rudraksha.secretchat.data.model.UserCredentials
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Service for handling authentication API calls
 */
class AuthApiService {
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * Login with email and password to get JWT token
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = httpClient.post("${ApiConfig.BASE_HTTP_URL}${ApiConfig.LOGIN_ENDPOINT}") {
                contentType(ContentType.Application.Json)
                setBody(UserCredentials(email = email, password = password))
            }

            val authResponse = response.body<AuthResponse>()
            Log.d("AuthApiService", "Login successful for user: $email")
            Result.success(authResponse)
        } catch (e: Exception) {
            Log.e("AuthApiService", "Login failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     */
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = httpClient.post("${ApiConfig.BASE_HTTP_URL}${ApiConfig.REGISTER_ENDPOINT}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                ))
            }

            val authResponse = response.body<AuthResponse>()
            Log.d("AuthApiService", "Registration successful for user: $email")
            Result.success(authResponse)
        } catch (e: Exception) {
            Log.e("AuthApiService", "Registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Register anonymously
     */
    suspend fun registerAnonymously(name: String): Result<AuthResponse> {
        return try {
            val response = httpClient.post("${ApiConfig.BASE_HTTP_URL}${ApiConfig.ANONYMOUS_REGISTER_ENDPOINT}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("name" to name))
            }

            val authResponse = response.body<AuthResponse>()
            Log.d("AuthApiService", "Anonymous registration successful for user: $name")
            Result.success(authResponse)
        } catch (e: Exception) {
            Log.e("AuthApiService", "Anonymous registration failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun cleanup() {
        httpClient.close()
    }
} 