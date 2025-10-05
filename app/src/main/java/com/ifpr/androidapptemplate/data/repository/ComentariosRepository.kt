package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.delay

/**
 * Repository para gerenciar operações de comentários
 * Suporta comentários aninhados, paginação e interações sociais
 */
class ComentariosRepository {
    
    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _currentComments = MutableLiveData<List<Comentario>>(emptyList())
    val currentComments: LiveData<List<Comentario>> = _currentComments
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true
    private val pageSize = 20
    
    // Cache para diferentes postagens
    private val cacheMap = mutableMapOf<String, ComentariosResult>()
    
    /**
     * Carrega comentários de uma postagem (primeira página)
     */
    suspend fun loadComments(
        postId: String,
        page: Int = 1,
        sortBy: ComentariosSort = ComentariosSort.MOST_RECENT
    ): Result<ComentariosResult> {
        return try {
            if (page == 1) {
                _loadingState.postValue(LoadingState.Loading)
            } else {
                _loadingState.postValue(LoadingState.LoadingMore)
            }
            
            // Verifica cache primeiro
            val cacheKey = createCacheKey(postId, page, sortBy)
            val cachedResult = cacheMap[cacheKey]
            
            val result = if (cachedResult != null && !isCacheExpired(cachedResult)) {
                cachedResult
            } else {
                // Simula delay de rede
                delay(if (page == 1) 1000 else 800)
                
                val newResult = ComentarioMockData.gerarComentariosPaginados(postId, page, pageSize)
                val sortedResult = sortComments(newResult, sortBy)
                
                // Cache do resultado
                cacheMap[cacheKey] = sortedResult
                sortedResult
            }
            
            if (page == 1) {
                _currentComments.postValue(result.comentarios)
            }
            
            _loadingState.postValue(LoadingState.Success(result.hasNextPage))
            
            Result.success(result)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro desconhecido"))
            Result.failure(e)
        }
    }
    
    /**
     * Adiciona novo comentário
     */
    suspend fun addComment(novoComentario: NovoComentario): Result<Comentario> {
        return try {
            _loadingState.postValue(LoadingState.Loading)
            
            // Simula delay de rede
            delay(500)
            
            // Cria comentário com dados do usuário atual (mock)
            val comentario = Comentario(
                id = "comment_${System.currentTimeMillis()}",
                postId = novoComentario.postId,
                parentId = novoComentario.parentId,
                usuario = UsuarioComentario(
                    id = "current_user",
                    nomeExibicao = "Usuário Atual",
                    avatarUrl = "https://example.com/current_user.jpg",
                    isVerificado = false,
                    nivel = NivelUsuario.INICIANTE
                ),
                conteudo = novoComentario.conteudo,
                attachments = novoComentario.attachments,
                timestamp = System.currentTimeMillis()
            )
            
            // Atualiza cache
            invalidateCache(novoComentario.postId)
            
            _loadingState.postValue(LoadingState.Success(false))
            
            Result.success(comentario)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro ao adicionar comentário"))
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza comentário existente
     */
    suspend fun updateComment(atualizacao: AtualizacaoComentario): Result<Comentario> {
        return try {
            _loadingState.postValue(LoadingState.Loading)
            
            // Simula delay de rede
            delay(300)
            
            // Em uma implementação real, isso buscaria o comentário existente e atualizaria
            val comentarioAtualizado = Comentario(
                id = atualizacao.comentarioId,
                // ... outros campos atualizados
                conteudo = atualizacao.novoConteudo,
                isEdited = true
            )
            
            // Atualiza cache
            invalidateCacheForAllPosts()
            
            _loadingState.postValue(LoadingState.Success(false))
            
            Result.success(comentarioAtualizado)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro ao atualizar comentário"))
            Result.failure(e)
        }
    }
    
    /**
     * Remove comentário
     */
    suspend fun deleteComment(comentarioId: String, postId: String): Result<Unit> {
        return try {
            _loadingState.postValue(LoadingState.Loading)
            
            // Simula delay de rede
            delay(300)
            
            // Atualiza cache
            invalidateCache(postId)
            
            _loadingState.postValue(LoadingState.Success(false))
            
            Result.success(Unit)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro ao remover comentário"))
            Result.failure(e)
        }
    }
    
    /**
     * Curtir/descurtir comentário
     */
    suspend fun toggleLike(comentarioId: String, postId: String): Result<Comentario> {
        return try {
            _loadingState.postValue(LoadingState.Loading)
            
            // Simula delay de rede
            delay(200)
            
            // Em uma implementação real, isso buscaria o comentário e atualizaria o like
            val comentarioAtualizado = Comentario(
                id = comentarioId,
                // ... outros campos
                likes = 10, // valor mock
                likedByUser = true // valor mock
            )
            
            // Atualiza cache
            invalidateCache(postId)
            
            _loadingState.postValue(LoadingState.Success(false))
            
            Result.success(comentarioAtualizado)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro ao curtir comentário"))
            Result.failure(e)
        }
    }
    
    /**
     * Cria chave de cache
     */
    private fun createCacheKey(postId: String, page: Int, sortBy: ComentariosSort): String {
        return "${postId}_${page}_${sortBy.name}"
    }
    
    /**
     * Verifica se o cache expirou (5 minutos)
     */
    private fun isCacheExpired(result: ComentariosResult): Boolean {
        return System.currentTimeMillis() - result.comentarios.firstOrNull()?.timestamp ?: 0 > 5 * 60 * 1000
    }
    
    /**
     * Invalida cache para uma postagem específica
     */
    private fun invalidateCache(postId: String) {
        cacheMap.entries.removeIf { it.key.startsWith(postId) }
    }
    
    /**
     * Invalida cache para todas as postagens
     */
    private fun invalidateCacheForAllPosts() {
        cacheMap.clear()
    }
    
    /**
     * Ordena comentários
     */
    private fun sortComments(result: ComentariosResult, sortBy: ComentariosSort): ComentariosResult {
        val sortedComments = when (sortBy) {
            ComentariosSort.MOST_RECENT -> result.comentarios.sortedByDescending { it.timestamp }
            ComentariosSort.MOST_LIKED -> result.comentarios.sortedByDescending { it.likes }
            ComentariosSort.MOST_REPLIED -> result.comentarios.sortedByDescending { it.totalReplies }
        }
        
        return result.copy(comentarios = sortedComments)
    }
    
    /**
     * Limpa todos os caches
     */
    fun clearAllCache() {
        cacheMap.clear()
    }
    
    /**
     * Reset do estado de paginação
     */
    fun resetPagination() {
        currentPage = 1
        isLoading = false
        hasMorePages = true
        _currentComments.postValue(emptyList())
        _loadingState.postValue(LoadingState.Idle)
    }
}

/**
 * Opções de ordenação para comentários
 */
enum class ComentariosSort {
    MOST_RECENT,    // Mais recentes primeiro
    MOST_LIKED,     // Mais curtidos primeiro
    MOST_REPLIED    // Mais respondidos primeiro
}