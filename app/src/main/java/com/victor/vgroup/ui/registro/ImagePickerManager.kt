package com.victor.vgroup.ui.registro

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages image selection from camera and gallery
 * Handles permissions, launchers, and temporary file management
 */
class ImagePickerManager(
    private val activity: AppCompatActivity,
    private val onImagesSelected: (List<Uri>) -> Unit,
    private val onImageFromCamera: () -> Unit
) {
    
    private val CAMERA_PERMISSION_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST = 101
    
    private var currentPhotoUri: Uri? = null
    
    // Activity Result Launchers
    private val cameraLauncher: ActivityResultLauncher<Uri> = 
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                onImageFromCamera()
            }
        }
    
    // Photo Picker for single image (Android 13+ and backported via Google Play Services)
    private val galleryPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> = 
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                onImagesSelected(listOf(uri))
            }
        }
    
    // Legacy gallery picker for older devices (single image)
    private val legacyGalleryLauncher: ActivityResultLauncher<String> = 
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                onImagesSelected(listOf(uri))
            }
        }
    
    /**
     * Request to take a photo with camera
     */
    fun takePhoto() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }
        
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            photoFile
        )
        
        currentPhotoUri?.let { cameraLauncher.launch(it) }
    }
    
    /**
     * Request to select images from gallery
     * Photo Picker doesn't require permissions on Android 13+
     */
    fun selectFromGallery() {
        try {
            // Try Photo Picker first (Android 13+ and backported to older versions via Google Play Services)
            galleryPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            // Fallback to legacy gallery picker
            if (checkStoragePermission()) {
                legacyGalleryLauncher.launch("image/*")
            } else {
                requestStoragePermission()
            }
        }
    }
    
    /**
     * Get current photo URI (used by ViewModel after camera capture)
     */
    fun getCurrentPhotoUri(): Uri? = currentPhotoUri
    
    /**
     * Handle permission results from Activity
     */
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFromGallery()
                }
            }
        }
    }
    
    // Permission checks
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST
        )
    }
    
    // Helper methods
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
}
