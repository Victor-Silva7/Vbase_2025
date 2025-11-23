package com.ifpr.androidapptemplate.ui.registro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.ifpr.androidapptemplate.data.repository.RegistroRepository
import com.ifpr.androidapptemplate.data.repository.RegistrationStats
import com.ifpr.androidapptemplate.data.model.Planta
import com.ifpr.androidapptemplate.data.model.Inseto
import com.ifpr.androidapptemplate.data.model.PlantHealthCategory
import com.ifpr.androidapptemplate.data.model.InsectCategory
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * Enum para filtros de categoria
 */
enum class FiltroCategoria {
    TODOS,      // Exibir plantas e insetos
    PLANTAS,    // Exibir apenas plantas
    INSETOS     // Exibir apenas insetos
}

/**
 * ViewModel para gerenciar os dados da tela de registros pessoais
 */
class MeusRegistrosViewModel : ViewModel() {

    private val repository = RegistroRepository.getInstance()

    private val _registrationStats = MutableLiveData<RegistrationStats>()
    val registrationStats: LiveData<RegistrationStats> = _registrationStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _searchResults = MutableLiveData<SearchResults>()
    val searchResults: LiveData<SearchResults> = _searchResults

    // Search job for debouncing
    private var searchJob: Job? = null
    private val searchDebounceDelay = 300L // milliseconds

    // Expose repository LiveData for plants and insects
    val userPlants: LiveData<List<Planta>> = repository.userPlants
    val userInsects: LiveData<List<Inseto>> = repository.userInsects

    // Filtered data for search results
    val filteredPlants: LiveData<List<Planta>> = repository.filteredPlants
    val filteredInsects: LiveData<List<Inseto>> = repository.filteredInsects

    // Combined registrations for unified display
    private val _combinedRegistrations = MutableLiveData<List<RegistrationItem>>()
    val combinedRegistrations: LiveData<List<RegistrationItem>> = _combinedRegistrations

    // Filter by category
    private val _currentFilter = MutableLiveData<FiltroCategoria>(FiltroCategoria.TODOS)
    val currentFilter: LiveData<FiltroCategoria> = _currentFilter

    // Filtered combined registrations based on category filter
    private val _filteredCombinedRegistrations = MutableLiveData<List<RegistrationItem>>()
    val filteredCombinedRegistrations: LiveData<List<RegistrationItem>> = _filteredCombinedRegistrations

    init {
        // Start listening to real-time updates
        repository.startListeningToUserPlants()
        repository.startListeningToUserInsects()
        
        // Observe changes in data to update statistics
        userPlants.observeForever { updateStatistics(); updateCombinedRegistrations() }
        userInsects.observeForever { updateStatistics(); updateCombinedRegistrations() }
        filteredPlants.observeForever { updateCombinedRegistrations() }
        filteredInsects.observeForever { updateCombinedRegistrations() }
        
        // Observe filter changes to update combined list
        _currentFilter.observeForever { updateCombinedRegistrations() }
    }

    /**
     * Carrega os registros do usuário
     */
    fun loadRegistrations() {
        Log.d("MeusRegistrosVM", "🔄 loadRegistrations() iniciado")
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                Log.d("MeusRegistrosVM", "🔄 Chamando repository.getUserPlants...")
                // Force refresh of user data
                repository.getUserPlants(forceRefresh = true)
                
                Log.d("MeusRegistrosVM", "🔄 Chamando repository.getUserInsects...")
                repository.getUserInsects(forceRefresh = true)
                
                Log.d("MeusRegistrosVM", "🔄 Atualizando estatísticas...")
                // Update statistics
                updateStatistics()
                
                _isLoading.value = false
                Log.d("MeusRegistrosVM", "✅ loadRegistrations() completo")
                
            } catch (e: Exception) {
                Log.e("MeusRegistrosVM", "❌ Erro em loadRegistrations: ${e.message}", e)
                _isLoading.value = false
                _errorMessage.value = "Erro ao carregar registros: ${e.message}"
            }
        }
    }

    /**
     * Busca registros com base na query fornecida com debouncing
     */
    fun searchRegistrations(query: String) {
        _searchQuery.value = query
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            // Clear search and show all results
            repository.clearPlantFilter()
            repository.clearInsectFilter()
            _isSearching.value = false
            return
        }

        // Start new search with debouncing
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(searchDebounceDelay)
            
            try {
                // Apply real-time filtering
                repository.filterPlants(query)
                repository.filterInsects(query)
                
                // Perform full search for more comprehensive results
                val plantsResult = repository.searchUserPlants(query = query)
                val insectsResult = repository.searchUserInsects(query = query)
                
                val searchResults = SearchResults(
                    plants = plantsResult.getOrElse { emptyList() },
                    insects = insectsResult.getOrElse { emptyList() },
                    query = query,
                    totalResults = plantsResult.getOrElse { emptyList() }.size + 
                                 insectsResult.getOrElse { emptyList() }.size
                )
                
                _searchResults.value = searchResults
                _isSearching.value = false
                
            } catch (e: Exception) {
                _isSearching.value = false
                _errorMessage.value = "Erro na busca: ${e.message}"
            }
        }
    }
    
    /**
     * Search plants by category
     */
    fun searchPlantsByCategory(category: PlantHealthCategory?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.searchUserPlants(category = category)
                result.onSuccess { plantas ->
                    _searchResults.value = _searchResults.value?.copy(
                        plants = plantas
                    ) ?: SearchResults(plants = plantas)
                }.onFailure { exception ->
                    _errorMessage.value = "Erro ao filtrar plantas: ${exception.message}"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro inesperado: ${e.message}"
            }
        }
    }
    
    /**
     * Search insects by category
     */
    fun searchInsectsByCategory(category: InsectCategory?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.searchUserInsects(category = category)
                result.onSuccess { insetos ->
                    _searchResults.value = _searchResults.value?.copy(
                        insects = insetos
                    ) ?: SearchResults(insects = insetos)
                }.onFailure { exception ->
                    _errorMessage.value = "Erro ao filtrar insetos: ${exception.message}"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro inesperado: ${e.message}"
            }
        }
    }

    /**
     * Limpa a busca atual
     */
    fun clearSearch() {
        _searchQuery.value = ""
        loadRegistrations()
    }

    /**
     * Atualiza as estatísticas com base nos dados atuais
     */
    private fun updateStatistics() {
        viewModelScope.launch {
            try {
                val statsResult = repository.getRegistrationStats()
                statsResult.onSuccess { stats ->
                    _registrationStats.postValue(stats)
                }.onFailure { exception ->
                    _errorMessage.postValue("Erro ao calcular estatísticas: ${exception.message}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Atualiza a lista combinada de registros
     */
    private fun updateCombinedRegistrations() {
        val plants = userPlants.value ?: emptyList()
        val insects = userInsects.value ?: emptyList()
        val currentFilter = _currentFilter.value ?: FiltroCategoria.TODOS
        
        Log.d("MeusRegistrosVM", "Combinando registros: ${plants.size} plantas + ${insects.size} insetos, filtro: $currentFilter")
        
        val combinedList = mutableListOf<RegistrationItem>()
        
        // Add based on current filter
        when (currentFilter) {
            FiltroCategoria.TODOS -> {
                // Add plants
                plants.forEach { planta ->
                    combinedList.add(RegistrationItem.PlantItem(planta))
                }
                // Add insects
                insects.forEach { inseto ->
                    combinedList.add(RegistrationItem.InsectItem(inseto))
                }
            }
            FiltroCategoria.PLANTAS -> {
                // Add only plants
                plants.forEach { planta ->
                    combinedList.add(RegistrationItem.PlantItem(planta))
                }
            }
            FiltroCategoria.INSETOS -> {
                // Add only insects
                insects.forEach { inseto ->
                    combinedList.add(RegistrationItem.InsectItem(inseto))
                }
            }
        }
        
        // Sort by timestamp (most recent first)
        combinedList.sortByDescending { it.commonTimestamp }
        
        Log.d("MeusRegistrosVM", "Lista final de registros: ${combinedList.size}")
        
        _combinedRegistrations.postValue(combinedList)
        _filteredCombinedRegistrations.postValue(combinedList)
    }

    /**
     * Força atualização dos dados
     */
    fun refreshData() {
        loadRegistrations()
    }

    /**
     * Limpa mensagens de erro
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * Aplica filtro de categoria
     */
    fun applyFilter(filter: FiltroCategoria) {
        _currentFilter.value = filter
        updateCombinedRegistrations()
    }

    /**
     * Obter contagem por filtro
     */
    fun getFilterCounts(): Triple<Int, Int, Int> {
        val plants = userPlants.value?.size ?: 0
        val insects = userInsects.value?.size ?: 0
        val total = plants + insects
        return Triple(total, plants, insects)
    }

    override fun onCleared() {
        super.onCleared()
        // Stop listening to real-time updates
        repository.stopListeningToUserPlants()
        repository.stopListeningToUserInsects()
        // Cancel any ongoing search
        searchJob?.cancel()
    }
}

/**
 * Data class for search results
 */
data class SearchResults(
    val plants: List<Planta> = emptyList(),
    val insects: List<Inseto> = emptyList(),
    val query: String = "",
    val totalResults: Int = 0
) {
    fun isEmpty(): Boolean = plants.isEmpty() && insects.isEmpty()
    fun hasPlants(): Boolean = plants.isNotEmpty()
    fun hasInsects(): Boolean = insects.isNotEmpty()
}