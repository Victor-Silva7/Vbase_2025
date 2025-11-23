package com.ifpr.androidapptemplate.ui.postagens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.firebase.SimpleSocialService
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
    private val socialService = SimpleSocialService.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var postagenListener: ValueEventListener? = null

    init {
        loadPostagens()
    }

    /**
     * Carrega postagens em tempo real do Firebase
     */
    fun loadPostagens() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        
        postagenListener = databaseService.listenToAllPostagens { postagensList ->
            // Se há usuário logado, verificar curtidas
            if (userId != null) {
                viewModelScope.launch {
                    val postagensComCurtidas = postagensList.map { postagem ->
                        val curtidaResult = socialService.verificarCurtida(postagem.id, userId)
                        val curtiu = curtidaResult.getOrElse { false }
                        
                        postagem.copy(
                            interacoes = postagem.interacoes.copy(curtidoPeloUsuario = curtiu)
                        )
                    }
                    _postagens.value = postagensComCurtidas
                }
            } else {
                _postagens.value = postagensList
            }
            
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
        val userId = auth.currentUser?.uid ?: run {
            Log.wtf("PostagensVM", "❌ userId é nulo, usuário não autenticado")
            _errorMessage.value = "Você precisa estar logado para curtir"
            return
        }
        
        Log.wtf("PostagensVM", "🔵 likePostagem chamado: postagemId=$postagemId, userId=$userId")
        
        viewModelScope.launch {
            val currentList = _postagens.value ?: return@launch
            val postagemAtual = currentList.find { it.id == postagemId } ?: return@launch
            
            // Primeiro verificar o estado real no Firebase
            val verificacaoResult = socialService.verificarCurtida(postagemId, userId)
            val jaCurtiuNoFirebase = verificacaoResult.getOrElse { false }
            
            Log.wtf("PostagensVM", "🔵 Estado atual no Firebase: jaCurtiu=$jaCurtiuNoFirebase")
            Log.wtf("PostagensVM", "🔵 Estado atual na UI: curtidoPeloUsuario=${postagemAtual.interacoes.curtidoPeloUsuario}")
            
            // Sincronizar com Firebase
            Log.wtf("PostagensVM", "🔵 Sincronizando com Firebase...")
            val result = socialService.toggleCurtida(postagemId, userId)
            
            result.onSuccess { curtiu ->
                Log.wtf("PostagensVM", "✅ Sincronização bem-sucedida: curtiu=$curtiu")
                
                // Atualizar UI com o resultado real do Firebase
                val updatedList = currentList.map { postagem ->
                    if (postagem.id == postagemId) {
                        val curtidasAtuais = postagemAtual.interacoes.curtidas
                        val novasCurtidas = if (curtiu) curtidasAtuais + 1 else maxOf(0, curtidasAtuais - 1)
                        
                        val novasInteracoes = postagem.interacoes.copy(
                            curtidoPeloUsuario = curtiu,
                            curtidas = novasCurtidas
                        )
                        Log.wtf("PostagensVM", "🔵 UI atualizada: curtiu=$curtiu, curtidas=$novasCurtidas")
                        postagem.copy(interacoes = novasInteracoes)
                    } else {
                        postagem
                    }
                }
                _postagens.value = updatedList
            }
            
            result.onFailure { e ->
                Log.wtf("PostagensVM", "❌ Erro ao sincronizar: ${e.message}", e)
                _errorMessage.value = "Erro ao curtir: ${e.message}"
            }
        }
    }

    /**
     * Comentar em uma postagem
     */
    fun commentOnPostagem(postagemId: String, comment: String) {
        // TODO: Implementar sistema de comentários
        Log.d("PostagensVM", "Comentário em postagem $postagemId: $comment")
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