package com.ifpr.androidapptemplate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifpr.androidapptemplate.data.firebase.FirebaseStorageManager
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Enhanced Image Upload Manager for V Group - Manejo Verde
 * Handles image compression, optimization, and batch uploads with progress tracking
 */
class ImageUploadManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: ImageUploadManager? = null
        
        fun getInstance(): ImageUploadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageUploadManager().also { INSTANCE = it }
            }
        }
        
        private const val TAG = "ImageUploadManager"
        private const val MAX_IMAGE_WIDTH = 1920
        private const val MAX_IMAGE_HEIGHT = 1920
        private const val JPEG_QUALITY = 85
        private const val MAX_FILE_SIZE = 2 * 1024 * 1024 // 2MB
    }
    
    private val storageManager = FirebaseStorageManager.getInstance()
    private val uploadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Upload progress tracking
    private val _uploadProgress = MutableLiveData<UploadProgress>()
    val uploadProgress: LiveData<UploadProgress> = _uploadProgress
    
    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus
    
    /**
     * Upload plant images with compression and progress tracking
     */
    fun uploadPlantImages(
        context: Context,
        plantId: String,
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        uploadScope.launch {
            try {
                _uploadStatus.postValue(UploadStatus.STARTING)
                
                val compressedImages = compressImages(context, imageUris)
                
                _uploadStatus.postValue(UploadStatus.UPLOADING)
                
                uploadCompressedImages(
                    images = compressedImages,
                    uploadFunction = { images, onSuccessCallback, onFailureCallback, onProgressCallback ->
                        storageManager.uploadPlantImages(
                            plantId = plantId,
                            imageUris = images,
                            onSuccess = onSuccessCallback,
                            onFailure = onFailureCallback,
                            onProgress = onProgressCallback
                        )
                    },
                    onSuccess = { urls ->
                        _uploadStatus.postValue(UploadStatus.SUCCESS)
                        onSuccess(urls)
                        cleanupTempFiles(compressedImages)
                    },
                    onFailure = { exception ->
                        _uploadStatus.postValue(UploadStatus.FAILED)
                        onFailure(exception)
                        cleanupTempFiles(compressedImages)
                    }
                )
                
            } catch (e: Exception) {
                _uploadStatus.postValue(UploadStatus.FAILED)
                onFailure(e)
            }
        }
    }
    
    /**
     * Upload insect images with compression and progress tracking
     */
    fun uploadInsectImages(
        context: Context,
        insectId: String,
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        uploadScope.launch {
            try {
                _uploadStatus.postValue(UploadStatus.STARTING)
                
                val compressedImages = compressImages(context, imageUris)
                
                _uploadStatus.postValue(UploadStatus.UPLOADING)
                
                uploadCompressedImages(
                    images = compressedImages,
                    uploadFunction = { images, onSuccessCallback, onFailureCallback, onProgressCallback ->
                        storageManager.uploadInsectImages(
                            insectId = insectId,
                            imageUris = images,
                            onSuccess = onSuccessCallback,
                            onFailure = onFailureCallback,
                            onProgress = onProgressCallback
                        )
                    },
                    onSuccess = { urls ->
                        _uploadStatus.postValue(UploadStatus.SUCCESS)
                        onSuccess(urls)
                        cleanupTempFiles(compressedImages)
                    },
                    onFailure = { exception ->
                        _uploadStatus.postValue(UploadStatus.FAILED)
                        onFailure(exception)
                        cleanupTempFiles(compressedImages)
                    }
                )
                
            } catch (e: Exception) {
                _uploadStatus.postValue(UploadStatus.FAILED)
                onFailure(e)
            }
        }
    }
    
    /**
     * Compress images to optimize upload size and quality
     */
    private suspend fun compressImages(context: Context, imageUris: List<Uri>): List<Uri> {
        return withContext(Dispatchers.IO) {
            val compressedUris = mutableListOf<Uri>()
            
            imageUris.forEachIndexed { index, uri ->
                try {
                    _uploadProgress.postValue(
                        UploadProgress(
                            currentStep = "Comprimindo imagem ${index + 1}/${imageUris.size}",
                            progress = ((index.toFloat() / imageUris.size) * 50).toInt() // 50% for compression
                        )
                    )
                    
                    val compressedUri = compressImage(context, uri)
                    compressedUris.add(compressedUri)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to compress image: ${e.message}")
                    // Use original image if compression fails
                    compressedUris.add(uri)
                }
            }
            
            compressedUris
        }
    }
    
    /**
     * Compress a single image
     */
    private fun compressImage(context: Context, imageUri: Uri): Uri {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Rotate image if needed based on EXIF data
        val rotatedBitmap = rotateImageIfRequired(context, originalBitmap, imageUri)
        
        // Resize image if too large
        val resizedBitmap = resizeImage(rotatedBitmap)
        
        // Compress to JPEG
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        val compressedData = outputStream.toByteArray()
        
        // Save compressed image to temp file
        val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val fileOutputStream = FileOutputStream(tempFile)
        fileOutputStream.write(compressedData)
        fileOutputStream.close()
        
        // Clean up bitmaps
        if (rotatedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
        resizedBitmap.recycle()
        
        return Uri.fromFile(tempFile)
    }
    
    /**
     * Rotate image based on EXIF orientation data
     */
    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            inputStream?.use {
                val exif = ExifInterface(it)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                    else -> bitmap
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read EXIF data: ${e.message}")
        }
        
        return bitmap
    }
    
    /**
     * Rotate bitmap by degrees
     */
    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
    
    /**
     * Resize image if it exceeds maximum dimensions
     */
    private fun resizeImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            MAX_IMAGE_WIDTH to (MAX_IMAGE_WIDTH / aspectRatio).toInt()
        } else {
            // Portrait or square
            (MAX_IMAGE_HEIGHT * aspectRatio).toInt() to MAX_IMAGE_HEIGHT
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Upload compressed images with progress tracking
     */
    private fun uploadCompressedImages(
        images: List<Uri>,
        uploadFunction: (List<Uri>, (List<String>) -> Unit, (Exception) -> Unit, (Double) -> Unit) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        uploadFunction(
            images,
            onSuccess,
            onFailure
        ) { progress ->
            // Combine compression progress (50%) with upload progress (50%)
            val totalProgress = 50 + (progress * 0.5).toInt()
            _uploadProgress.postValue(
                UploadProgress(
                    currentStep = "Fazendo upload das imagens...",
                    progress = totalProgress
                )
            )
        }
    }
    
    /**
     * Clean up temporary compressed files
     */
    private fun cleanupTempFiles(compressedUris: List<Uri>) {
        compressedUris.forEach { uri ->
            try {
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: return@forEach)
                    if (file.exists() && file.name.startsWith("compressed_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cleanup temp file: ${e.message}")
            }
        }
    }
    
    /**
     * Cancel ongoing uploads
     */
    fun cancelUploads() {
        uploadScope.coroutineContext.cancelChildren()
        _uploadStatus.postValue(UploadStatus.CANCELLED)
    }
    
    /**
     * Check if image needs compression
     */
    fun needsCompression(context: Context, imageUri: Uri): Boolean {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            val width = options.outWidth
            val height = options.outHeight
            val fileSize = getImageFileSize(context, imageUri)
            
            return width > MAX_IMAGE_WIDTH || 
                   height > MAX_IMAGE_HEIGHT || 
                   fileSize > MAX_FILE_SIZE
                   
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check compression needs: ${e.message}")
            return true // Assume compression is needed if we can't determine
        }
    }
    
    /**
     * Get image file size
     */
    private fun getImageFileSize(context: Context, imageUri: Uri): Long {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val size = inputStream?.available()?.toLong() ?: 0L
            inputStream?.close()
            size
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Upload progress data class
 */
data class UploadProgress(
    val currentStep: String,
    val progress: Int // 0-100
)

/**
 * Upload status enum
 */
enum class UploadStatus {
    IDLE,
    STARTING,
    COMPRESSING,
    UPLOADING,
    SUCCESS,
    FAILED,
    CANCELLED
}