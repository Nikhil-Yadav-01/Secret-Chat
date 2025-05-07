package com.rudraksha.secretchat.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Manages user session and auto-logout
 */
class SessionManager(
    private val context: Context,
    private val securePreferences: SecurePreferences,
    private val logoutCallback: () -> Unit
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    
    private val handler = Handler(Looper.getMainLooper())
    private var logoutRunnable: Runnable? = null
    
    init {
        if (context is Application) {
            context.registerActivityLifecycleCallbacks(this)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        setupLogoutTimer()
    }
    
    private fun setupLogoutTimer() {
        logoutRunnable = Runnable {
            if (securePreferences.hasSessionTimedOut()) {
                logoutCallback()
            }
        }
    }
    
    /**
     * Start the auto-logout timer
     */
    private fun startLogoutTimer() {
        cancelLogoutTimer()
        
        // Only start timer if auto-logout is enabled (timeout > 0)
        if (securePreferences.autoLogoutTimeMinutes > 0) {
            val timeoutMs = securePreferences.autoLogoutTimeMinutes * 60 * 1000L
            handler.postDelayed(logoutRunnable!!, timeoutMs)
        }
    }
    
    /**
     * Cancel the auto-logout timer
     */
    private fun cancelLogoutTimer() {
        logoutRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
    
    /**
     * Update last active timestamp
     */
    fun updateLastActive() {
        securePreferences.updateLastActive()
    }
    
    /**
     * Set auto-logout time in minutes
     */
    fun setAutoLogoutTime(minutes: Int) {
        securePreferences.autoLogoutTimeMinutes = minutes
        if (minutes > 0) {
            startLogoutTimer()
        } else {
            cancelLogoutTimer()
        }
    }
    
    /**
     * Called when app is foregrounded
     */
    override fun onStart(owner: LifecycleOwner) {
        if (securePreferences.hasSessionTimedOut()) {
            logoutCallback()
        } else {
            updateLastActive()
        }
    }
    
    /**
     * Called when app is backgrounded
     */
    override fun onStop(owner: LifecycleOwner) {
        startLogoutTimer()
    }
    
    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    
    override fun onActivityStarted(activity: Activity) {
        updateLastActive()
    }
    
    override fun onActivityResumed(activity: Activity) {
        cancelLogoutTimer()
        updateLastActive()
    }
    
    override fun onActivityPaused(activity: Activity) {}
    
    override fun onActivityStopped(activity: Activity) {}
    
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    
    override fun onActivityDestroyed(activity: Activity) {}
    
    /**
     * Cleanup when no longer needed
     */
    fun cleanup() {
        cancelLogoutTimer()
        if (context is Application) {
            context.unregisterActivityLifecycleCallbacks(this)
        }
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
} 