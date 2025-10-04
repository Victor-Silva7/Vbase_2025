package com.ifpr.androidapptemplate.ui.registro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.repository.RegistroRepository
import com.ifpr.androidapptemplate.data.repository.RegistrationStats
import com.ifpr.androidapptemplate.data.model.Planta
import com.ifpr.androidapptemplate.data.model.Inseto
import kotlinx.coroutines.launch

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

    // Expose repository LiveData for plants and insects
    val userPlants: LiveData<List<Planta>> = repository.userPlants
    val userInsects: LiveData<List<Inseto>> = repository.userInsects

    init {
        // Start listening to real-time updates
        repository.startListeningToUserPlants()
        repository.startListeningToUserInsects()
        
        // Observe changes in data to update statistics
        userPlants.observeForever { updateStatistics() }
        userInsects.observeForever { updateStatistics() }
    }

    /**
     * Carrega os registros do usuário
     */
    fun loadRegistrations() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Force refresh of user data
                repository.getUserPlants(forceRefresh = true)
                repository.getUserInsects(forceRefresh = true)
                
                // Update statistics
                updateStatistics()
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro ao carregar registros: ${e.message}"
            }
        }
    }

    /**
     * Busca registros com base na query fornecida
     */
    fun searchRegistrations(query: String) {
        _searchQuery.value = query
        
        if (query.isEmpty()) {
            // If query is empty, just reload normal data
            loadRegistrations()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Search both plants and insects
                // Note: The actual filtering will be done in the list fragments
                // This just triggers the search state
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro na busca: ${e.message}"
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

    override fun onCleared() {
        super.onCleared()
        // Stop listening to real-time updates
        repository.stopListeningToUserPlants()
        repository.stopListeningToUserInsects()
    }
}