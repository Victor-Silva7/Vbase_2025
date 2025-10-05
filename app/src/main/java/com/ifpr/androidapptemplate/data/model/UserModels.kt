package com.ifpr.androidapptemplate.data.model

/**
 * Modelo de dados para usuário
 */
data class Usuario(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val dataRegistro: Long = System.currentTimeMillis(),
    val isVerificado: Boolean = false,
    val biografia: String = "",
    val localizacao: String = ""
) {
    fun getNomeExibicao(): String {
        return if (nome.isNotEmpty()) nome else "Usuário"
    }
}