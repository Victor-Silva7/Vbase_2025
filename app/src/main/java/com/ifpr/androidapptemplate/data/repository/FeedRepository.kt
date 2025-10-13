package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.delay

/**
 * Repository para gerenciar postagens com suporte a paginação
 */
class FeedRepository {
    
    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _currentPosts = MutableLiveData<List<PostagemFeed>>(emptyList())
    val currentPosts: LiveData<List<PostagemFeed>> = _currentPosts
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true
    private val pageSize = 10
    
    // Cache para diferentes filtros
    private val cacheMap = mutableMapOf<String, PaginatedResult<PostagemFeed>>()
    private var currentFilter: TipoFiltro = TipoFiltro.TODAS
    private var currentSearchQuery: String = ""
    
    /**
     * Carrega a primeira página (refresh)
     */
    suspend fun loadFirstPage(
        filtro: TipoFiltro = TipoFiltro.TODAS,
        searchQuery: String = ""
    ): Result<PaginatedResult<PostagemFeed>> {
        return try {
            _loadingState.postValue(LoadingState.Loading)
            
            // Reset pagination state
            currentPage = 1
            hasMorePages = true
            currentFilter = filtro
            currentSearchQuery = searchQuery
            
            // Simula delay de rede
            delay(1000)
            
            val result = PostagemMockData.gerarPostagensPaginadas(currentPage, pageSize)
            val filteredResult = applyFilters(result, filtro, searchQuery)
            
            // Atualiza estado
            _currentPosts.postValue(filteredResult.data)
            hasMorePages = filteredResult.hasNextPage
            _loadingState.postValue(LoadingState.Success(hasMorePages))
            
            // Cache do resultado
            val cacheKey = createCacheKey(filtro, searchQuery, currentPage)
            cacheMap[cacheKey] = filteredResult
            
            Result.success(filteredResult)
        } catch (e: Exception) {
            _loadingState.postValue(LoadingState.Error(e.message ?: "Erro desconhecido"))
            Result.failure(e)
        }
    }
    
    /**
     * Carrega próxima página (scroll infinito)
     */
    suspend fun loadNextPage(): Result<PaginatedResult<PostagemFeed>> {
        if (isLoading || !hasMorePages) {
            return Result.success(PaginatedResult(data = emptyList()))
        }
        
        return try {
            isLoading = true
            _loadingState.postValue(LoadingState.LoadingMore)
            
            // Incrementa página
            currentPage++
            
            // Verifica cache primeiro
            val cacheKey = createCacheKey(currentFilter, currentSearchQuery, currentPage)
            val cachedResult = cacheMap[cacheKey]
            
            val result = if (cachedResult != null) {
                cachedResult
            } else {
                // Simula delay de rede
                delay(800)
                
                val newResult = PostagemMockData.gerarPostagensPaginadas(currentPage, pageSize)
                val filteredResult = applyFilters(newResult, currentFilter, currentSearchQuery)
                
                // Cache do resultado
                cacheMap[cacheKey] = filteredResult
                filteredResult
            }
            
            // Combina com dados existentes
            val currentData = _currentPosts.value ?: emptyList()
            val combinedData = currentData + result.data
            
            _currentPosts.postValue(combinedData)
            hasMorePages = result.hasNextPage
            _loadingState.postValue(LoadingState.Success(hasMorePages))
            
            isLoading = false
            Result.success(result.copy(data = combinedData))
        } catch (e: Exception) {
            isLoading = false
            currentPage-- // Reverte página em caso de erro
            _loadingState.postValue(LoadingState.Error(e.message ?: \"Erro ao carregar mais postagens\"))
            Result.failure(e)
        }
    }
    
    /**
     * Atualiza uma postagem específica (para interações como curtidas)
     */
    fun updatePostagem(postagemAtualizada: PostagemFeed) {
        val currentList = _currentPosts.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == postagemAtualizada.id }
        
        if (index != -1) {
            currentList[index] = postagemAtualizada
            _currentPosts.postValue(currentList)
            
            // Atualiza também no cache
            updateCacheEntry(postagemAtualizada)
        }
    }
    
    /**
     * Aplica filtros aos resultados
     */
    private fun applyFilters(
        result: PaginatedResult<PostagemFeed>,
        filtro: TipoFiltro,
        searchQuery: String
    ): PaginatedResult<PostagemFeed> {
        var filteredData = result.data
        
        // Filtro por tipo
        when (filtro) {
            TipoFiltro.PLANTAS -> filteredData = filteredData.filter { it.tipo == TipoPostagem.PLANTA }
            TipoFiltro.INSETOS -> filteredData = filteredData.filter { it.tipo == TipoPostagem.INSETO }
            TipoFiltro.TODAS -> { /* Mantém todos */ }
        }
        
        // Filtro por busca de texto
        if (searchQuery.isNotEmpty()) {
            filteredData = filteredData.filter { postagem ->
                postagem.titulo.contains(searchQuery, ignoreCase = true) ||
                postagem.descricao.contains(searchQuery, ignoreCase = true) ||
                postagem.usuario.nomeExibicao.contains(searchQuery, ignoreCase = true) ||
                postagem.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                (postagem.detalhesPlanta?.nomeComum?.contains(searchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesPlanta?.nomeCientifico?.contains(searchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeComum?.contains(searchQuery, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeCientifico?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        
        return result.copy(
            data = filteredData,
            totalItems = filteredData.size
        )
    }
    
    /**
     * Cria chave de cache baseada nos filtros
     */
    private fun createCacheKey(filtro: TipoFiltro, searchQuery: String, page: Int): String {
        return \"${filtro.name}_${searchQuery.hashCode()}_$page\"
    }
    
    /**
     * Atualiza entrada específica no cache
     */
    private fun updateCacheEntry(postagemAtualizada: PostagemFeed) {
        cacheMap.forEach { (key, result) ->
            val updatedData = result.data.map { postagem ->
                if (postagem.id == postagemAtualizada.id) {
                    postagemAtualizada
                } else {
                    postagem
                }
            }
            cacheMap[key] = result.copy(data = updatedData)
        }
    }
    
    /**
     * Limpa cache (útil para refresh completo)
     */
    fun clearCache() {
        cacheMap.clear()
    }
    
    /**
     * Reset do estado de paginação
     */
    fun resetPagination() {
        currentPage = 1
        isLoading = false
        hasMorePages = true
        _currentPosts.postValue(emptyList())
        _loadingState.postValue(LoadingState.Idle)
    }
    
    /**
     * Verifica se pode carregar mais páginas
     */
    fun canLoadMore(): Boolean {
        return hasMorePages && !isLoading
    }
    
    /**
     * Obtém informações de paginação atuais
     */
    fun getPaginationInfo(): PaginationInfo {
        return PaginationInfo(
            currentPage = currentPage,
            hasMorePages = hasMorePages,
            isLoading = isLoading,
            totalItemsLoaded = _currentPosts.value?.size ?: 0
        )
    }
}

/**
 * Enum para tipos de filtro
 */
enum class TipoFiltro {
    TODAS, PLANTAS, INSETOS
}

/**
 * Informações sobre o estado da paginação
 */
data class PaginationInfo(
    val currentPage: Int,
    val hasMorePages: Boolean,
    val isLoading: Boolean,
    val totalItemsLoaded: Int
)