package com.victor.vgroup.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.victor.vgroup.utils.Base64ImageUtil
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
            Log.d(TAG, "🔥 Iniciando salvamento de ${imageUris.size} imagens em: $path")
            val imageIds = mutableListOf<String>()
            val totalImages = imageUris.size
            
            imageUris.forEachIndexed { index, uri ->
                Log.d(TAG, "📸 Processando imagem ${index + 1}/$totalImages: $uri")
                
                // Converte a imagem para Base64
                val base64Image = Base64ImageUtil.toBase64(context, uri)
                    ?: throw Exception("Falha ao converter imagem para Base64")
                
                Log.d(TAG, "✅ Imagem convertida para Base64 (tamanho: ${base64Image.length} chars)")
                
                // Gera um ID único para a imagem
                val imageId = UUID.randomUUID().toString()
                
                // Salva a imagem no database
                val ref = database.reference
                    .child(path)
                    .child("imagens")
                    .child(imageId)
                
                Log.d(TAG, "🔍 ANTES DO SAVE:")
                Log.d(TAG, "  - Path completo: ${ref.path}")
                Log.d(TAG, "  - ImageId: $imageId")
                Log.d(TAG, "  - Base64 size: ${base64Image.length}")
                Log.d(TAG, "  - Primeiros 50 chars: ${base64Image.take(50)}")
                
                try {
                    // Tenta salvar
                    Log.d(TAG, "📤 Chamando setValue()...")
                    ref.setValue(base64Image).await()
                    Log.d(TAG, "✅ setValue() completou SEM ERRO!")
                    
                    // VERIFICAÇÃO IMEDIATA: Confirma que salvou
                    Log.d(TAG, "🔍 Verificando com get()...")
                    val verification = ref.get().await()
                    Log.d(TAG, "  - verification.exists(): ${verification.exists()}")
                    Log.d(TAG, "  - verification.value: ${verification.value?.toString()?.take(50)}")
                    
                    if (!verification.exists()) {
                        // ERRO CRÍTICO: setValue não falhou mas não salvou!
                        throw Exception("CRÍTICO: setValue() sucesso mas get() falhou! Path: ${ref.path}, Base64 size: ${base64Image.length}")
                    }
                    
                    // Sucesso! Adiciona o ID
                    Log.d(TAG, "✅ VERIFICAÇÃO OK! Adicionando imageId à lista")
                    imageIds.add(imageId)
                } catch (e: Exception) {
                    // Reporta erro detalhado
                    Log.e(TAG, "❌ EXCEPTION CAPTURADA: ${e.message}")
                    throw Exception("SAVE FAILED: ${e.message} | Path: ${ref.path} | Base64 size: ${base64Image.length} | ImageId: $imageId")
                }
                
                // Atualiza o progresso
                onProgress(((index + 1) * 100) / totalImages)
            }
            
            Log.d(TAG, "✅✅✅ Todas as imagens salvas com sucesso: ${imageIds.size}")
            
            // DEBUG: Força exception com informações se não salvou
            if (imageIds.isEmpty()) {
                throw Exception("DEBUG SAVE: imageIds está VAZIO após loop! path=$path")
            }
            
            Result.success(imageIds)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagens: ${e.message}", e)
            // Inclui path na exception para debug
            Result.failure(Exception("SAVE ERROR em '$path': ${e.message}"))
        }
    }
    
    /**
     * ✅ CORRIGIDO - Salva imagens de plantas em Base64
     * Path: usuarios/{userId}/plantas/{plantId}/imagens/{imageId}
     */
    suspend fun savePlantImages(
        context: Context,
        plantId: String,
        imageUris: List<Uri>,
        onProgress: (progress: Int) -> Unit
    ): Result<List<String>> {
        return try {
            android.util.Log.wtf("SAVE_PLANT", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_PLANT", "║  savePlantImages() EXECUTANDO!   ║")
            android.util.Log.wtf("SAVE_PLANT", "╚═══════════════════════════════════╝")
            android.util.Log.wtf("SAVE_PLANT", "plantId: $plantId")
            android.util.Log.wtf("SAVE_PLANT", "imageUris.size: ${imageUris.size}")
            
            val userId = FirebaseConfig.getCurrentUserId()
            android.util.Log.wtf("SAVE_PLANT", "userId: $userId")
            
            if (userId == null) {
                throw Exception("Usuário não autenticado")
            }
            
            val imageIds = mutableListOf<String>()
            val totalImages = imageUris.size
            
            imageUris.forEachIndexed { index, uri ->
                android.util.Log.wtf("SAVE_PLANT", "━━━ PROCESSANDO IMAGEM ${index + 1}/$totalImages ━━━")
                
                // 1. Converter para Base64
                android.util.Log.wtf("SAVE_PLANT", "  1️⃣ Convertendo URI para Base64...")
                val base64Image = Base64ImageUtil.toBase64(context, uri)
                if (base64Image == null) {
                    android.util.Log.wtf("SAVE_PLANT", "  ❌ FALHA na conversão!")
                    throw Exception("Falha ao converter imagem para Base64")
                }
                android.util.Log.wtf("SAVE_PLANT", "  ✅ Base64 gerado: ${base64Image.length} chars")
                
                // 2. Gerar ID único
                val imageId = UUID.randomUUID().toString()
                android.util.Log.wtf("SAVE_PLANT", "  2️⃣ UUID gerado: $imageId")
                
                // 3. Construir path completo
                val fullPath = "usuarios/$userId/plantas/$plantId/imagens/$imageId"
                android.util.Log.wtf("SAVE_PLANT", "  3️⃣ Path: $fullPath")
                
                // 4. Salvar no Firebase
                android.util.Log.wtf("SAVE_PLANT", "  4️⃣ Salvando no Firebase...")
                database.reference.child(fullPath).setValue(base64Image).await()
                android.util.Log.wtf("SAVE_PLANT", "  ✅ Salvo com sucesso!")
                
                // 5. Adicionar ID à lista
                imageIds.add(imageId)
                
                // 6. Atualizar progresso
                val progress = ((index + 1) * 100) / totalImages
                onProgress(progress)
                android.util.Log.wtf("SAVE_PLANT", "  📊 Progresso: $progress%")
            }
            
            android.util.Log.wtf("SAVE_PLANT", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_PLANT", "║     ✅ SUCESSO TOTAL!            ║")
            android.util.Log.wtf("SAVE_PLANT", "║  Imagens salvas: ${imageIds.size}              ║")
            android.util.Log.wtf("SAVE_PLANT", "╚═══════════════════════════════════╝")
            
            Result.success(imageIds)
        } catch (e: Exception) {
            android.util.Log.wtf("SAVE_PLANT", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_PLANT", "║     ❌ ERRO CAPTURADO!           ║")
            android.util.Log.wtf("SAVE_PLANT", "╚═══════════════════════════════════╝")
            android.util.Log.wtf("SAVE_PLANT", "Erro: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * ✅ REESCRITO COMPLETAMENTE - Salva imagens de Base64 no Realtime Database
     * Path: usuarios/{userId}/insetos/{insectId}/imagens/{imageId}
     */
    suspend fun saveInsectImages(
        context: Context,
        insectId: String,
        imageUris: List<Uri>,
        onProgress: (progress: Int) -> Unit
    ): Result<List<String>> {
        return try {
            android.util.Log.wtf("SAVE_IMG", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_IMG", "║  saveInsectImages() EXECUTANDO!  ║")
            android.util.Log.wtf("SAVE_IMG", "╚═══════════════════════════════════╝")
            android.util.Log.wtf("SAVE_IMG", "insectId: $insectId")
            android.util.Log.wtf("SAVE_IMG", "imageUris.size: ${imageUris.size}")
            
            val userId = FirebaseConfig.getCurrentUserId()
            android.util.Log.wtf("SAVE_IMG", "userId: $userId")
            
            val imageIds = mutableListOf<String>()
            val totalImages = imageUris.size
            
            imageUris.forEachIndexed { index, uri ->
                android.util.Log.wtf("SAVE_IMG", "═══ PROCESSANDO IMAGEM ${index + 1}/$totalImages ═══")
                
                // 1. Converter para Base64
                android.util.Log.wtf("SAVE_IMG", "  1️⃣ Convertendo URI para Base64...")
                val base64Image = Base64ImageUtil.toBase64(context, uri)
                if (base64Image == null) {
                    android.util.Log.wtf("SAVE_IMG", "  ❌ FALHA na conversão!")
                    throw Exception("Falha ao converter imagem para Base64")
                }
                android.util.Log.wtf("SAVE_IMG", "  ✅ Base64 gerado: ${base64Image.length} chars")
                
                // 2. Gerar ID único
                val imageId = UUID.randomUUID().toString()
                android.util.Log.wtf("SAVE_IMG", "  2️⃣ UUID gerado: $imageId")
                
                // 3. Construir path completo
                val fullPath = "usuarios/$userId/insetos/$insectId/imagens/$imageId"
                android.util.Log.wtf("SAVE_IMG", "  3️⃣ Path: $fullPath")
                
                // 4. Salvar no Firebase
                android.util.Log.wtf("SAVE_IMG", "  4️⃣ Salvando no Firebase...")
                database.reference.child(fullPath).setValue(base64Image).await()
                android.util.Log.wtf("SAVE_IMG", "  ✅ Salvo com sucesso!")
                
                // 5. Adicionar ID à lista
                imageIds.add(imageId)
                
                // 6. Atualizar progresso
                val progress = ((index + 1) * 100) / totalImages
                onProgress(progress)
                android.util.Log.wtf("SAVE_IMG", "  📊 Progresso: $progress%")
            }
            
            android.util.Log.wtf("SAVE_IMG", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_IMG", "║     ✅ SUCESSO TOTAL!            ║")
            android.util.Log.wtf("SAVE_IMG", "║  Imagens salvas: ${imageIds.size}              ║")
            android.util.Log.wtf("SAVE_IMG", "╚═══════════════════════════════════╝")
            
            Result.success(imageIds)
        } catch (e: Exception) {
            android.util.Log.wtf("SAVE_IMG", "╔═══════════════════════════════════╗")
            android.util.Log.wtf("SAVE_IMG", "║     ❌ ERRO CAPTURADO!           ║")
            android.util.Log.wtf("SAVE_IMG", "╚═══════════════════════════════════╝")
            android.util.Log.wtf("SAVE_IMG", "Erro: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Recupera uma imagem Base64 do database
     */
    suspend fun getImage(path: String, imageId: String): Result<String> {
        return try {
            val snapshot = database.reference
                .child(path)
                .child("imagens")
                .child(imageId)
                .get()
                .await()
            
            val base64Image = snapshot.getValue(String::class.java)
            if (base64Image != null) {
                Log.d(TAG, "✅ Imagem recuperada: $imageId (${base64Image.length} chars)")
                Result.success(base64Image)
            } else {
                Log.e(TAG, "❌ Imagem não encontrada: $imageId")
                Result.failure(Exception("Imagem não encontrada"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recuperar imagem: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Recupera a primeira imagem de uma planta
     */
    suspend fun getFirstPlantImage(plantId: String): Result<String> {
        val userId = FirebaseConfig.getCurrentUserId()
        val path = "usuarios/$userId/plantas/$plantId"
        
        return try {
            // PRIMEIRO: Verifica se o nó da planta existe
            val plantSnapshot = database.reference.child(path).get().await()
            if (!plantSnapshot.exists()) {
                return Result.failure(Exception("DEBUG: Planta não existe em $path"))
            }
            
            // SEGUNDO: Lista TODOS os filhos do nó da planta
            val childrenNames = plantSnapshot.children.map { it.key }.joinToString(", ")
            if (!childrenNames.contains("imagens")) {
                return Result.failure(Exception("DEBUG: Nó 'imagens' NÃO existe! Filhos: [$childrenNames]"))
            }
            
            // TERCEIRO: Agora busca na pasta /imagens
            val snapshot = database.reference
                .child(path)
                .child("imagens")
                .limitToFirst(1)
                .get()
                .await()
            
            // Debug via exception message para aparecer no ViewModel
            if (!snapshot.exists()) {
                return Result.failure(Exception("DEBUG: snapshot.exists()=false em $path/imagens"))
            }
            
            if (snapshot.childrenCount == 0L) {
                return Result.failure(Exception("DEBUG: snapshot.childrenCount=0 em $path/imagens"))
            }
            
            val firstChild = snapshot.children.first()
            val imageId = firstChild.key
            val base64Image = firstChild.getValue(String::class.java)
            
            if (base64Image == null) {
                return Result.failure(Exception("DEBUG: base64Image é NULL para imageId=$imageId"))
            }
            
            if (base64Image.isEmpty()) {
                return Result.failure(Exception("DEBUG: base64Image está VAZIO para imageId=$imageId"))
            }
            
            // Sucesso!
            Result.success(base64Image)
        } catch (e: Exception) {
            Result.failure(Exception("DEBUG: Exception capturada: ${e.javaClass.simpleName} - ${e.message}"))
        }
    }
    
    /**
     * Recupera a primeira imagem de um inseto
     */
    suspend fun getFirstInsectImage(insectId: String): Result<String> {
        val userId = FirebaseConfig.getCurrentUserId()
        val path = "usuarios/$userId/insetos/$insectId"
        
        return try {
            // PRIMEIRO: Verifica se o nó do inseto existe
            val insectSnapshot = database.reference.child(path).get().await()
            if (!insectSnapshot.exists()) {
                return Result.failure(Exception("DEBUG: Inseto não existe em $path"))
            }
            
            // SEGUNDO: Lista TODOS os filhos do nó do inseto
            val childrenNames = insectSnapshot.children.map { it.key }.joinToString(", ")
            if (!childrenNames.contains("imagens")) {
                return Result.failure(Exception("DEBUG: Nó 'imagens' NÃO existe! Filhos: [$childrenNames]"))
            }
            
            // TERCEIRO: Agora busca na pasta /imagens
            val snapshot = database.reference
                .child(path)
                .child("imagens")
                .limitToFirst(1)
                .get()
                .await()
            
            // Debug via exception message para aparecer no ViewModel
            if (!snapshot.exists()) {
                return Result.failure(Exception("DEBUG: snapshot.exists()=false em $path/imagens"))
            }
            
            if (snapshot.childrenCount == 0L) {
                return Result.failure(Exception("DEBUG: snapshot.childrenCount=0 em $path/imagens"))
            }
            
            val firstChild = snapshot.children.first()
            val imageId = firstChild.key
            val base64Image = firstChild.getValue(String::class.java)
            
            if (base64Image == null) {
                return Result.failure(Exception("DEBUG: base64Image é NULL para imageId=$imageId"))
            }
            
            if (base64Image.isEmpty()) {
                return Result.failure(Exception("DEBUG: base64Image está VAZIO para imageId=$imageId"))
            }
            
            // Sucesso!
            Result.success(base64Image)
        } catch (e: Exception) {
            Result.failure(Exception("DEBUG: Exception capturada: ${e.javaClass.simpleName} - ${e.message}"))
        }
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