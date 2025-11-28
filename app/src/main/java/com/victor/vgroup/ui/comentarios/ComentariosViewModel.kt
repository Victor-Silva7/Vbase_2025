package com.victor.vgroup.ui.comentarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.victor.vgroup.data.repository.SimpleComentariosRepository
import kotlinx.coroutines.launch

class ComentariosViewModel : ViewModel() {
    
    private val repository = SimpleComentariosRepository.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _comentarios = MutableLiveData<List<ComentarioSimples>>()
    val comentarios: LiveData<List<ComentarioSimples>> = _comentarios
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun carregarComentarios(postagemId: String) {
        _isLoading.value = true
        
        viewModelScope.launch {
            val result = repository.buscarComentarios(postagemId)
            
            result.onSuccess { lista ->
                val comentarios = lista.map { map ->
                    ComentarioSimples(
                        id = map["id"] as? String ?: "",
                        userId = map["userId"] as? String ?: "",
                        userName = map["userName"] as? String ?: "Usuario",
                        userAvatar = map["userAvatar"] as? String ?: "",
                        conteudo = map["conteudo"] as? String ?: "",
                        timestamp = (map["timestamp"] as? Long) ?: 0L
                    )
                }
                _comentarios.value = comentarios
            }.onFailure { e ->
                _errorMessage.value = "Erro ao carregar comentarios: ${e.message}"
            }
            
            _isLoading.value = false
        }
    }
    
    fun adicionarComentario(postagemId: String, texto: String) {
        val userId = auth.currentUser?.uid ?: run {
            _errorMessage.value = "Voce precisa estar logado para comentar"
            return
        }
        
        val userName = auth.currentUser?.displayName ?: "Usuario"
        val userAvatar = auth.currentUser?.photoUrl?.toString() ?: ""
        
        _isLoading.value = true
        
        viewModelScope.launch {
            val result = repository.adicionarComentario(
                postagemId = postagemId,
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                texto = texto
            )
            
            result.onSuccess {
                carregarComentarios(postagemId)
            }.onFailure { e ->
                _errorMessage.value = "Erro ao adicionar comentario: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}

data class ComentarioSimples(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val conteudo: String,
    val timestamp: Long
) {
    fun getTempoRelativo(): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}min"
            else -> "agora"
        }
    }
}
