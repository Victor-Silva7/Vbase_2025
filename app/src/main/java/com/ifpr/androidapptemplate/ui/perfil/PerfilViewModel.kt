package com.ifpr.androidapptemplate.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.model.Notificacao
import com.ifpr.androidapptemplate.data.model.TipoNotificacao
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilViewModel : ViewModel() {

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

    private val _notificacoes = MutableLiveData<List<Notificacao>>()
    val notificacoes: LiveData<List<Notificacao>> = _notificacoes

    // Função para carregar dados do usuário
    fun loadUserData() {
        _isLoading.value = true
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val nome = user.displayName ?: "Usuário"
            val email = user.email ?: ""
            _userName.value = nome
            _userEmail.value = email
            
            // Buscar estatísticas reais no Realtime DB
            loadUserStats(user.uid)
        } else {
            _userName.value = "Não autenticado"
            _userEmail.value = ""
            _isLoading.value = false
        }
    }
    
    private fun loadUserStats(userId: String) {
        viewModelScope.launch {
            try {
                val database = FirebaseConfig.getDatabase()
                val userRef = database.reference
                    .child("usuarios")
                    .child(userId)
                
                // Contar plantas
                val plantasSnapshot = userRef.child("plantas").get().await()
                val totalPlantas = plantasSnapshot.childrenCount.toInt()
                
                // Contar insetos
                val insetosSnapshot = userRef.child("insetos").get().await()
                val totalInsetos = insetosSnapshot.childrenCount.toInt()
                
                val totalRegistros = totalPlantas + totalInsetos
                
                _userStats.value = when {
                    totalRegistros == 0 -> "Nenhum registro ainda"
                    totalRegistros == 1 -> "1 registro realizado"
                    else -> "$totalRegistros registros realizados"
                }
                
                android.util.Log.wtf("PerfilViewModel", "✅ Carregadas estatísticas: $totalPlantas plantas, $totalInsetos insetos")
                
            } catch (e: Exception) {
                android.util.Log.wtf("PerfilViewModel", "❌ Erro ao carregar estatísticas: ${e.message}", e)
                _userStats.value = "Erro ao carregar estatísticas"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Função para atualizar perfil
    fun updateProfile(name: String, email: String) {
        // TODO: Implementar atualização do perfil
        _userName.value = name
        _userEmail.value = email
    }

    // Função para carregar notificações
    fun loadNotificacoes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val database = FirebaseConfig.getDatabase()
                val notificacoesList = mutableListOf<Notificacao>()
                
                android.util.Log.wtf("PerfilViewModel", "🔍 Iniciando busca de notificações para userId: $userId")
                
                // Buscar postagens do usuário
                val postagensSnapshot = database.reference
                    .child("postagens")
                    .orderByChild("usuario/id")
                    .equalTo(userId)
                    .get()
                    .await()
                
                android.util.Log.wtf("PerfilViewModel", "🔍 Postagens encontradas: ${postagensSnapshot.childrenCount}")
                
                for (postagemSnap in postagensSnapshot.children) {
                    val postagemId = postagemSnap.key ?: continue
                    val postagemTitulo = postagemSnap.child("titulo").value as? String ?: "postagem"
                    
                    android.util.Log.wtf("PerfilViewModel", "🔍 Processando postagem: $postagemTitulo (ID: $postagemId)")
                    
                    // Buscar curtidas nesta postagem
                    try {
                        val curtidasSnapshot = database.reference
                            .child("curtidas")
                            .child(postagemId)
                            .get()
                            .await()
                        
                        android.util.Log.wtf("PerfilViewModel", "✅ Curtidas carregadas: ${curtidasSnapshot.childrenCount}")
                        
                        for (curtidaSnap in curtidasSnapshot.children) {
                            val curtidaUserId = curtidaSnap.key ?: continue
                            if (curtidaUserId == userId) continue // Ignorar próprias curtidas
                            
                            val timestamp = curtidaSnap.value as? Long ?: System.currentTimeMillis()
                            
                            // Buscar nome do usuário de forma segura
                            val userName = try {
                                val userSnapshot = database.reference
                                    .child("usuarios")
                                    .child(curtidaUserId)
                                    .child("nome")
                                    .get()
                                    .await()
                                userSnapshot.value as? String ?: "Alguém"
                            } catch (e: Exception) {
                                android.util.Log.wtf("PerfilViewModel", "⚠️ Não foi possível buscar nome do usuário $curtidaUserId: ${e.message}")
                                "Alguém"
                            }
                            
                            notificacoesList.add(
                                Notificacao(
                                    id = "${postagemId}_${curtidaUserId}_curtida",
                                    tipo = TipoNotificacao.CURTIDA,
                                    userId = curtidaUserId,
                                    userName = userName,
                                    postagemId = postagemId,
                                    postagemTitulo = postagemTitulo,
                                    timestamp = timestamp
                                )
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.wtf("PerfilViewModel", "❌ Erro ao carregar curtidas: ${e.message}")
                    }
                    
                    // Buscar comentários nesta postagem
                    val comentariosSnapshot = database.reference
                        .child("comentarios")
                        .child(postagemId)
                        .get()
                        .await()
                    
                    android.util.Log.wtf("PerfilViewModel", "🔍 Comentários na postagem '$postagemTitulo' (ID: $postagemId): ${comentariosSnapshot.childrenCount}")
                    
                    for (comentarioSnap in comentariosSnapshot.children) {
                        val comentarioId = comentarioSnap.key ?: continue
                        val comentarioUserId = comentarioSnap.child("userId").value as? String ?: continue
                        
                        android.util.Log.wtf("PerfilViewModel", "🔍 Comentário encontrado: ID=$comentarioId, userId=$comentarioUserId, meuUserId=$userId")
                        
                        if (comentarioUserId == userId) {
                            android.util.Log.wtf("PerfilViewModel", "⏭️ Ignorando comentário próprio")
                            continue // Ignorar próprios comentários
                        }
                        
                        val timestamp = comentarioSnap.child("timestamp").value as? Long ?: System.currentTimeMillis()
                        val userName = comentarioSnap.child("userName").value as? String ?: "Alguém"
                        
                        android.util.Log.wtf("PerfilViewModel", "✅ Adicionando notificação de comentário de $userName")
                        
                        notificacoesList.add(
                            Notificacao(
                                id = "${postagemId}_${comentarioId}_comentario",
                                tipo = TipoNotificacao.COMENTARIO,
                                userId = comentarioUserId,
                                userName = userName,
                                postagemId = postagemId,
                                postagemTitulo = postagemTitulo,
                                timestamp = timestamp
                            )
                        )
                    }
                }
                
                // Ordenar por timestamp (mais recentes primeiro)
                val notificacoesOrdenadas = notificacoesList.sortedByDescending { it.timestamp }
                
                android.util.Log.wtf("PerfilViewModel", "📊 Total de notificações: ${notificacoesOrdenadas.size}")
                android.util.Log.wtf("PerfilViewModel", "📊 Curtidas: ${notificacoesOrdenadas.count { it.tipo == TipoNotificacao.CURTIDA }}")
                android.util.Log.wtf("PerfilViewModel", "📊 Comentários: ${notificacoesOrdenadas.count { it.tipo == TipoNotificacao.COMENTARIO }}")
                
                _notificacoes.value = notificacoesOrdenadas
                
                android.util.Log.wtf("PerfilViewModel", "✅ Carregadas ${notificacoesOrdenadas.size} notificações")
                
            } catch (e: Exception) {
                android.util.Log.wtf("PerfilViewModel", "❌ Erro ao carregar notificações: ${e.message}", e)
                _notificacoes.value = emptyList()
            }
        }
    }

    // Função para logout
    fun logout() {
        // TODO: Implementar logout
    }
}