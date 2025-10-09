package com.ifpr.androidapptemplate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _title = MutableLiveData<String>().apply {
        value = "Selecione seu Registro"
    }
    val title: LiveData<String> = _title

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _plantCount = MutableLiveData<String>().apply {
        value = "0"
    }
    val plantCount: LiveData<String> = _plantCount

    private val _insectCount = MutableLiveData<String>().apply {
        value = "0"
    }
    val insectCount: LiveData<String> = _insectCount

    // Função para carregar estatísticas do usuário
    fun loadUserStats() {
        _isLoading.value = true
        // TODO: Carregar estatísticas do Firebase
        // Por enquanto, valores estáticos
        _plantCount.value = "0"
        _insectCount.value = "0"
        _isLoading.value = false
    }

    // Função para navegar para registro de planta
    fun navigateToPlantRegistration() {
        // TODO: Implementar navegação para registro de planta
    }

    // Função para navegar para registro de inseto
    fun navigateToInsectRegistration() {
        // TODO: Implementar navegação para registro de inseto
    }

    // Função para navegar para registros do usuário
    fun navigateToUserRecords() {
        // TODO: Implementar navegação para registros do usuário
    }
}