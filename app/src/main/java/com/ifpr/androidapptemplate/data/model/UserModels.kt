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
    val localizacao: String = "",
    val telefone: String = "",
    val website: String = "",
    val nivel: NivelUsuario = NivelUsuario.INICIANTE
) {
    fun getNomeExibicao(): String {
        return if (nome.isNotEmpty()) nome else "Usuário"
    }
}

enum class NivelUsuario {
    INICIANTE, INTERMEDIARIO, AVANCADO, ESPECIALISTA
}