package com.ifpr.androidapptemplate.ui.comentarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.model.*
import com.ifpr.androidapptemplate.data.repository.ComentariosRepository
import com.ifpr.androidapptemplate.data.repository.ComentariosSort
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar a interface de comentários
 * Controla estado, interações e dados dos comentários
 */
class ComentariosViewModel : ViewModel() {
    
    private val repository = ComentariosRepository()
    
    // Estados observáveis
    val loadingState: LiveData<LoadingState> = repository.loadingState
    val currentComments: LiveData<List<Comentario>> = repository.currentComments
    
    private val _postId = MutableLiveData<String>()
    val postId: LiveData<String> = _postId
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _sortBy = MutableLiveData<ComentariosSort>(ComentariosSort.MOST_RECENT)
    val sortBy: LiveData<ComentariosSort> = _sortBy
    
    private var currentPage = 1
    private var hasMorePages = true
    
    /**
     * Inicializa o ViewModel com uma postagem específica
     */
    fun initForPost(postId: String) {
        _postId.value = postId
        loadComments(postId, 1)
    }
    
    /**
     * Carrega comentários (paginação)
     */
    fun loadComments(postId: String, page: Int = 1) {
        if (page > 1 && !hasMorePages) return
        
        viewModelScope.launch {
            val result = repository.loadComments(postId, page, _sortBy.value ?: ComentariosSort.MOST_RECENT)
            
            result.onSuccess { comentariosResult ->
                hasMorePages = comentariosResult.hasNextPage
                currentPage = comentariosResult.currentPage
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
    
    /**
     * Carrega mais comentários (scroll infinito)
     */
    fun loadMoreComments() {
        val currentPostId = _postId.value ?: return
        if (hasMorePages) {
            loadComments(currentPostId, currentPage + 1)
        }
    }
    
    /**
     * Adiciona novo comentário
     */
    fun addComment(conteudo: String, parentId: String? = null, attachments: List<String> = emptyList()) {
        val currentPostId = _postId.value ?: return
        
        val novoComentario = NovoComentario(
            postId = currentPostId,
            parentId = parentId,
            conteudo = conteudo,
            attachments = attachments
        )
        
        viewModelScope.launch {
            val result = repository.addComment(novoComentario)
            
            result.onSuccess { comentario ->
                // Atualiza a lista de comentários localmente
                val currentList = repository.currentComments.value?.toMutableList() ?: mutableListOf()
                currentList.add(0, comentario) // Adiciona no início
                // Note: Em uma implementação real, precisaríamos atualizar o adapter
                
            }.onFailure { exception ->
                _errorMessage.value = "Erro ao adicionar comentário: ${exception.message}"
            }
        }
    }
    
    /**
     * Atualiza comentário existente
     */
    fun updateComment(comentarioId: String, novoConteudo: String, attachments: List<String> = emptyList()) {
        val atualizacao = AtualizacaoComentario(
            comentarioId = comentarioId,
            novoConteudo = novoConteudo,
            attachments = attachments
        )
        
        viewModelScope.launch {
            val result = repository.updateComment(atualizacao)
            
            result.onSuccess { comentario ->
                // Atualiza a lista de comentários localmente
                // Note: Em uma implementação real, precisaríamos atualizar o adapter
                
            }.onFailure { exception ->
                _errorMessage.value = "Erro ao atualizar comentário: ${exception.message}"
            }
        }
    }
    
    /**
     * Remove comentário
     */
    fun deleteComment(comentarioId: String) {
        val currentPostId = _postId.value ?: return
        
        viewModelScope.launch {
            val result = repository.deleteComment(comentarioId, currentPostId)
            
            result.onSuccess {
                // Remove da lista local
                // Note: Em uma implementação real, precisaríamos atualizar o adapter
                
            }.onFailure { exception ->
                _errorMessage.value = "Erro ao remover comentário: ${exception.message}"
            }
        }
    }
    
    /**
     * Curtir/descurtir comentário
     */
    fun toggleLike(comentarioId: String) {
        val currentPostId = _postId.value ?: return
        
        viewModelScope.launch {
            val result = repository.toggleLike(comentarioId, currentPostId)
            
            result.onSuccess { comentario ->
                // Atualiza a lista de comentários localmente
                // Note: Em uma implementação real, precisaríamos atualizar o adapter
                
            }.onFailure { exception ->
                _errorMessage.value = "Erro ao curtir comentário: ${exception.message}"
            }
        }
    }
    
    /**
     * Altera ordenação dos comentários
     */
    fun changeSortOrder(sort: ComentariosSort) {
        if (_sortBy.value != sort) {
            _sortBy.value = sort
            val currentPostId = _postId.value ?: return
            loadComments(currentPostId, 1) // Recarrega com nova ordenação
        }
    }
    
    /**
     * Verifica se pode carregar mais comentários
     */
    fun canLoadMore(): Boolean {
        return hasMorePages
    }
    
    /**
     * Limpa mensagens de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Limpa todos os caches
     */
    fun clearCache() {
        repository.clearAllCache()
    }
}