package com.victor.vgroup.data.model

data class Notificacao(
    val id: String = "",
    val tipo: TipoNotificacao = TipoNotificacao.CURTIDA,
    val userId: String = "",           // ID do usuário que gerou a notificação
    val userName: String = "",         // Nome do usuário que gerou a notificação
    val postagemId: String = "",       // ID da postagem relacionada
    val postagemTitulo: String = "",   // Título da postagem
    val timestamp: Long = 0L,
    val lida: Boolean = false
)

enum class TipoNotificacao {
    CURTIDA,
    COMENTARIO
}

fun Notificacao.getMensagem(): String {
    return when (tipo) {
        TipoNotificacao.CURTIDA -> "$userName curtiu sua postagem sobre $postagemTitulo"
        TipoNotificacao.COMENTARIO -> "$userName comentou em sua postagem sobre $postagemTitulo"
    }
}

fun Notificacao.getIconeResId(): Int {
    return when (tipo) {
        TipoNotificacao.CURTIDA -> com.victor.vgroup.R.drawable.ic_favorite_24dp
        TipoNotificacao.COMENTARIO -> com.victor.vgroup.R.drawable.ic_comment_24dp
    }
}

fun Notificacao.getTempoDecorrido(): String {
    val agora = System.currentTimeMillis()
    val diff = agora - timestamp
    
    val segundos = diff / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24
    
    return when {
        segundos < 60 -> "agora"
        minutos < 60 -> "há ${minutos}m"
        horas < 24 -> "há ${horas}h"
        dias < 7 -> "há ${dias}d"
        else -> "há ${dias / 7} semanas"
    }
}
