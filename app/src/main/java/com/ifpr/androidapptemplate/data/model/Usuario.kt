package com.ifpr.androidapptemplate.data.model

import java.util.*

/**
 * Data class para representar um usuário do sistema
 */
data class Usuario(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val fotoPerfil: String = "",
    val dataCadastro: Long = System.currentTimeMillis(),
    val ultimoLogin: Long = System.currentTimeMillis(),
    val totalPlantas: Int = 0,
    val totalInsetos: Int = 0,
    val configuracoes: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * Cria objeto Usuario a partir de dados do Firebase
         */
        fun fromFirebaseMap(map: Map<String, Any?>): Usuario {
            return Usuario(
                id = map["id"] as? String ?: "",
                nome = map["nome"] as? String ?: "",
                email = map["email"] as? String ?: "",
                fotoPerfil = map["fotoPerfil"] as? String ?: "",
                dataCadastro = (map["dataCadastro"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                ultimoLogin = (map["ultimoLogin"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                totalPlantas = (map["totalPlantas"] as? Number)?.toInt() ?: 0,
                totalInsetos = (map["totalInsetos"] as? Number)?.toInt() ?: 0,
                configuracoes = (map["configuracoes"] as? Map<String, Any>) ?: emptyMap()
            )
        }
    }

    /**
     * Converte o objeto para um mapa para salvar no Firebase
     */
    fun toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nome" to nome,
            "email" to email,
            "fotoPerfil" to fotoPerfil,
            "dataCadastro" to dataCadastro,
            "ultimoLogin" to ultimoLogin,
            "totalPlantas" to totalPlantas,
            "totalInsetos" to totalInsetos,
            "configuracoes" to configuracoes
        )
    }

    /**
     * Obtém total de registros
     */
    fun getTotalRegistros(): Int = totalPlantas + totalInsetos

    /**
     * Verifica se é um usuário ativo
     */
    fun isActiveUser(): Boolean {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return ultimoLogin > oneWeekAgo
    }
}
