package com.ifpr.androidapptemplate.ui.registro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistroViewModel : ViewModel() {

    private val _title = MutableLiveData<String>().apply {
        value = "Selecione seu Registro"
    }
    val title: LiveData<String> = _title

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    // Função para futuras operações de registro
    fun navigateToPlantRegistration() {
        // TODO: Implementar navegação para registro de planta
    }

    fun navigateToInsectRegistration() {
        // TODO: Implementar navegação para registro de inseto
    }

    fun navigateToUserRecords() {
        // TODO: Implementar navegação para registros do usuário
    }
}