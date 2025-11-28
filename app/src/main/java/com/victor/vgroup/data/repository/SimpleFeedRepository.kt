package com.victor.vgroup.data.repository

import com.victor.vgroup.data.firebase.SimpleSocialService
import com.victor.vgroup.data.model.PostagemFeed
import com.victor.vgroup.data.model.TipoPostagem

/**
 * Repository SIMPLIFICADO para Feed
 * Gerencia postagens, curtidas e paginação
 */
class SimpleFeedRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleFeedRepository? = null
        
        fun getInstance(): SimpleFeedRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SimpleFeedRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val socialService = SimpleSocialService.getInstance()
    private val postagens = mutableListOf<PostagemFeed>()
    private var ultimaDataCarregada: Long? = null
    
    /**
     * Carregar feed inicial
     */
    suspend fun carregarFeed(limite: Int = 10): Result<List<PostagemFeed>> {
        return try {
            val result = socialService.buscarPostagensPaginadas(null, limite)
            
            result.onSuccess { lista ->
                postagens.clear()
                postagens.addAll(lista)
                ultimaDataCarregada = lista.lastOrNull()?.dataPostagem
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Carregar mais postagens (paginação)
     */
    suspend fun carregarMais(limite: Int = 10): Result<List<PostagemFeed>> {
        return try {
            val result = socialService.buscarPostagensPaginadas(ultimaDataCarregada, limite)
            
            result.onSuccess { lista ->
                postagens.addAll(lista)
                ultimaDataCarregada = lista.lastOrNull()?.dataPostagem
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Atualizar feed (pull to refresh)
     */
    suspend fun atualizarFeed(limite: Int = 10): Result<List<PostagemFeed>> {
        ultimaDataCarregada = null
        return carregarFeed(limite)
    }
    
    /**
     * Filtrar por tipo
     */
    fun filtrarPorTipo(tipo: TipoPostagem?): List<PostagemFeed> {
        return if (tipo == null) {
            postagens
        } else {
            postagens.filter { it.tipo == tipo }
        }
    }
    
    /**
     * Buscar por texto
     */
    fun buscarPorTexto(query: String): List<PostagemFeed> {
        if (query.isBlank()) return postagens
        
        val queryLower = query.lowercase()
        return postagens.filter { 
            it.titulo.lowercase().contains(queryLower) ||
            it.descricao.lowercase().contains(queryLower) ||
            it.usuario.nomeExibicao.lowercase().contains(queryLower)
        }
    }
    
    /**
     * Curtir/Descurtir postagem
     */
    suspend fun toggleCurtida(postagemId: String, userId: String): Result<Boolean> {
        return socialService.toggleCurtida(postagemId, userId)
    }
    
    /**
     * Obter postagens carregadas
     */
    fun getPostagens(): List<PostagemFeed> = postagens.toList()
}
