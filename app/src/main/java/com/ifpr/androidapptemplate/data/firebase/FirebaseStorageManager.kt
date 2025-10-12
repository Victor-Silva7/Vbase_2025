package com.ifpr.androidapptemplate.data.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

/**
 * Firebase Storage Manager for V Group - Manejo Verde
 * Handles all image upload and download operations for plant and insect registrations
 * with compression and progress tracking
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
    
    private val storage: FirebaseStorage by lazy {
        FirebaseConfig.getStorage()
    }

    private val storageRef: StorageReference by lazy {
        storage.reference
    }

    // Storage paths for different types of content
    private val plantsPath = "plantas"
    private val insectsPath = "insetos"
    private val profilesPath = "perfis"
    private val postsPath = "postagens"

    /**
     * Upload image with compression and progress tracking
     */
    suspend fun uploadImage(
        imageUri: Uri,
        userId: String,
        registrationId: String,
        context: Context,
        onProgress: ((Int) -> Unit)? = null
    ): Result<String> {
        return try {
            // Compress image before upload
            val compressedData = compressImage(imageUri, context)
            
            // Create unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef
                .child("registrations")
                .child(userId)
                .child(registrationId)
                .child(fileName)

            // Upload with progress tracking
            val uploadTask = imageRef.putBytes(compressedData)
            
            onProgress?.let { callback ->
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    callback(progress)
                }
            }

            // Wait for upload completion
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload multiple images
     */
    suspend fun uploadImages(
        imageUris: List<Uri>,
        userId: String,
        registrationId: String,
        context: Context,
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()
            
            imageUris.forEachIndexed { index, uri ->
                val result = uploadImage(uri, userId, registrationId, context) { progress ->
                    onProgress?.invoke(index + 1, progress)
                }
                
                result.onSuccess { url ->
                    downloadUrls.add(url)
                }.onFailure { exception ->
                    throw exception
                }
            }
            
            Result.success(downloadUrls)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        
        uploadImageLegacy(imageRef, imageUri, onSuccess, onFailure, onProgress)
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
        
        uploadImageLegacy(imageRef, imageUri, onSuccess, onFailure, onProgress)
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
     * Delete image from storage
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete multiple images
     */
    suspend fun deleteImages(imageUrls: List<String>): Result<Unit> {
        return try {
            imageUrls.forEach { url ->
                val result = deleteImage(url)
                result.onFailure { exception ->
                    throw exception
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all images for a registration
     */
    suspend fun deleteRegistrationImages(userId: String, registrationId: String): Result<Unit> {
        return try {
            val registrationRef = storageRef
                .child("registrations")
                .child(userId)
                .child(registrationId)

            // List all files in the registration folder
            val listResult = registrationRef.listAll().await()
            
            // Delete each file
            listResult.items.forEach { item ->
                item.delete().await()
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compress image to reduce file size
     */
    private fun compressImage(imageUri: Uri, context: Context): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Calculate compression quality based on image size
        val quality = when {
            bitmap.byteCount > 5_000_000 -> 60 // Large images: 60% quality
            bitmap.byteCount > 2_000_000 -> 75 // Medium images: 75% quality
            else -> 85 // Small images: 85% quality
        }

        // Compress to JPEG with calculated quality
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    /**
     * Get storage usage for a user
     */
    suspend fun getUserStorageUsage(userId: String): Result<Long> {
        return try {
            val userRef = storageRef.child("registrations").child(userId)
            val listResult = userRef.listAll().await()
            
            var totalSize = 0L
            listResult.items.forEach { item ->
                val metadata = item.metadata.await()
                totalSize += metadata.sizeBytes
            }
            
            Result.success(totalSize)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Legacy methods for backward compatibility
    
    private fun uploadImageLegacy(
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
            
            uploadImageLegacy(
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
