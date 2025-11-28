package com.victor.vgroup.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.victor.vgroup.data.model.*
import com.victor.vgroup.data.repository.SimpleFeedRepository
import kotlinx.coroutines.*

/**
 * ViewModel SIMPLIFICADO para o feed de postagens
 * Usa SimpleFeedRepository para gerenciar postagens, curtidas e paginação
 */
class FeedViewModel : ViewModel() {
    
    private val repository = SimpleFeedRepository.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
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
    private var currentFilter: TipoPostagem? = null
    private var currentSearchQuery = ""
    private var hasMorePages = true
    
    init {
        carregarFeed()
    }
    
    /**
     * Carrega o feed inicial
     */
    private fun carregarFeed() {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = repository.carregarFeed(limite = 20)
            
            result.onSuccess { postagens ->
                aplicarFiltros(postagens)
                hasMorePages = postagens.size >= 20
                _loadingState.value = LoadingState.Success(hasMorePages)
            }.onFailure { e ->
                _errorMessage.value = "Erro ao carregar feed: ${e.message}"
                _loadingState.value = LoadingState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Aplica filtros localmente
     */
    private fun aplicarFiltros(postagens: List<PostagemFeed>) {
        var resultado = postagens
        
        // Filtrar por tipo
        if (currentFilter != null) {
            resultado = repository.filtrarPorTipo(currentFilter)
        }
        
        // Filtrar por busca
        if (currentSearchQuery.isNotBlank()) {
            resultado = repository.buscarPorTexto(currentSearchQuery)
        }
        
        _currentPosts.value = resultado
    }
    
    /**
     * Atualiza o feed (pull-to-refresh)
     */
    fun refreshFeed() {
        _isRefreshing.value = true
        
        viewModelScope.launch {
            val result = repository.atualizarFeed(limite = 20)
            
            result.onSuccess { postagens ->
                aplicarFiltros(postagens)
                hasMorePages = postagens.size >= 20
                _isRefreshing.value = false
                _loadingState.value = LoadingState.Success(hasMorePages)
                _errorMessage.value = null
            }.onFailure { e ->
                _errorMessage.value = "Erro ao atualizar feed: ${e.message}"
                _isRefreshing.value = false
                _loadingState.value = LoadingState.Error(e.message ?: "Erro desconhecido")
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
            val result = repository.carregarMais(limite = 10)
            
            result.onSuccess { novasPostagens ->
                hasMorePages = novasPostagens.size >= 10
                aplicarFiltros(repository.getPostagens())
                _loadingState.value = LoadingState.Success(hasMorePages)
            }.onFailure { e ->
                _errorMessage.value = "Erro ao carregar mais: ${e.message}"
                _loadingState.value = LoadingState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Aplica filtro por tipo
     */
    fun applyFilter(tipo: TipoPostagem?) {
        currentFilter = tipo
        aplicarFiltros(repository.getPostagens())
    }
    
    /**
     * Aplica busca por texto
     */
    fun applySearch(query: String) {
        currentSearchQuery = query
        aplicarFiltros(repository.getPostagens())
    }
    
    /**
     * Curtir/Descurtir postagem
     */
    fun toggleLike(postagem: PostagemFeed) {
        val userId = auth.currentUser?.uid ?: run {
            android.util.Log.wtf("FeedViewModel", "❌ userId é nulo, usuário não autenticado")
            return
        }
        
        android.util.Log.wtf("FeedViewModel", "🔵 toggleLike chamado: postagemId=${postagem.id}, curtidoPeloUsuario=${postagem.interacoes.curtidoPeloUsuario}")
        
        viewModelScope.launch {
            // Atualização otimista na UI
            val currentList = _currentPosts.value ?: return@launch
            val updatedList = currentList.map { p ->
                if (p.id == postagem.id) {
                    val curtiu = !p.interacoes.curtidoPeloUsuario
                    val novasInteracoes = p.interacoes.copy(
                        curtidoPeloUsuario = curtiu,
                        curtidas = if (curtiu) p.interacoes.curtidas + 1 else maxOf(0, p.interacoes.curtidas - 1)
                    )
                    android.util.Log.wtf("FeedViewModel", "🔵 UI atualizada otimisticamente: curtiu=$curtiu, curtidas=${novasInteracoes.curtidas}")
                    p.copy(interacoes = novasInteracoes)
                } else {
                    p
                }
            }
            _currentPosts.value = updatedList
            
            // Sincronizar com Firebase
            android.util.Log.wtf("FeedViewModel", "🔵 Sincronizando com Firebase...")
            val result = repository.toggleCurtida(postagem.id, userId)
            result.onSuccess { curtiu ->
                android.util.Log.wtf("FeedViewModel", "✅ Sincronização bem-sucedida: curtiu=$curtiu")
            }
            result.onFailure { e ->
                // Reverter em caso de erro
                android.util.Log.wtf("FeedViewModel", "❌ Erro ao sincronizar: ${e.message}", e)
                _currentPosts.value = currentList
                _errorMessage.value = "Erro ao curtir: ${e.message}"
            }
        }
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun clearError() {
        _errorMessage.value = null
    }
}