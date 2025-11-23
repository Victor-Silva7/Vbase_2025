package com.ifpr.androidapptemplate.ui.registro

import android.Manifest
import android.app.Activity
import android.content.Context
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
    
    private val galleryPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> = 
        activity.registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                onImagesSelected(uris)
            }
        }
    
    private val legacyGalleryLauncher: ActivityResultLauncher<String> = 
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                onImagesSelected(uris)
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
     */
    fun selectFromGallery() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }
        
        try {
            galleryPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            // Fallback for older devices
            legacyGalleryLauncher.launch("image/*")
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
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun requestStoragePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
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
