package com.ifpr.androidapptemplate.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class NotificationsViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>().apply {
        value = "Usuário"
    }
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>().apply {
        value = ""
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
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val nome = user.displayName ?: "Usuário"
            val email = user.email ?: ""
            _userName.value = nome
            _userEmail.value = email
            // TODO: buscar estatísticas reais no Realtime DB
        } else {
            _userName.value = "Não autenticado"
            _userEmail.value = ""
        }
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