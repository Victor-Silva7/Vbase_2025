package com.ifpr.androidapptemplate.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.model.*
import com.ifpr.androidapptemplate.data.repository.TipoFiltro
import kotlinx.coroutines.*

/**
 * ViewModel para o feed de postagens
 * Gerencia estado de carregamento, postagens filtradas e interações do usuário
 */
class FeedViewModel : ViewModel() {
    
    // Estado de carregamento
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    // Postagens atuais
    private val _currentPosts = MutableLiveData<List<PostagemFeed>>()
    val currentPosts: LiveData<List<PostagemFeed>> = _currentPosts
    
    // Estado de refresh
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    
    // Mensagens de erro
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Filtros atuais
    private var currentFilter = TipoFiltro.TODAS
    private var currentSearchQuery = ""
    
    // Paginação
    private var currentPage = 0
    private val pageSize = 10
    private var hasMorePages = true
    
    // Debounce para busca
    private var searchJob: Job? = null
    private val searchDebounceDelay = 300L
    
    init {
        loadInitialFeed()
    }
    
    /**
     * Carrega o feed inicial
     */
    private fun loadInitialFeed() {
        _loadingState.value = LoadingState.Loading
        _isRefreshing.value = false
        
        // Em uma implementação completa, carregaria do Firebase
        // Por enquanto, usamos dados mock
        viewModelScope.launch {
            delay(1000) // Simula network delay
            
            try {
                val mockPosts = PostagemMockData.gerarPostagensMock()
                _currentPosts.value = mockPosts
                _loadingState.value = LoadingState.Success(true)
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar postagens: ${e.message}"
                _loadingState.value = LoadingState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Atualiza o feed (pull-to-refresh)
     */
    fun refreshFeed() {
        _isRefreshing.value = true
        
        viewModelScope.launch {
            delay(1000) // Simula network delay
            
            try {
                val mockPosts = PostagemMockData.gerarPostagensMock()
                _currentPosts.value = mockPosts
                _isRefreshing.value = false
                _loadingState.value = LoadingState.Success(true)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao atualizar feed: ${e.message}"
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Carrega a próxima página (scroll infinito)
     */
    fun loadNextPage() {
        if (!hasMorePages || _loadingState.value is LoadingState.LoadingMore) return
        
        _loadingState.value = LoadingState.LoadingMore
        
        viewModelScope.launch {
            delay(1000) // Simula network delay
            
            try {
                // Em uma implementação real, buscaria a próxima página do Firebase
                currentPage++
                
                // Se chegamos ao fim das páginas mock, não tem mais conteúdo
                if (currentPage > 3) {
                    hasMorePages = false
                    _loadingState.value = LoadingState.Success(false)
                    return@launch
                }
                
                val mockPosts = PostagemMockData.gerarPostagensMock()
                val currentList = _currentPosts.value ?: emptyList()
                val newList = currentList + mockPosts
                _currentPosts.value = newList
                _loadingState.value = LoadingState.Success(true)
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mais postagens: ${e.message}"
                _loadingState.value = LoadingState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Aplica filtro por categoria
     */
    fun applyFilter(filtro: TipoFiltro) {
        currentFilter = filtro
        filterAndSearchPosts()
    }
    
    /**
     * Aplica busca por texto
     */
    fun applySearch(query: String) {
        // Cancela busca anterior
        searchJob?.cancel()
        
        // Nova busca com debounce
        searchJob = viewModelScope.launch {
            delay(searchDebounceDelay)
            currentSearchQuery = query
            filterAndSearchPosts()
        }
    }
    
    /**
     * Filtra e busca postagens com base nos critérios atuais
     */
    private fun filterAndSearchPosts() {
        val allPosts = _currentPosts.value ?: return
        var filteredPosts = allPosts
        
        // Aplica filtro por categoria
        filteredPosts = when (currentFilter) {
            TipoFiltro.PLANTAS -> filteredPosts.filter { it.tipo == TipoPostagem.PLANTA }
            TipoFiltro.INSETOS -> filteredPosts.filter { it.tipo == TipoPostagem.INSETO }
            TipoFiltro.TODAS -> filteredPosts
        }
        
        // Aplica busca por texto
        if (currentSearchQuery.isNotEmpty()) {
            filteredPosts = filteredPosts.filter { postagem ->
                postagem.titulo.contains(currentSearchQuery, ignoreCase = true) ||
                postagem.descricao.contains(currentSearchQuery, ignoreCase = true) ||
                postagem.usuario.nomeExibicao.contains(currentSearchQuery, ignoreCase = true) ||
                postagem.tags.any { it.contains(currentSearchQuery, ignoreCase = true) } ||
                (postagem.detalhesPlanta?.nomeComum?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesPlanta?.nomeCientifico?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeComum?.contains(currentSearchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeCientifico?.contains(currentSearchQuery, ignoreCase = true) == true)
            }
        }
        
        _currentPosts.value = filteredPosts
    }
    
    /**
     * Alterna estado de curtida de uma postagem
     */
    fun toggleLike(postagem: PostagemFeed) {
        // Em uma implementação real, atualizaria no Firebase
        // Por enquanto, apenas simula a mudança local
        val currentList = _currentPosts.value ?: return
        val updatedList = currentList.map { p ->
            if (p.id == postagem.id) {
                val novasInteracoes = p.interacoes.copy(
                    curtidoPeloUsuario = !p.interacoes.curtidoPeloUsuario,
                    curtidas = if (p.interacoes.curtidoPeloUsuario) {
                        (p.interacoes.curtidas - 1).coerceAtLeast(0)
                    } else {
                        p.interacoes.curtidas + 1
                    }
                )
                p.copy(interacoes = novasInteracoes)
            } else {
                p
            }
        }
        _currentPosts.value = updatedList
    }
    
    /**
     * Alterna estado de salvamento de uma postagem
     */
    fun toggleBookmark(postagem: PostagemFeed) {
        // Em uma implementação real, atualizaria no Firebase
        // Por enquanto, apenas simula a mudança local
        val currentList = _currentPosts.value ?: return
        val updatedList = currentList.map { p ->
            if (p.id == postagem.id) {
                val novasInteracoes = p.interacoes.copy(
                    salvosPeloUsuario = !p.interacoes.salvosPeloUsuario
                )
                p.copy(interacoes = novasInteracoes)
            } else {
                p
            }
        }
        _currentPosts.value = updatedList
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}