package com.rudraksha.secretchat.utils

import android.app.Activity
import android.os.Build
import android.view.WindowManager

/**
 * Utility to block screenshots and screen recordings for privacy
 */
object ScreenshotBlocker {
    
    /**
     * Enable screenshot blocking for an activity
     */
    fun enable(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    
    /**
     * Disable screenshot blocking for an activity
     */
    fun disable(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    
    /**
     * Apply screenshot blocking based on setting
     */
    fun applyFromSettings(activity: Activity, enabled: Boolean) {
        if (enabled) {
            enable(activity)
        } else {
            disable(activity)
        }
    }
} 