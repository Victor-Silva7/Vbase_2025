package com.ifpr.androidapptemplate.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.utils.Base64ImageUtil
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Gerenciador para salvar imagens em Base64 no Realtime Database
 */
class RealtimeDatabaseImageManager private constructor() {
    
    companion object {
        private const val TAG = "RealtimeDBImageManager"
        
        @Volatile
        private var INSTANCE: RealtimeDatabaseImageManager? = null
        
        fun getInstance(): RealtimeDatabaseImageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealtimeDatabaseImageManager().also { INSTANCE = it }
            }
        }
    }
    
    private val database = FirebaseDatabase.getInstance()
    
    /**
     * Salva uma imagem no Realtime Database
     */
    suspend fun saveImage(
        context: Context,
        imageUri: Uri,
        path: String
    ): Result<String> {
        return try {
            // Converte a imagem para Base64
            val base64Image = Base64ImageUtil.toBase64(context, imageUri)
                ?: throw Exception("Falha ao converter imagem para Base64")
                
            // Gera um ID único para a imagem
            val imageId = UUID.randomUUID().toString()
            
            // Salva a imagem no database
            database.reference
                .child(path)
                .child("imagens")
                .child(imageId)
                .setValue(base64Image)
                .await()
                
            Log.d(TAG, "Imagem salva com sucesso: $imageId")
            Result.success(imageId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagem: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Salva múltiplas imagens
     */
    suspend fun saveImages(
        context: Context,
        imageUris: List<Uri>,
        path: String,
        onProgress: (progress: Int) -> Unit
    ): Result<List<String>> {
        return try {
            val imageIds = mutableListOf<String>()
            val totalImages = imageUris.size
            
            imageUris.forEachIndexed { index, uri ->
                // Converte a imagem para Base64
                val base64Image = Base64ImageUtil.toBase64(context, uri)
                    ?: throw Exception("Falha ao converter imagem para Base64")
                
                // Gera um ID único para a imagem
                val imageId = UUID.randomUUID().toString()
                
                // Cria o caminho no database
                val imagePath = "$path/imagens/$imageId"
                
                // Salva a imagem no database
                database.reference.child(imagePath)
                    .setValue(base64Image)
                    .await()
                
                // Adiciona a referência da imagem
                imageIds.add(imageId)
                
                // Atualiza o progresso
                onProgress(((index + 1) * 100) / totalImages)
            }
            
            Log.d(TAG, "Todas as imagens salvas com sucesso: ${imageIds.size}")
            Result.success(imageIds)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagens: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Salva imagens de uma planta
     */
    suspend fun savePlantImages(
        context: Context,
        plantId: String,
        imageUris: List<Uri>,
        onProgress: (progress: Int) -> Unit
    ): Result<List<String>> {
        val path = "usuarios/${FirebaseConfig.getCurrentUserId()}/plantas/$plantId"
        return saveImages(context, imageUris, path, onProgress)
    }
    
    /**
     * Salva imagens de um inseto
     */
    suspend fun saveInsectImages(
        context: Context,
        insectId: String,
        imageUris: List<Uri>,
        onProgress: (progress: Int) -> Unit
    ): Result<List<String>> {
        val path = "usuarios/${FirebaseConfig.getCurrentUserId()}/insetos/$insectId"
        return saveImages(context, imageUris, path, onProgress)
    }
    
    /**
     * Remove uma imagem do database
     */
    suspend fun deleteImage(path: String, imageId: String): Result<Unit> {
        return try {
            database.reference
                .child(path)
                .child("imagens")
                .child(imageId)
                .removeValue()
                .await()
            
            Log.d(TAG, "Imagem deletada com sucesso: $imageId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar imagem: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove múltiplas imagens
     */
    suspend fun deleteImages(path: String, imageIds: List<String>): Result<Unit> {
        return try {
            imageIds.forEach { imageId ->
                val result = deleteImage(path, imageId)
                if (result.isFailure) {
                    throw result.exceptionOrNull()!!
                }
            }
            Log.d(TAG, "Todas as imagens deletadas com sucesso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar imagens: ${e.message}", e)
            Result.failure(e)
        }
    }
}