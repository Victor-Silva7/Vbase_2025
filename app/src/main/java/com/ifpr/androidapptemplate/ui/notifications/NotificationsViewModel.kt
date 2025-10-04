package com.ifpr.androidapptemplate.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>().apply {
        value = "Usuário V Group"
    }
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>().apply {
        value = "usuario@vgroup.com"
    }
    val userEmail: LiveData<String> = _userEmail

    private val _userStats = MutableLiveData<String>().apply {
        value = "0 registros realizados"
    }
    val userStats: LiveData<String> = _userStats

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    // Função para carregar dados do usuário
    fun loadUserData() {
        _isLoading.value = true
        // TODO: Carregar dados do Firebase
        _isLoading.value = false
    }

    // Função para atualizar perfil
    fun updateProfile(name: String, email: String) {
        // TODO: Implementar atualização do perfil
        _userName.value = name
        _userEmail.value = email
    }

    // Função para logout
    fun logout() {
        // TODO: Implementar logout
    }
}