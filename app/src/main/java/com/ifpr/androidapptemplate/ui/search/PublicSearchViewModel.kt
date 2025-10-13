package com.ifpr.androidapptemplate.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.model.*
import com.ifpr.androidapptemplate.data.repository.PublicSearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para busca de registros públicos
 * Gerencia estado da busca, filtros e resultados
 */
class PublicSearchViewModel : ViewModel() {

    private val repository = PublicSearchRepository.getInstance()

    // Estados da busca
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> = _isSearching

    private val _searchResults = MutableLiveData<PublicSearchResult>()
    val searchResults: LiveData<PublicSearchResult> = _searchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Filtros aplicados
    private val _currentFilters = MutableLiveData<SearchFilters>(SearchFilters())
    val currentFilters: LiveData<SearchFilters> = _currentFilters

    private val _hasActiveFilters = MutableLiveData<Boolean>(false)
    val hasActiveFilters: LiveData<Boolean> = _hasActiveFilters

    // Sugestões e histórico
    private val _searchSuggestions = MutableLiveData<List<String>>()
    val searchSuggestions: LiveData<List<String>> = _searchSuggestions

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val _popularSearches = MutableLiveData<List<String>>()
    val popularSearches: LiveData<List<String>> = _popularSearches

    // Estado da interface
    private val _showSuggestions = MutableLiveData<Boolean>(false)
    val showSuggestions: LiveData<Boolean> = _showSuggestions

    private val _searchMode = MutableLiveData<SearchMode>(SearchMode.INITIAL)
    val searchMode: LiveData<SearchMode> = _searchMode

    // Job para controle de debouncing
    private var searchJob: Job? = null
    private var suggestionJob: Job? = null
    private val searchDebounceDelay = 500L
    private val suggestionDebounceDelay = 300L

    init {
        observeRepositoryData()
        loadInitialData()
    }

    /**
     * Observar dados do repository
     */
    private fun observeRepositoryData() {
        // Observar resultados de busca
        repository.searchResults.observeForever { result ->
            _searchResults.postValue(result)
            updateSearchMode()
        }

        // Observar estado de busca
        repository.isSearching.observeForever { isSearching ->
            _isSearching.postValue(isSearching)
        }

        // Observar sugestões
        repository.searchSuggestions.observeForever { suggestions ->
            _searchSuggestions.postValue(suggestions)
        }

        // Observar buscas recentes
        repository.recentSearches.observeForever { recent ->
            _recentSearches.postValue(recent)
        }

        // Observar buscas populares
        repository.popularSearches.observeForever { popular ->
            _popularSearches.postValue(popular)
        }
    }

    /**
     * Carregar dados iniciais
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                repository.getTrendingSearches()
                _searchMode.postValue(SearchMode.INITIAL)
            } catch (e: Exception) {
                _errorMessage.postValue("Erro ao carregar dados iniciais: ${e.message}")
            }
        }
    }

    /**
     * Executar busca principal
     */
    fun search(query: String) {
        if (query.trim().isEmpty()) {
            clearSearch()
            return
        }

        _searchQuery.value = query.trim()
        
        // Cancelar busca anterior
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            try {
                delay(searchDebounceDelay)
                
                val filters = _currentFilters.value ?: SearchFilters()
                val result = repository.searchPublicRecords(query.trim(), filters)
                
                result.onSuccess { searchResult ->
                    _searchResults.postValue(searchResult)
                    _searchMode.postValue(if (searchResult.isEmpty()) SearchMode.NO_RESULTS else SearchMode.RESULTS)
                }.onFailure { exception ->
                    _errorMessage.postValue("Erro na busca: ${exception.message}")
                    _searchMode.postValue(SearchMode.ERROR)
                }
                
            } catch (e: Exception) {
                _errorMessage.postValue("Erro inesperado: ${e.message}")
                _searchMode.postValue(SearchMode.ERROR)
            }
        }
    }

    /**
     * Buscar com sugestões em tempo real
     */
    fun searchWithSuggestions(query: String) {
        _searchQuery.value = query
        
        if (query.trim().isEmpty()) {
            _showSuggestions.value = false
            _searchMode.value = SearchMode.INITIAL
            return
        }

        if (query.length >= 2) {
            _showSuggestions.value = true
            _searchMode.value = SearchMode.SUGGESTIONS
            
            // Cancelar job anterior de sugestões
            suggestionJob?.cancel()
            
            suggestionJob = viewModelScope.launch {
                repository.searchWithDebouncing(query.trim(), suggestionDebounceDelay)
            }
        } else {
            _showSuggestions.value = false
        }
    }

    /**
     * Aplicar filtros
     */
    fun applyFilters(filters: SearchFilters) {
        _currentFilters.value = filters
        _hasActiveFilters.value = !filters.isEmpty()
        
        // Reexecutar busca se há uma query ativa
        _searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                search(query)
            }
        }
    }

    /**
     * Limpar filtros
     */
    fun clearFilters() {
        _currentFilters.value = SearchFilters()
        _hasActiveFilters.value = false
        
        // Reexecutar busca se há uma query ativa
        _searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                search(query)
            }
        }
    }

    /**
     * Limpar busca
     */
    fun clearSearch() {
        searchJob?.cancel()
        suggestionJob?.cancel()
        
        _searchQuery.value = ""
        _searchResults.value = PublicSearchResult()
        _searchSuggestions.value = emptyList()
        _showSuggestions.value = false
        _searchMode.value = SearchMode.INITIAL
        
        repository.clearSearchResults()
    }

    /**
     * Selecionar sugestão
     */
    fun selectSuggestion(suggestion: String) {
        _showSuggestions.value = false
        search(suggestion)
    }

    /**
     * Executar busca recente
     */
    fun searchRecent(recentQuery: String) {
        search(recentQuery)
    }

    /**
     * Limpar histórico de buscas
     */
    fun clearSearchHistory() {
        repository.clearSearchHistory()
    }

    /**
     * Atualizar modo de busca baseado nos resultados
     */
    private fun updateSearchMode() {
        val results = _searchResults.value
        val query = _searchQuery.value
        
        _searchMode.value = when {
            query.isNullOrEmpty() -> SearchMode.INITIAL
            _isSearching.value == true -> SearchMode.SEARCHING
            results?.isEmpty() == true -> SearchMode.NO_RESULTS
            results != null -> SearchMode.RESULTS
            else -> SearchMode.INITIAL
        }
    }

    /**
     * Obter contagem de resultados por tipo
     */
    fun getResultCounts(): Triple<Int, Int, Int> {
        val results = _searchResults.value ?: return Triple(0, 0, 0)
        return Triple(results.plants.size, results.insects.size, results.users.size)
    }

    /**
     * Filtrar resultados por tipo
     */
    fun filterResultsByType(type: SearchItemType) {
        val currentFilters = _currentFilters.value ?: SearchFilters()
        val newFilters = currentFilters.copy(type = type)
        applyFilters(newFilters)
    }

    /**
     * Ordenar resultados
     */
    fun sortResults(sortOrder: SortOrder) {
        val currentFilters = _currentFilters.value ?: SearchFilters()
        val newFilters = currentFilters.copy(sortBy = sortOrder)
        applyFilters(newFilters)
    }

    /**
     * Limpar mensagens de erro
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * Refresh dos dados
     */
    fun refresh() {
        _searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                search(query)
            }
        } ?: loadInitialData()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        suggestionJob?.cancel()
    }
}

/**
 * Modos da interface de busca
 */
enum class SearchMode {
    INITIAL,        // Estado inicial com buscas recentes e populares
    SUGGESTIONS,    // Mostrando sugestões em tempo real
    SEARCHING,      // Buscando (loading)
    RESULTS,        // Mostrando resultados
    NO_RESULTS,     // Sem resultados
    ERROR           // Erro na busca
}