package com.ifpr.androidapptemplate.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    
    private val _currentUser = MutableLiveData<Usuario>()
    val currentUser: LiveData<Usuario> = _currentUser
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    init {
        loadCurrentUser()
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                delay(1000)
                
                // Create mock user data
                val usuario = Usuario(
                    id = "user_123",
                    nome = "João Silva",
                    email = "joao.silva@example.com",
                    avatarUrl = "https://example.com/avatar.jpg",
                    biografia = "Apaixonado por jardinagem e fotografia",
                    localizacao = "São Paulo, SP",
                    telefone = "(11) 99999-9999",
                    website = "https://joaosilva.com",
                    nivel = com.ifpr.androidapptemplate.data.model.NivelUsuario.INTERMEDIARIO
                )
                
                _currentUser.value = usuario
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar perfil: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(
        nome: String, 
        telefone: String, 
        website: String, 
        biografia: String, 
        localizacao: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                delay(1500)
                
                // Validate inputs
                if (nome.isBlank()) {
                    _errorMessage.value = "Nome é obrigatório"
                    _isLoading.value = false
                    return@launch
                }
                
                // Update current user
                val updatedUser = _currentUser.value?.copy(
                    nome = nome,
                    telefone = telefone,
                    website = website,
                    biografia = biografia,
                    localizacao = localizacao
                ) ?: return@launch
                
                _currentUser.value = updatedUser
                _successMessage.value = "Perfil atualizado com sucesso!"
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao atualizar perfil: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate image upload delay
                delay(2000)
                
                // In a real app, you would upload the image to a server here
                // and get back a URL for the uploaded image
                
                // For now, we'll just simulate a successful upload
                val imageUrl = "https://example.com/uploaded_avatar_${System.currentTimeMillis()}.jpg"
                
                // Update user with new avatar URL
                val updatedUser = _currentUser.value?.copy(
                    avatarUrl = imageUrl
                ) ?: return@launch
                
                _currentUser.value = updatedUser
                _successMessage.value = "Foto de perfil atualizada!"
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao enviar foto: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSuccess() {
        _successMessage.value = null
    }
}