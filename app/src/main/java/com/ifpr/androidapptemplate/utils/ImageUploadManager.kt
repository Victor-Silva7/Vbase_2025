package com.ifpr.androidapptemplate.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifpr.androidapptemplate.data.firebase.RealtimeDatabaseImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Gerenciador de upload de imagens para Base64 no Realtime Database
 */
class ImageUploadManager private constructor() {

    companion object {
        private const val TAG = "ImageUploadManager"

        @Volatile
        private var INSTANCE: ImageUploadManager? = null

        fun getInstance(): ImageUploadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageUploadManager().also { INSTANCE = it }
            }
        }
    }

    private val realtimeManager = RealtimeDatabaseImageManager.getInstance()
    private val uploadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _uploadProgress = MutableLiveData<UploadProgress>()
    val uploadProgress: LiveData<UploadProgress> = _uploadProgress

    private val _uploadStatus = MutableLiveData<UploadStatus>()
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus

    /**
     * Upload de imagens de plantas para Base64 no Realtime Database
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

                val path = "plantas/$plantId"
                val result = realtimeManager.saveImages(
                    context = context,
                    imageUris = imageUris,
                    path = path
                ) { progress ->
                    _uploadProgress.postValue(UploadProgress(
                        currentStep = 1,
                        progress = progress.toDouble()
                    ))
                }

                result.fold(
                    onSuccess = { imageIds ->
                        _uploadStatus.postValue(UploadStatus.SUCCESS)
                        onSuccess(imageIds)
                    },
                    onFailure = { throwable ->
                        val exception = if (throwable is Exception) throwable else Exception(throwable)
                        Log.e(TAG, "Erro no upload: ${exception.message}", exception)
                        _uploadStatus.postValue(UploadStatus.ERROR)
                        onFailure(exception)
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Erro geral: ${e.message}", e)
                _uploadStatus.postValue(UploadStatus.ERROR)
                onFailure(e)
            }
        }
    }

    /**
     * Upload insect images using Base64 on Realtime Database
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

                val path = "insetos/$insectId"
                val result = realtimeManager.saveImages(
                    context = context,
                    imageUris = imageUris,
                    path = path
                ) { progress ->
                    _uploadProgress.postValue(UploadProgress(
                        currentStep = 1,
                        progress = progress.toDouble()
                    ))
                }

                result.fold(
                    onSuccess = { imageIds ->
                        _uploadStatus.postValue(UploadStatus.SUCCESS)
                        onSuccess(imageIds)
                    },
                    onFailure = { throwable ->
                        val exception = if (throwable is Exception) throwable else Exception(throwable)
                        Log.e(TAG, "Erro no upload: ${exception.message}", exception)
                        _uploadStatus.postValue(UploadStatus.ERROR)
                        onFailure(exception)
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Erro geral: ${e.message}", e)
                _uploadStatus.postValue(UploadStatus.ERROR)
                onFailure(e)
            }
        }
    }

    enum class UploadStatus {
        STARTING,
        UPLOADING,
        SUCCESS,
        ERROR
    }
}