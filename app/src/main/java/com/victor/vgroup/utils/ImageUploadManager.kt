package com.victor.vgroup.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.victor.vgroup.data.firebase.RealtimeDatabaseImageManager
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
        Log.d(TAG, "🚀 uploadPlantImages INICIADO")
        Log.d(TAG, "🚀 plantId: $plantId")
        Log.d(TAG, "🚀 imageUris.size: ${imageUris.size}")
        uploadScope.launch {
            try {
                Log.d(TAG, "📡 Postando status STARTING")
                _uploadStatus.postValue(UploadStatus.STARTING)

                android.util.Log.wtf("PLANT_UPLOAD", "╔═════════════════════════════════════╗")
                android.util.Log.wtf("PLANT_UPLOAD", "║  🔥 ANTES DE CHAMAR savePlantImages ║")
                android.util.Log.wtf("PLANT_UPLOAD", "╚═════════════════════════════════════╝")
                android.util.Log.wtf("PLANT_UPLOAD", "  realtimeManager: $realtimeManager")
                android.util.Log.wtf("PLANT_UPLOAD", "  plantId: $plantId")
                android.util.Log.wtf("PLANT_UPLOAD", "  imageUris.size: ${imageUris.size}")
                
                Log.d(TAG, "📞 Chamando realtimeManager.savePlantImages...")
                // Usar savePlantImages que já inclui o caminho correto com userId
                val result = realtimeManager.savePlantImages(
                    context = context,
                    plantId = plantId,
                    imageUris = imageUris
                ) { progress ->
                    Log.d(TAG, "📊 Progresso: $progress%")
                    _uploadProgress.postValue(UploadProgress(
                        currentStep = 1,
                        progress = progress.toDouble()
                    ))
                }
                
                android.util.Log.wtf("PLANT_UPLOAD", "╔═════════════════════════════════════╗")
                android.util.Log.wtf("PLANT_UPLOAD", "║  🔥 DEPOIS DE CHAMAR savePlantImages ║")
                android.util.Log.wtf("PLANT_UPLOAD", "╚═════════════════════════════════════╝")
                android.util.Log.wtf("PLANT_UPLOAD", "  result.isSuccess: ${result.isSuccess}")
                android.util.Log.wtf("PLANT_UPLOAD", "  result.getOrNull(): ${result.getOrNull()}")

                Log.d(TAG, "🔍 Result obtido: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val imageIds = result.getOrNull() ?: emptyList()
                    Log.d(TAG, "✅✅✅ uploadPlantImages SUCESSO! ${imageIds.size} imagens")
                    Log.d(TAG, "✅ IDs: $imageIds")
                    _uploadStatus.postValue(UploadStatus.SUCCESS)
                    Log.d(TAG, "📞 Chamando callback onSuccess...")
                    onSuccess(imageIds)
                    Log.d(TAG, "✅ Callback onSuccess EXECUTADO!")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Erro desconhecido")
                    Log.e(TAG, "❌❌❌ uploadPlantImages FALHOU: ${exception.message}", exception)
                    _uploadStatus.postValue(UploadStatus.ERROR)
                    onFailure(exception as? Exception ?: Exception(exception))
                }

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
        Log.d(TAG, "🚀 uploadInsectImages INICIADO")
        Log.d(TAG, "🚀 insectId: $insectId")
        Log.d(TAG, "🚀 imageUris.size: ${imageUris.size}")
        uploadScope.launch {
            try {
                Log.d(TAG, "📡 Postando status STARTING")
                _uploadStatus.postValue(UploadStatus.STARTING)

                android.util.Log.wtf("IMG_UPLOAD", "═══════════════════════════════════")
                android.util.Log.wtf("IMG_UPLOAD", "🔥 ANTES DE CHAMAR saveInsectImages!")
                android.util.Log.wtf("IMG_UPLOAD", "  realtimeManager: $realtimeManager")
                android.util.Log.wtf("IMG_UPLOAD", "  insectId: $insectId")
                android.util.Log.wtf("IMG_UPLOAD", "  imageUris.size: ${imageUris.size}")
                android.util.Log.wtf("IMG_UPLOAD", "═══════════════════════════════════")
                
                android.util.Log.wtf("IMG_MGR", "═══════════════════════════════════")
                android.util.Log.wtf("IMG_MGR", "ANTES de chamar realtimeManager.saveInsectImages()")
                android.util.Log.wtf("IMG_MGR", "realtimeManager = $realtimeManager")
                android.util.Log.wtf("IMG_MGR", "insectId = $insectId")
                android.util.Log.wtf("IMG_MGR", "imageUris.size = ${imageUris.size}")
                android.util.Log.wtf("IMG_MGR", "═══════════════════════════════════")
                
                Log.d(TAG, "📞 Chamando realtimeManager.saveInsectImages...")
                // Usar saveInsectImages que já inclui o caminho correto com userId
                val result = realtimeManager.saveInsectImages(
                    context = context,
                    insectId = insectId,
                    imageUris = imageUris
                ) { progress ->
                    Log.d(TAG, "📊 Progresso: $progress%")
                    _uploadProgress.postValue(UploadProgress(
                        currentStep = 1,
                        progress = progress.toDouble()
                    ))
                }
                
                android.util.Log.wtf("IMG_MGR", "═══════════════════════════════════")
                android.util.Log.wtf("IMG_MGR", "DEPOIS de chamar saveInsectImages()")
                android.util.Log.wtf("IMG_MGR", "result.isSuccess = ${result.isSuccess}")
                android.util.Log.wtf("IMG_MGR", "result.getOrNull() = ${result.getOrNull()}")
                android.util.Log.wtf("IMG_MGR", "═══════════════════════════════════")
                
                android.util.Log.wtf("IMG_UPLOAD", "🔥 DEPOIS DE CHAMAR saveInsectImages!")
                android.util.Log.wtf("IMG_UPLOAD", "  result.isSuccess: ${result.isSuccess}")
                android.util.Log.wtf("IMG_UPLOAD", "  result.getOrNull(): ${result.getOrNull()}")

                Log.d(TAG, "🔍 Result obtido: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val imageIds = result.getOrNull() ?: emptyList()
                    Log.d(TAG, "✅✅✅ uploadInsectImages SUCESSO! ${imageIds.size} imagens")
                    Log.d(TAG, "✅ IDs: $imageIds")
                    _uploadStatus.postValue(UploadStatus.SUCCESS)
                    Log.d(TAG, "📞 Chamando callback onSuccess...")
                    onSuccess(imageIds)
                    Log.d(TAG, "✅ Callback onSuccess EXECUTADO!")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Erro desconhecido")
                    Log.e(TAG, "❌❌❌ uploadInsectImages FALHOU: ${exception.message}", exception)
                    _uploadStatus.postValue(UploadStatus.ERROR)
                    onFailure(exception as? Exception ?: Exception(exception))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro geral em uploadInsectImages: ${e.message}", e)
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