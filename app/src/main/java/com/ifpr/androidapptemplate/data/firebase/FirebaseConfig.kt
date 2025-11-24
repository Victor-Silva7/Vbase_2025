package com.ifpr.androidapptemplate.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Firebase Configuration Manager for V Group - Manejo Verde
 * Centralizes Firebase initialization and configuration
 */
object FirebaseConfig {
    
    private var isInitialized = false
    
    /**
     * Initialize Firebase services with V Group specific configurations
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(context)
            
            // Configure Firebase Database
            configureDatabase()
            
            // Configure Firebase Storage
            configureStorage()
            
            // Configure Firebase Auth
            configureAuth()
            
            isInitialized = true
            
        } catch (e: Exception) {
            throw FirebaseInitializationException("Failed to initialize Firebase: ${e.message}", e)
        }
    }
    
    /**
     * Configure Firebase Realtime Database settings
     */
    private fun configureDatabase() {
        val database = FirebaseDatabase.getInstance()
        
        // Enable offline persistence for better UX
        database.setPersistenceEnabled(true)
        
        // Set cache size (100MB for plant/insect data)
        database.setPersistenceCacheSizeBytes(100 * 1024 * 1024)
        
        // Configure database URL if needed (for custom instances)
        // database.useEmulator("10.0.2.2", 9000) // For development only
    }
    
    /**
     * Configure Firebase Storage settings
     */
    private fun configureStorage() {
        val storage = FirebaseStorage.getInstance()
        
        // Set upload timeout (30 seconds for large images)
        storage.maxUploadRetryTimeMillis = 30000
        
        // Set download timeout (20 seconds)
        storage.maxDownloadRetryTimeMillis = 20000
        
        // Configure storage emulator for development
        // storage.useEmulator("10.0.2.2", 9199) // For development only
    }
    
    /**
     * Configure Firebase Authentication settings
     */
    private fun configureAuth() {
        val auth = FirebaseAuth.getInstance()
        
        // Set language code for auth messages
        auth.setLanguageCode("pt-BR")
        
        // Configure auth state persistence
        // Auth state is automatically persisted by default
    }
    
    /**
     * Get Firebase Database instance with V Group configuration
     */
    fun getDatabase(): FirebaseDatabase {
        checkInitialization()
        return FirebaseDatabase.getInstance()
    }
    
    /**
     * Get Firebase Storage instance with V Group configuration
     */
    fun getStorage(): FirebaseStorage {
        checkInitialization()
        return FirebaseStorage.getInstance()
    }
    
    /**
     * Get Firebase Auth instance with V Group configuration
     */
    fun getAuth(): FirebaseAuth {
        checkInitialization()
        return FirebaseAuth.getInstance()
    }
    
    /**
     * Get Firebase Storage Manager instance
     */
    fun getStorageManager(): FirebaseStorageManager {
        checkInitialization()
        return FirebaseStorageManager.getInstance()
    }
    
    /**
     * Get Firebase Database Service instance
     */
    fun getDatabaseService(): FirebaseDatabaseService {
        checkInitialization()
        return FirebaseDatabaseService.getInstance()
    }
    
    /**
     * Get Realtime Database Image Manager instance
     */
    fun getRealtimeDatabaseImageManager(): RealtimeDatabaseImageManager {
        checkInitialization()
        return RealtimeDatabaseImageManager.getInstance()
    }
    
    /**
     * Database structure for V Group
     */
    object DatabasePaths {
        const val USUARIOS = "usuarios"
        const val PLANTAS = "plantas"
        const val INSETOS = "insetos"
        const val POSTAGENS = "postagens"
        const val CATEGORIAS = "categorias"
        const val ESTATISTICAS = "estatisticas"
        
        // User-specific paths
        fun userPlantas(userId: String) = "$USUARIOS/$userId/plantas"
        fun userInsetos(userId: String) = "$USUARIOS/$userId/insetos"
        fun userPostagens(userId: String) = "$USUARIOS/$userId/postagens"
        fun userProfile(userId: String) = "$USUARIOS/$userId/perfil"
        
        // Public paths for community features
        const val PUBLIC_PLANTAS = "publico/plantas"
        const val PUBLIC_INSETOS = "publico/insetos"
        const val PUBLIC_POSTAGENS = "publico/postagens"
    }
    
    /**
     * Storage structure for V Group
     */
    object StoragePaths {
        const val PLANTAS = "plantas"
        const val INSETOS = "insetos"
        const val PERFIS = "perfis"
        const val POSTAGENS = "postagens"
        const val TEMP = "temp"
        
        fun plantImages(plantId: String) = "$PLANTAS/$plantId"
        fun insectImages(insectId: String) = "$INSETOS/$insectId"
        fun userProfile(userId: String) = "$PERFIS/$userId"
        fun postImages(postId: String) = "$POSTAGENS/$postId"
        fun tempImages(sessionId: String) = "$TEMP/$sessionId"
    }
    
    /**
     * Firebase Security Rules constants
     */
    object SecurityRules {
        const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10MB
        const val MAX_IMAGES_PER_REGISTRATION = 1
        const val ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/webp"
        
        fun isValidImageType(contentType: String?): Boolean {
            return contentType?.let { 
                ALLOWED_IMAGE_TYPES.split(",").contains(it) 
            } ?: false
        }
    }
    
    /**
     * Check if Firebase is properly initialized
     */
    private fun checkInitialization() {
        if (!isInitialized) {
            throw IllegalStateException("Firebase not initialized. Call FirebaseConfig.initialize() first.")
        }
    }
    
    /**
     * Get initialization status
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get current authenticated user
     */
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    
    /**
     * Get current authenticated user ID
     */
    fun getCurrentUserId(): String? {
        checkInitialization()
        return FirebaseAuth.getInstance().currentUser?.uid
    }
    
    /**
     * Get current authenticated user name
     */
    fun getCurrentUserName(): String? {
        checkInitialization()
        return FirebaseAuth.getInstance().currentUser?.displayName
    }
    
    /**
     * Get current authenticated user email
     */
    fun getCurrentUserEmail(): String? {
        checkInitialization()
        return FirebaseAuth.getInstance().currentUser?.email
    }
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        checkInitialization()
        return FirebaseAuth.getInstance().currentUser != null
    }
    
    /**
     * Reset initialization status (for testing purposes)
     */
    internal fun resetForTesting() {
        isInitialized = false
    }
}

/**
 * Custom exception for Firebase initialization errors
 */
class FirebaseInitializationException(message: String, cause: Throwable? = null) : Exception(message, cause)