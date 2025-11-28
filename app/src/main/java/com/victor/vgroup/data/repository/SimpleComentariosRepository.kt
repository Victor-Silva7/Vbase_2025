package com.victor.vgroup.data.repository

import com.victor.vgroup.data.firebase.SimpleSocialService

/**
 * Repository SIMPLIFICADO para Comentários
 */
class SimpleComentariosRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleComentariosRepository? = null
        
        fun getInstance(): SimpleComentariosRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SimpleComentariosRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val socialService = SimpleSocialService.getInstance()
    
    /**
     * Adicionar comentário
     */
    suspend fun adicionarComentario(
        postagemId: String,
        userId: String,
        userName: String,
        userAvatar: String,
        texto: String
    ): Result<String> {
        return socialService.adicionarComentario(postagemId, userId, userName, userAvatar, texto)
    }
    
    /**
     * Buscar comentários
     */
    suspend fun buscarComentarios(postagemId: String): Result<List<Map<String, Any?>>> {
        return socialService.buscarComentarios(postagemId)
    }
    
    /**
     * Contar comentários
     */
    suspend fun contarComentarios(postagemId: String): Result<Int> {
        return socialService.contarComentarios(postagemId)
    }
}
