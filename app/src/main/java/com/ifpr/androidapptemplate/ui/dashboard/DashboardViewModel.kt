package com.ifpr.androidapptemplate.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _title = MutableLiveData<String>().apply {
        value = "Postagens Recentes"
    }
    val title: LiveData<String> = _title

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // TODO: Adicionar lista de postagens
    // private val _postagens = MutableLiveData<List<PostagemModel>>()
    // val postagens: LiveData<List<PostagemModel>> = _postagens

    // Função para carregar postagens do Firebase
    fun loadPostagens() {
        _isLoading.value = true
        // TODO: Implementar carregamento de postagens do Firebase
        _isLoading.value = false
    }

    // Função para curtir uma postagem
    fun likePostagem(postagemId: String) {
        // TODO: Implementar like/unlike
    }

    // Função para comentar em uma postagem
    fun commentOnPostagem(postagemId: String, comment: String) {
        // TODO: Implementar sistema de comentários
    }
}