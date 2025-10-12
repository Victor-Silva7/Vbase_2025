package com.ifpr.androidapptemplate

import android.app.Application
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig

/**
 * Application class for V Group - Manejo Verde
 * Handles app-wide initialization including Firebase configuration
 */
class VGroupApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase with V Group specific configuration
        try {
            FirebaseConfig.initialize(this)
        } catch (e: Exception) {
            // Log error and handle gracefully
            android.util.Log.e("VGroupApp", "Failed to initialize Firebase", e)
        }
    }
}