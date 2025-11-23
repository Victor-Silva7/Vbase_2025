package com.ifpr.androidapptemplate.data.firebase

import com.google.firebase.database.*
import com.ifpr.androidapptemplate.data.model.PostagemFeed
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Serviço SIMPLIFICADO para rede social
 * Foco: Postagens, Curtidas e Comentários
 */
class SimpleSocialService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleSocialService? = null
        
        fun getInstance(): SimpleSocialService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SimpleSocialService().also { INSTANCE = it }
            }
        }
        
        // Paths do Firebase
        private const val POSTAGENS = "postagens"
        private const val CURTIDAS = "curtidas"
        private const val COMENTARIOS = "comentarios"
    }
    
    private val database = FirebaseConfig.getDatabase()
    
    // ============================================
    // POSTAGENS
    // ============================================
    
    /**
     * Salvar postagem no feed
     */
    suspend fun salvarPostagem(postagem: PostagemFeed): Result<String> {
        return try {
            database.reference
                .child(POSTAGENS)
                .child(postagem.id)
                .setValue(postagem.toMap())
                .await()
            
            Result.success(postagem.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Buscar todas postagens (ordenadas por data)
     */
    suspend fun buscarPostagens(): Result<List<PostagemFeed>> {
        return try {
            val snapshot = database.reference
                .child(POSTAGENS)
                .orderByChild("dataPostagem")
                .get()
                .await()
            
            val postagens = snapshot.children.mapNotNull { child ->
                try {
                    val data = child.value as? Map<String, Any?> ?: return@mapNotNull null
                    PostagemFeed.fromMap(data)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.dataPostagem }
            
            Result.success(postagens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Buscar postagens com paginação
     */
    suspend fun buscarPostagensPaginadas(
        ultimaData: Long? = null,
        limite: Int = 10
    ): Result<List<PostagemFeed>> {
        return try {
            val query = if (ultimaData != null) {
                database.reference
                    .child(POSTAGENS)
                    .orderByChild("dataPostagem")
                    .endBefore(ultimaData.toDouble())
                    .limitToLast(limite)
            } else {
                database.reference
                    .child(POSTAGENS)
                    .orderByChild("dataPostagem")
                    .limitToLast(limite)
            }
            
            val snapshot = query.get().await()
            
            val postagens = snapshot.children.mapNotNull { child ->
                try {
                    val data = child.value as? Map<String, Any?> ?: return@mapNotNull null
                    PostagemFeed.fromMap(data)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.dataPostagem }
            
            Result.success(postagens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // CURTIDAS
    // ============================================
    
    /**
     * Curtir/Descurtir postagem
     * Retorna true se curtiu, false se descurtiu
     */
    suspend fun toggleCurtida(postagemId: String, userId: String): Result<Boolean> {
        return try {
            android.util.Log.wtf("SimpleSocialService", "🔵 toggleCurtida: postagemId=$postagemId, userId=$userId")
            
            val curtidaRef = database.reference
                .child(CURTIDAS)
                .child(postagemId)
                .child(userId)
            
            val jaCurtiu = curtidaRef.get().await().exists()
            android.util.Log.wtf("SimpleSocialService", "🔵 jaCurtiu=$jaCurtiu")
            
            if (jaCurtiu) {
                // Descurtir
                android.util.Log.wtf("SimpleSocialService", "🔵 Descurtindo...")
                curtidaRef.removeValue().await()
                decrementarCurtidas(postagemId)
                android.util.Log.wtf("SimpleSocialService", "✅ Descurtida com sucesso")
                Result.success(false)
            } else {
                // Curtir
                android.util.Log.wtf("SimpleSocialService", "🔵 Curtindo...")
                curtidaRef.setValue(System.currentTimeMillis()).await()
                incrementarCurtidas(postagemId)
                android.util.Log.wtf("SimpleSocialService", "✅ Curtida com sucesso")
                Result.success(true)
            }
        } catch (e: Exception) {
            android.util.Log.wtf("SimpleSocialService", "❌ Erro ao curtir: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verificar se usuário curtiu postagem
     */
    suspend fun verificarCurtida(postagemId: String, userId: String): Result<Boolean> {
        return try {
            val curtiu = database.reference
                .child(CURTIDAS)
                .child(postagemId)
                .child(userId)
                .get()
                .await()
                .exists()
            
            Result.success(curtiu)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Contar curtidas de uma postagem
     */
    suspend fun contarCurtidas(postagemId: String): Result<Int> {
        return try {
            val count = database.reference
                .child(CURTIDAS)
                .child(postagemId)
                .get()
                .await()
                .childrenCount
                .toInt()
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // COMENTÁRIOS
    // ============================================
    
    /**
     * Adicionar comentário
     */
    suspend fun adicionarComentario(
        postagemId: String,
        userId: String,
        userName: String,
        userAvatar: String,
        texto: String
    ): Result<String> {
        return try {
            val comentarioRef = database.reference
                .child(COMENTARIOS)
                .child(postagemId)
                .push()
            
            val comentarioId = comentarioRef.key ?: throw Exception("Erro ao gerar ID")
            
            val comentario = mapOf(
                "id" to comentarioId,
                "userId" to userId,
                "userName" to userName,
                "userAvatar" to userAvatar,
                "conteudo" to texto,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0
            )
            
            comentarioRef.setValue(comentario).await()
            incrementarComentarios(postagemId)
            
            android.util.Log.wtf("SimpleSocialService", "✅ Comentário adicionado:")
            android.util.Log.wtf("SimpleSocialService", "   📝 PostagemID: $postagemId")
            android.util.Log.wtf("SimpleSocialService", "   📝 ComentarioID: $comentarioId")
            android.util.Log.wtf("SimpleSocialService", "   👤 UserId: $userId")
            android.util.Log.wtf("SimpleSocialService", "   👤 UserName: $userName")
            android.util.Log.wtf("SimpleSocialService", "   💬 Conteúdo: $texto")
            android.util.Log.wtf("SimpleSocialService", "   🕒 Timestamp: ${comentario["timestamp"]}")
            android.util.Log.wtf("SimpleSocialService", "   🔗 Caminho Firebase: comentarios/$postagemId/$comentarioId")
            
            Result.success(comentarioId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Buscar comentários de uma postagem
     */
    suspend fun buscarComentarios(postagemId: String): Result<List<Map<String, Any?>>> {
        return try {
            val snapshot = database.reference
                .child(COMENTARIOS)
                .child(postagemId)
                .orderByChild("timestamp")
                .get()
                .await()
            
            val comentarios = snapshot.children.mapNotNull { child ->
                child.value as? Map<String, Any?>
            }.reversed() // Mais recentes primeiro
            
            Result.success(comentarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Contar comentários de uma postagem
     */
    suspend fun contarComentarios(postagemId: String): Result<Int> {
        return try {
            val count = database.reference
                .child(COMENTARIOS)
                .child(postagemId)
                .get()
                .await()
                .childrenCount
                .toInt()
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // HELPERS PRIVADOS
    // ============================================
    
    private suspend fun incrementarCurtidas(postagemId: String) {
        suspendCoroutine<Unit> { continuation ->
            database.reference
                .child(POSTAGENS)
                .child(postagemId)
                .child("interacoes")
                .child("curtidas")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(data: MutableData): Transaction.Result {
                        val count = (data.value as? Long) ?: 0L
                        data.value = count + 1
                        return Transaction.success(data)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        continuation.resume(Unit)
                    }
                })
        }
    }
    
    private suspend fun decrementarCurtidas(postagemId: String) {
        suspendCoroutine<Unit> { continuation ->
            database.reference
                .child(POSTAGENS)
                .child(postagemId)
                .child("interacoes")
                .child("curtidas")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(data: MutableData): Transaction.Result {
                        val count = (data.value as? Long) ?: 0L
                        data.value = maxOf(0L, count - 1)
                        return Transaction.success(data)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        continuation.resume(Unit)
                    }
                })
        }
    }
    
    private suspend fun incrementarComentarios(postagemId: String) {
        suspendCoroutine<Unit> { continuation ->
            database.reference
                .child(POSTAGENS)
                .child(postagemId)
                .child("interacoes")
                .child("comentarios")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(data: MutableData): Transaction.Result {
                        val count = (data.value as? Long) ?: 0L
                        data.value = count + 1
                        return Transaction.success(data)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        continuation.resume(Unit)
                    }
                })
        }
    }
}
