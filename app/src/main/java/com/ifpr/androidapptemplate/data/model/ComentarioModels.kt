package com.ifpr.androidapptemplate.data.model

import com.ifpr.androidapptemplate.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modelo de dados para comentários em postagens
 * Suporta comentários aninhados (respostas) e integração com usuários
 */
data class Comentario(
    val id: String = "",
    val postId: String = "",
    val parentId: String? = null, // null para comentários principais, ID do comentário pai para respostas
    val usuario: UsuarioComentario = UsuarioComentario(),
    val conteudo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedByUser: Boolean = false,
    val totalReplies: Int = 0,
    val isEdited: Boolean = false,
    val attachments: List<String> = emptyList() // URLs de imagens/anexos
) {
    /**
     * Verifica se é um comentário principal (não é resposta)
     */
    fun isRootComment(): Boolean = parentId == null
    
    /**
     * Verifica se tem respostas
     */
    fun hasReplies(): Boolean = totalReplies > 0
    
    /**
     * Formata o tempo relativo do comentário
     */
    fun getRelativeTime(): String {
        return TimeUtils.getRelativeTime(timestamp)
    }
    
    /**
     * Formata a data completa do comentário
     */
    fun getFormattedDate(): String {
        return TimeUtils.getFormattedDateTime(timestamp)
    }
    
    /**
     * Formata apenas a data (sem hora)
     */
    fun getFormattedDateOnly(): String {
        return TimeUtils.getFormattedDate(timestamp)
    }
}

/**
 * Informações do usuário para comentários (versão simplificada para performance)
 */
data class UsuarioComentario(
    val id: String = "",
    val nomeExibicao: String = "",
    val avatarUrl: String = "",
    val isVerificado: Boolean = false
)

/**
 * Modelo para nova postagem de comentário
 */
data class NovoComentario(
    val postId: String,
    val parentId: String? = null,
    val conteudo: String,
    val attachments: List<String> = emptyList()
)

/**
 * Modelo para atualização de comentário
 */
data class AtualizacaoComentario(
    val comentarioId: String,
    val novoConteudo: String,
    val attachments: List<String> = emptyList()
)

/**
 * Resultado paginado para comentários
 */
data class ComentariosResult(
    val comentarios: List<Comentario> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

/**
 * Estatísticas de comentários para uma postagem
 */
data class ComentarioStats(
    val totalComentarios: Int = 0,
    val totalReplies: Int = 0,
    val comentariosHoje: Int = 0,
    val usuariosAtivos: Int = 0
)

/**
 * Configuração para carregamento de comentários
 */
data class ComentariosConfig(
    val pageSize: Int = 20,
    val maxNestingLevel: Int = 3, // Profundidade máxima de respostas
    val enableReplies: Boolean = true,
    val enableLikes: Boolean = true
)

