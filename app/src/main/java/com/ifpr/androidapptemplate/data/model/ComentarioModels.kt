package com.ifpr.androidapptemplate.data.model

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
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "Agora"
            minutes < 60 -> "${minutes}min"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val weeks = days / 7
                "${weeks}sem"
            }
        }
    }
    
    /**
     * Formata a data completa do comentário
     */
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Formata apenas a data (sem hora)
     */
    fun getFormattedDateOnly(): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}

/**
 * Informações do usuário para comentários (versão simplificada para performance)
 */
data class UsuarioComentario(
    val id: String = "",
    val nomeExibicao: String = "",
    val avatarUrl: String = "",
    val isVerificado: Boolean = false,
    val nivel: NivelUsuario = NivelUsuario.INICIANTE
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

/**
 * Dados mock para testes
 */
object ComentarioMockData {
    
    private val usuarios = listOf(
        UsuarioComentario(
            id = "user_001",
            nomeExibicao = "Maria Silva",
            avatarUrl = "https://example.com/avatar1.jpg",
            isVerificado = true,
            nivel = NivelUsuario.ESPECIALISTA
        ),
        UsuarioComentario(
            id = "user_002", 
            nomeExibicao = "João Santos",
            avatarUrl = "https://example.com/avatar2.jpg",
            isVerificado = false,
            nivel = NivelUsuario.INTERMEDIARIO
        ),
        UsuarioComentario(
            id = "user_003",
            nomeExibicao = "Ana Costa",
            avatarUrl = "https://example.com/avatar3.jpg", 
            isVerificado = true,
            nivel = NivelUsuario.AVANCADO
        )
    )
    
    fun gerarComentariosMock(postId: String, count: Int = 15): List<Comentario> {
        val comentarios = mutableListOf<Comentario>()
        val random = Random(123) // Seed fixo para consistência
        
        for (i in 1..count) {
            val usuario = usuarios[random.nextInt(usuarios.size)]
            val isReply = random.nextBoolean() && comentarios.isNotEmpty()
            val parentId = if (isReply) comentarios[random.nextInt(comentarios.size)].id else null
            val hasAttachments = random.nextInt(10) < 2 // 20% chance
            
            comentarios.add(
                Comentario(
                    id = "comment_${postId}_${i.toString().padStart(3, '0')}",
                    postId = postId,
                    parentId = parentId,
                    usuario = usuario,
                    conteudo = gerarConteudoMock(random),
                    timestamp = System.currentTimeMillis() - random.nextLong(0, 7 * 24 * 60 * 60 * 1000),
                    likes = random.nextInt(50),
                    likedByUser = random.nextBoolean(),
                    totalReplies = if (!isReply) random.nextInt(5) else 0,
                    isEdited = random.nextInt(10) < 1, // 10% chance
                    attachments = if (hasAttachments) listOf("https://example.com/image_${random.nextInt(10)}.jpg") else emptyList()
                )
            )
        }
        
        return comentarios.sortedBy { it.timestamp }
    }
    
    private fun gerarConteudoMock(random: Random): String {
        val conteudos = listOf(
            "Ótima observação! Eu também tenho notado isso no meu jardim.",
            "Que espécie interessante! Alguém sabe o nome científico?",
            "Obrigado por compartilhar! Vou tentar reproduzir essa técnica.",
            "Essa planta está linda! Como você conseguiu esse resultado?",
            "Cuidado com as pragas, elas podem se espalhar rapidamente.",
            "Excelente postagem! Muito informativa e bem fotografada.",
            "Tenho uma dúvida sobre o solo ideal para esse tipo de planta.",
            "Parabéns pelo trabalho! A comunidade agradece por compartilhar.",
            "Eu recomendo usar um fertilizante orgânico para melhores resultados.",
            "Essa espécie é nativa da região? Alguém sabe me dizer?"
        )
        
        return conteudos[random.nextInt(conteudos.size)]
    }
    
    fun gerarComentariosPaginados(postId: String, page: Int, pageSize: Int = 20): ComentariosResult {
        val todosComentarios = gerarComentariosMock(postId, 65)
        val totalItems = todosComentarios.size
        val totalPages = (totalItems + pageSize - 1) / pageSize
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalItems)
        
        val pageData = if (startIndex < totalItems) {
            todosComentarios.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return ComentariosResult(
            comentarios = pageData,
            currentPage = page,
            totalPages = totalPages,
            totalItems = totalItems,
            hasNextPage = page < totalPages,
            hasPreviousPage = page > 1
        )
    }
}