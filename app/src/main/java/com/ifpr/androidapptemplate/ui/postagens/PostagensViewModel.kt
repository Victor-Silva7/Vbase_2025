package com.ifpr.androidapptemplate.ui.postagens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.model.PostagemFeed
import kotlinx.coroutines.launch

class PostagensViewModel : ViewModel() {

    private val _title = MutableLiveData<String>().apply {
        value = "Postagens Recentes"
    }
    val title: LiveData<String> = _title

    private val _postagens = MutableLiveData<List<PostagemFeed>>()
    val postagens: LiveData<List<PostagemFeed>> = _postagens

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val databaseService = FirebaseConfig.getDatabaseService()
    private var postagenListener: ValueEventListener? = null

    init {
        loadPostagens()
    }

    /**
     * Carrega postagens em tempo real do Firebase
     */
    fun loadPostagens() {
        _isLoading.value = true
        
        postagenListener = databaseService.listenToAllPostagens { postagensList ->
            _postagens.value = postagensList
            _isLoading.value = false
            
            if (postagensList.isEmpty()) {
                _errorMessage.value = "Nenhuma postagem disponível"
            } else {
                _errorMessage.value = ""
            }
        }
        
        if (postagenListener == null) {
            _isLoading.value = false
            _errorMessage.value = "Erro ao conectar ao servidor de postagens"
        }
    }

    /**
     * Curtir/descurtir uma postagem
     */
    fun likePostagem(postagemId: String) {
        // TODO: Implementar like/unlike
        Log.d("PostagensVM", "Like postagem: $postagemId")
    }

    /**
     * Comentar em uma postagem
     */
    fun commentOnPostagem(postagemId: String, comment: String) {
        // TODO: Implementar sistema de comentários
        Log.d("PostagensVM", "Comentário em postagem $postagemId: $comment")
    }

    /**
     * Compartilhar uma postagem
     */
    fun sharePostagem(postagemId: String) {
        // TODO: Implementar compartilhamento
        Log.d("PostagensVM", "Compartilhar postagem: $postagemId")
    }

    /**
     * Remover listener quando ViewModel for destruído
     */
    override fun onCleared() {
        super.onCleared()
        // Listener será removido automaticamente pelo Firebase
        postagenListener = null
    }
}