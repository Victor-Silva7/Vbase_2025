package com.ifpr.androidapptemplate.utils

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import kotlinx.coroutines.tasks.await

/**
 * Helper para migrar postagens antigas que têm apenas ID de imagem
 * em vez do Base64 completo
 */
object ImageMigrationHelper {
    private const val TAG = "ImageMigration"
    
    /**
     * Corrige uma postagem que tem apenas ID de imagem
     * Busca a imagem Base64 e atualiza a postagem
     */
    suspend fun fixPostagemImage(postagemId: String, tipo: String): Result<Unit> {
        return try {
            val database = FirebaseDatabase.getInstance()
            
            // Buscar a postagem atual
            val postagemRef = database.reference.child("postagens").child(postagemId)
            val snapshot = postagemRef.get().await()
            
            if (!snapshot.exists()) {
                Log.w(TAG, "Postagem não encontrada: $postagemId")
                return Result.failure(Exception("Postagem não encontrada"))
            }
            
            val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
            
            // Verificar se já tem o prefixo correto
            if (imageUrl.startsWith("data:image")) {
                Log.d(TAG, "✅ Postagem $postagemId já tem Base64 correto")
                return Result.success(Unit)
            }
            
            // Se é vazio, não fazer nada
            if (imageUrl.isEmpty()) {
                Log.d(TAG, "⚠️ Postagem $postagemId sem imagem")
                return Result.success(Unit)
            }
            
            // Se parece ser um UUID (ID de imagem), buscar o Base64
            if (imageUrl.matches(Regex("[0-9a-f-]{36}"))) {
                Log.d(TAG, "🔧 Corrigindo postagem $postagemId (imageUrl é ID: $imageUrl)")
                
                // Buscar userId da postagem
                val userId = snapshot.child("usuario").child("id").getValue(String::class.java)
                if (userId == null) {
                    Log.e(TAG, "❌ Postagem sem userId")
                    return Result.failure(Exception("Postagem sem userId"))
                }
                
                // Buscar a imagem Base64
                val imagePath = if (tipo == "PLANTA") {
                    "usuarios/$userId/plantas/$postagemId/imagens"
                } else {
                    "usuarios/$userId/insetos/$postagemId/imagens"
                }
                
                val imagesSnapshot = database.reference.child(imagePath).limitToFirst(1).get().await()
                
                if (imagesSnapshot.exists() && imagesSnapshot.childrenCount > 0) {
                    val firstChild = imagesSnapshot.children.first()
                    val base64Image = firstChild.getValue(String::class.java)
                    
                    if (base64Image != null && base64Image.isNotEmpty()) {
                        // Atualizar a postagem com Base64
                        postagemRef.child("imageUrl").setValue(base64Image).await()
                        Log.d(TAG, "✅ Postagem $postagemId corrigida! (${base64Image.length} chars)")
                        return Result.success(Unit)
                    }
                }
                
                Log.e(TAG, "❌ Não foi possível encontrar imagem para $postagemId")
                return Result.failure(Exception("Imagem não encontrada"))
            }
            
            Log.d(TAG, "⚠️ ImageUrl não reconhecido: $imageUrl")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao corrigir postagem: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Corrige todas as postagens do usuário atual
     */
    suspend fun fixAllUserPostagens(): Result<Int> {
        return try {
            val userId = FirebaseConfig.getCurrentUserId()
            if (userId == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }
            
            val database = FirebaseDatabase.getInstance()
            val postagensSnapshot = database.reference
                .child("postagens")
                .orderByChild("usuario/id")
                .equalTo(userId)
                .get()
                .await()
            
            var fixed = 0
            
            postagensSnapshot.children.forEach { snapshot ->
                val postagemId = snapshot.key ?: return@forEach
                val tipo = snapshot.child("tipo").getValue(String::class.java) ?: return@forEach
                
                val result = fixPostagemImage(postagemId, tipo)
                if (result.isSuccess) {
                    fixed++
                }
            }
            
            Log.d(TAG, "✅ Migração completa! $fixed postagens corrigidas")
            Result.success(fixed)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na migração: ${e.message}", e)
            Result.failure(e)
        }
    }
}
