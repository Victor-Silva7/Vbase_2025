package com.ifpr.androidapptemplate.data.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Firebase Storage Manager for V Group - Manejo Verde
 * Handles all image upload and download operations for plant and insect registrations
 */
class FirebaseStorageManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseStorageManager? = null
        
        fun getInstance(): FirebaseStorageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseStorageManager().also { INSTANCE = it }
            }
        }
    }
    
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    
    // Storage paths for different types of content
    private val plantsPath = "plantas"
    private val insectsPath = "insetos"
    private val profilesPath = "perfis"
    private val postsPath = "postagens"
    
    /**
     * Upload a single image for plant registration
     */
    fun uploadPlantImage(
        plantId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        val fileName = generateImageFileName("planta")
        val imageRef = storageRef.child("$plantsPath/$plantId/$fileName")
        
        uploadImage(imageRef, imageUri, onSuccess, onFailure, onProgress)
    }
    
    /**
     * Upload multiple images for plant registration
     */
    fun uploadPlantImages(
        plantId: String,
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        uploadMultipleImages(
            "$plantsPath/$plantId",
            imageUris,
            "planta",
            onSuccess,
            onFailure,
            onProgress
        )
    }
    
    /**
     * Upload a single image for insect registration
     */
    fun uploadInsectImage(
        insectId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        val fileName = generateImageFileName("inseto")
        val imageRef = storageRef.child("$insectsPath/$insectId/$fileName")
        
        uploadImage(imageRef, imageUri, onSuccess, onFailure, onProgress)
    }
    
    /**
     * Upload multiple images for insect registration
     */
    fun uploadInsectImages(
        insectId: String,
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        uploadMultipleImages(
            "$insectsPath/$insectId",
            imageUris,
            "inseto",
            onSuccess,
            onFailure,
            onProgress
        )
    }
    
    /**
     * Upload profile image
     */
    fun uploadProfileImage(
        userId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        val fileName = generateImageFileName("perfil")
        val imageRef = storageRef.child("$profilesPath/$userId/$fileName")
        
        uploadImage(imageRef, imageUri, onSuccess, onFailure, onProgress)
    }
    
    /**
     * Upload image for social post
     */
    fun uploadPostImage(
        postId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit = {}
    ) {
        val fileName = generateImageFileName("post")
        val imageRef = storageRef.child("$postsPath/$postId/$fileName")
        
        uploadImage(imageRef, imageUri, onSuccess, onFailure, onProgress)
    }
    
    /**
     * Delete an image from storage
     */
    fun deleteImage(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val imageRef = storage.getReferenceFromUrl(downloadUrl)
            imageRef.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
    
    /**
     * Delete all images for a specific registration
     */
    fun deleteRegistrationImages(
        type: String, // "plantas" or "insetos"
        registrationId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val folderRef = storageRef.child("$type/$registrationId")
        
        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isEmpty()) {
                    onSuccess()
                    return@addOnSuccessListener
                }
                
                var deletedCount = 0
                val totalItems = listResult.items.size
                
                listResult.items.forEach { item ->
                    item.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == totalItems) {
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    
    /**
     * Get storage statistics for user content
     */
    fun getUserStorageStats(
        userId: String,
        onSuccess: (StorageStats) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Implementation would count user's images across all categories
        // This is a placeholder for future implementation
        val stats = StorageStats(
            totalImages = 0,
            plantImages = 0,
            insectImages = 0,
            postImages = 0,
            storageUsed = 0L
        )
        onSuccess(stats)
    }
    
    // Private helper methods
    
    private fun uploadImage(
        imageRef: StorageReference,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit
    ) {
        val uploadTask = imageRef.putFile(imageUri)
        
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            onProgress(progress)
        }.addOnSuccessListener {
            // Get download URL
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
    
    private fun uploadMultipleImages(
        basePath: String,
        imageUris: List<Uri>,
        prefix: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onProgress: (Double) -> Unit
    ) {
        if (imageUris.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        
        val downloadUrls = mutableListOf<String>()
        var uploadedCount = 0
        var totalProgress = 0.0
        
        imageUris.forEachIndexed { index, uri ->
            val fileName = generateImageFileName(prefix, index)
            val imageRef = storageRef.child("$basePath/$fileName")
            
            uploadImage(
                imageRef = imageRef,
                imageUri = uri,
                onSuccess = { downloadUrl ->
                    downloadUrls.add(downloadUrl)
                    uploadedCount++
                    
                    if (uploadedCount == imageUris.size) {
                        onSuccess(downloadUrls)
                    }
                },
                onFailure = { exception ->
                    onFailure(exception)
                },
                onProgress = { progress ->
                    totalProgress = ((uploadedCount * 100.0) + progress) / imageUris.size
                    onProgress(totalProgress)
                }
            )
        }
    }
    
    private fun generateImageFileName(prefix: String, index: Int? = null): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        val suffix = index?.let { "_$it" } ?: ""
        return "${prefix}_${timestamp}${suffix}.jpg"
    }
    
    /**
     * Create optimized storage reference for better organization
     */
    fun getStorageReference(path: String): StorageReference {
        return storageRef.child(path)
    }
    
    /**
     * Check if storage is available
     */
    fun isStorageAvailable(): Boolean {
        return try {
            storage.maxUploadRetryTimeMillis
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class for storage statistics
 */
data class StorageStats(
    val totalImages: Int,
    val plantImages: Int,
    val insectImages: Int,
    val postImages: Int,
    val storageUsed: Long // in bytes
)