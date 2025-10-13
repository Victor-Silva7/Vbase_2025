package com.ifpr.androidapptemplate.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.model.*
import com.ifpr.androidapptemplate.data.repository.FeedRepository
import com.ifpr.androidapptemplate.data.repository.TipoFiltro
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar o feed com suporte a paginação
 */
class FeedViewModel : ViewModel() {
    
    private val repository = FeedRepository()
    
    // Estados observáveis
    val loadingState: LiveData<LoadingState> = repository.loadingState
    val currentPosts: LiveData<List<PostagemFeed>> = repository.currentPosts
    
    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Estado atual dos filtros
    private var currentFilter = TipoFiltro.TODAS
    private var currentSearchQuery = ""
    
    init {
        loadFirstPage()
    }
    
    /**
     * Carrega primeira página (inicial ou refresh)
     */
    fun loadFirstPage(
        filtro: TipoFiltro = TipoFiltro.TODAS,
        searchQuery: String = \"\"
    ) {
        currentFilter = filtro
        currentSearchQuery = searchQuery
        
        viewModelScope.launch {
            val result = repository.loadFirstPage(filtro, searchQuery)
            result.onFailure { exception ->
                _errorMessage.postValue(exception.message)
            }
        }
    }
    
    /**
     * Refresh do feed (pull-to-refresh)
     */
    fun refreshFeed() {
        _isRefreshing.postValue(true)
        
        viewModelScope.launch {
            repository.clearCache()
            val result = repository.loadFirstPage(currentFilter, currentSearchQuery)
            
            _isRefreshing.postValue(false)
            
            result.onFailure { exception ->
                _errorMessage.postValue(\"Erro ao atualizar: ${exception.message}\")
            }
        }
    }
    
    /**
     * Carrega próxima página (scroll infinito)
     */
    fun loadNextPage() {
        if (!repository.canLoadMore()) return
        
        viewModelScope.launch {
            val result = repository.loadNextPage()
            result.onFailure { exception ->
                _errorMessage.postValue(\"Erro ao carregar mais: ${exception.message}\")
            }
        }
    }
    
    /**
     * Aplica filtro de categoria
     */
    fun applyFilter(filtro: TipoFiltro) {
        if (currentFilter != filtro) {
            loadFirstPage(filtro, currentSearchQuery)
        }
    }
    
    /**
     * Aplica busca de texto
     */
    fun applySearch(query: String) {
        if (currentSearchQuery != query.trim()) {
            loadFirstPage(currentFilter, query.trim())
        }
    }
    
    /**
     * Limpa busca
     */
    fun clearSearch() {
        if (currentSearchQuery.isNotEmpty()) {
            loadFirstPage(currentFilter, \"\")
        }
    }
    
    /**
     * Atualiza uma postagem específica (para interações)
     */
    fun updatePostagem(postagem: PostagemFeed) {
        repository.updatePostagem(postagem)
    }
    
    /**
     * Curtir/descurtir postagem
     */
    fun toggleLike(postagem: PostagemFeed) {
        val novasInteracoes = postagem.interacoes.copy(
            curtidoPeloUsuario = !postagem.interacoes.curtidoPeloUsuario,
            curtidas = if (postagem.interacoes.curtidoPeloUsuario) {
                postagem.interacoes.curtidas - 1
            } else {
                postagem.interacoes.curtidas + 1
            }
        )
        
        val postagemAtualizada = postagem.copy(interacoes = novasInteracoes)
        updatePostagem(postagemAtualizada)
    }
    
    /**
     * Salvar/remover dos favoritos
     */
    fun toggleBookmark(postagem: PostagemFeed) {
        val novasInteracoes = postagem.interacoes.copy(
            salvosPeloUsuario = !postagem.interacoes.salvosPeloUsuario
        )
        
        val postagemAtualizada = postagem.copy(interacoes = novasInteracoes)
        updatePostagem(postagemAtualizada)
    }
    
    /**
     * Obtém informações de paginação atuais
     */
    fun getPaginationInfo(): PaginationInfo {
        return repository.getPaginationInfo()
    }
    
    /**
     * Verifica se pode carregar mais páginas
     */
    fun canLoadMore(): Boolean {
        return repository.canLoadMore()
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun clearError() {
        _errorMessage.postValue(null)
    }
}