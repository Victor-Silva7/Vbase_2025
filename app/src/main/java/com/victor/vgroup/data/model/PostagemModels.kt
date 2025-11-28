package com.victor.vgroup.data.model

import com.victor.vgroup.utils.TimeUtils

/**
 * Classes para gerenciamento de paginação
 */
data class PaginatedResult<T>(
    val data: List<T> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val pageSize: Int = 10,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isFirstPage(): Boolean = currentPage == 1
    fun isLastPage(): Boolean = currentPage >= totalPages
    
    companion object {
        fun <T> loading(currentPage: Int = 1): PaginatedResult<T> {
            return PaginatedResult(
                currentPage = currentPage,
                isLoading = true
            )
        }
        
        fun <T> error(error: String, currentPage: Int = 1): PaginatedResult<T> {
            return PaginatedResult(
                currentPage = currentPage,
                error = error,
                isLoading = false
            )
        }
        
        fun <T> success(
            data: List<T>,
            currentPage: Int,
            totalItems: Int,
            pageSize: Int = 10
        ): PaginatedResult<T> {
            val totalPages = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
            return PaginatedResult(
                data = data,
                currentPage = currentPage,
                totalPages = totalPages,
                totalItems = totalItems,
                hasNextPage = currentPage < totalPages,
                hasPreviousPage = currentPage > 1,
                pageSize = pageSize,
                isLoading = false
            )
        }
    }
}

/**
 * Estado de carregamento para scroll infinito
 */
sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object LoadingMore : LoadingState()
    data class Error(val message: String) : LoadingState()
    data class Success(val hasMore: Boolean) : LoadingState()
}

/**
 * Configuração de paginação
 */
data class PaginationConfig(
    val pageSize: Int = 10,
    val initialLoadSize: Int = 20,
    val prefetchDistance: Int = 5,
    val enablePlaceholders: Boolean = false
)

/**
 * Item de loading para o adapter
 */
data class LoadingItem(
    val id: String = "loading_${System.currentTimeMillis()}",
    val isVisible: Boolean = true
)

/**
 * Modelo de dados para postagens no feed
 * Inclui informações completas do usuário e do registro
 */
data class PostagemFeed(
    val id: String = "",
    val tipo: TipoPostagem = TipoPostagem.PLANTA,
    
    // Informações do usuário
    val usuario: UsuarioPostagem = UsuarioPostagem(),
    
    // Conteúdo da postagem
    val titulo: String = "",
    val descricao: String = "",
    val imageUrl: String = "",
    val localizacao: String = "",
    val dataPostagem: Long = System.currentTimeMillis(),
    
    // Informações específicas baseadas no tipo
    val detalhesPlanta: DetalhesPlanta? = null,
    val detalhesInseto: DetalhesInseto? = null,
    
    // Interações sociais
    val interacoes: InteracoesPostagem = InteracoesPostagem(),
    
    // Comentários
    val comentarioStats: ComentarioStats = ComentarioStats(),
    
    // Status e configurações
    val isPublico: Boolean = true,
    val isVerificado: Boolean = false,
    val tags: List<String> = emptyList()
) {
    fun getTempoPostagem(): String {
        return TimeUtils.getRelativeTime(dataPostagem)
    }
    
    fun getIconeRecurso(): String {
        return when (tipo) {
            TipoPostagem.PLANTA -> "ic_planta_24dp"
            TipoPostagem.INSETO -> "ic_inseto_24dp"
        }
    }
    
    /**
     * Converte para Map para salvar no Firebase
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "tipo" to tipo.name,
            "usuario" to mapOf(
                "id" to usuario.id,
                "nome" to usuario.nome,
                "nomeExibicao" to usuario.nomeExibicao,
                "avatarUrl" to usuario.avatarUrl,
                "isVerificado" to usuario.isVerificado,
                "localizacao" to usuario.localizacao,
                "biografia" to usuario.biografia,
                "dataRegistro" to usuario.dataRegistro,
                "totalRegistros" to usuario.totalRegistros,
                "totalCurtidas" to usuario.totalCurtidas
            ),
            "titulo" to titulo,
            "descricao" to descricao,
            "imageUrl" to imageUrl,
            "localizacao" to localizacao,
            "dataPostagem" to dataPostagem,
            "detalhesPlanta" to detalhesPlanta?.let {
                mapOf(
                    "nomeComum" to it.nomeComum,
                    "status" to it.status.name
                )
            },
            "detalhesInseto" to detalhesInseto?.let {
                mapOf(
                    "nomeComum" to it.nomeComum,
                    "tipo" to it.tipo.name
                )
            },
            "interacoes" to mapOf(
                "curtidas" to interacoes.curtidas,
                "comentarios" to interacoes.comentarios,
                "curtidoPeloUsuario" to interacoes.curtidoPeloUsuario,
                "ultimaInteracao" to interacoes.ultimaInteracao
            ),
            "comentarioStats" to mapOf(
                "totalComentarios" to comentarioStats.totalComentarios,
                "totalReplies" to comentarioStats.totalReplies,
                "comentariosHoje" to comentarioStats.comentariosHoje,
                "usuariosAtivos" to comentarioStats.usuariosAtivos
            ),
            "isPublico" to isPublico,
            "isVerificado" to isVerificado,
            "tags" to tags
        )
    }
    
    companion object {
        /**
         * Cria PostagemFeed a partir de um Map do Firebase
         */
        fun fromMap(map: Map<String, Any?>): PostagemFeed {
            @Suppress("UNCHECKED_CAST")
            val usuarioMap = map["usuario"] as? Map<String, Any?> ?: emptyMap()
            
            val usuario = UsuarioPostagem(
                id = usuarioMap["id"] as? String ?: "",
                nome = usuarioMap["nome"] as? String ?: "",
                nomeExibicao = usuarioMap["nomeExibicao"] as? String ?: "",
                avatarUrl = usuarioMap["avatarUrl"] as? String ?: "",
                isVerificado = usuarioMap["isVerificado"] as? Boolean ?: false,
                localizacao = usuarioMap["localizacao"] as? String ?: "",
                biografia = usuarioMap["biografia"] as? String ?: "",
                dataRegistro = (usuarioMap["dataRegistro"] as? Number)?.toLong() ?: 0L,
                totalRegistros = (usuarioMap["totalRegistros"] as? Number)?.toInt() ?: 0,
                totalCurtidas = (usuarioMap["totalCurtidas"] as? Number)?.toInt() ?: 0
            )
            
            @Suppress("UNCHECKED_CAST")
            val detalhesPlantaMap = map["detalhesPlanta"] as? Map<String, Any?>
            val detalhesPlanta = detalhesPlantaMap?.let {
                DetalhesPlanta(
                    nomeComum = it["nomeComum"] as? String ?: "",
                    status = try {
                        StatusPlanta.valueOf(it["status"] as? String ?: "SAUDAVEL")
                    } catch (e: Exception) {
                        StatusPlanta.SAUDAVEL
                    },

                )
            }
            
            @Suppress("UNCHECKED_CAST")
            val detalhesInsetoMap = map["detalhesInseto"] as? Map<String, Any?>
            val detalhesInseto = detalhesInsetoMap?.let {
                DetalhesInseto(
                    nomeComum = it["nomeComum"] as? String ?: "",
                    tipo = try {
                        TipoInseto.valueOf(it["tipo"] as? String ?: "NEUTRO")
                    } catch (e: Exception) {
                        TipoInseto.NEUTRO
                    },
                )
            }
            
            @Suppress("UNCHECKED_CAST")
            val interacoesMap = map["interacoes"] as? Map<String, Any?> ?: emptyMap()
            val interacoes = InteracoesPostagem(
                curtidas = (interacoesMap["curtidas"] as? Number)?.toInt() ?: 0,
                comentarios = (interacoesMap["comentarios"] as? Number)?.toInt() ?: 0,
                curtidoPeloUsuario = interacoesMap["curtidoPeloUsuario"] as? Boolean ?: false,
                ultimaInteracao = (interacoesMap["ultimaInteracao"] as? Number)?.toLong() ?: 0L
            )
            
            @Suppress("UNCHECKED_CAST")
            val comentarioStatsMap = map["comentarioStats"] as? Map<String, Any?> ?: emptyMap()
            val comentarioStats = ComentarioStats(
                totalComentarios = (comentarioStatsMap["totalComentarios"] as? Number)?.toInt() ?: 0,
                totalReplies = (comentarioStatsMap["totalReplies"] as? Number)?.toInt() ?: 0,
                comentariosHoje = (comentarioStatsMap["comentariosHoje"] as? Number)?.toInt() ?: 0,
                usuariosAtivos = (comentarioStatsMap["usuariosAtivos"] as? Number)?.toInt() ?: 0
            )
            
            return PostagemFeed(
                id = map["id"] as? String ?: "",
                tipo = try {
                    TipoPostagem.valueOf(map["tipo"] as? String ?: "PLANTA")
                } catch (e: Exception) {
                    TipoPostagem.PLANTA
                },
                usuario = usuario,
                titulo = map["titulo"] as? String ?: "",
                descricao = map["descricao"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                localizacao = map["localizacao"] as? String ?: "",
                dataPostagem = (map["dataPostagem"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                detalhesPlanta = detalhesPlanta,
                detalhesInseto = detalhesInseto,
                interacoes = interacoes,
                comentarioStats = comentarioStats,
                isPublico = map["isPublico"] as? Boolean ?: true,
                isVerificado = map["isVerificado"] as? Boolean ?: false,
                tags = (map["tags"] as? List<*>)?.mapNotNull { t -> t as? String } ?: emptyList()
            )
        }
    }
}

/**
 * Informações do usuário que fez a postagem
 */
data class UsuarioPostagem(
    val id: String = "",
    val nome: String = "",
    val nomeExibicao: String = "",
    val avatarUrl: String = "",
    val isVerificado: Boolean = false,
    val localizacao: String = "",
    val biografia: String = "",
    val dataRegistro: Long = 0L,
    val totalRegistros: Int = 0,
    val totalCurtidas: Int = 0
)

/**
 * Detalhes específicos para postagens de plantas
 */
data class DetalhesPlanta(
    val nomeComum: String = "",
    val status: StatusPlanta = StatusPlanta.SAUDAVEL,
) {
    fun getTextoStatus(): String {
        return when (status) {
            StatusPlanta.SAUDAVEL -> "Saudável"
            StatusPlanta.DOENTE -> "Doente"
        }
    }
    
    fun getCorStatus(): String {
        return when (status) {
            StatusPlanta.SAUDAVEL -> "#4CAF50"
            StatusPlanta.DOENTE -> "#F44336"
        }
    }
}

/**
 * Detalhes específicos para postagens de insetos
 */
data class DetalhesInseto(
    val nomeComum: String = "",
    val tipo: TipoInseto = TipoInseto.NEUTRO,
) {
    fun getTextoTipo(): String {
        return when (tipo) {
            TipoInseto.BENEFICO -> "Benéfico"
            TipoInseto.PRAGA -> "Praga"
            TipoInseto.NEUTRO -> "Neutro"
        }
    }
    
    fun getCorTipo(): String {
        return when (tipo) {
            TipoInseto.BENEFICO -> "#4CAF50"
            TipoInseto.PRAGA -> "#F44336"
            TipoInseto.NEUTRO -> "#757575"
        }
    }
}

/**
 * Interações sociais da postagem
 * Simplificado: apenas curtidas e comentários
 */
data class InteracoesPostagem(
    val curtidas: Int = 0,
    val comentarios: Int = 0,
    val curtidoPeloUsuario: Boolean = false,
    val ultimaInteracao: Long = 0L
) {
    fun getTextoInteracoes(): String {
        val partes = mutableListOf<String>()
        
        if (curtidas > 0) {
            partes.add("$curtidas curtida${if (curtidas > 1) "s" else ""}")
        }
        
        if (comentarios > 0) {
            partes.add("$comentarios comentário${if (comentarios > 1) "s" else ""}")
        }
        
        return when (partes.size) {
            0 -> "Seja o primeiro a curtir"
            1 -> partes[0]
            2 -> "${partes[0]} • ${partes[1]}"
            else -> partes.joinToString(" • ")
        }
    }
    
    fun getTotalInteracoes(): Int {
        return curtidas + comentarios
    }
}

/**
 * Enums para categorização
 */
enum class TipoPostagem {
    PLANTA, INSETO
}

enum class StatusPlanta {
    SAUDAVEL, DOENTE
}

enum class TipoInseto {
    BENEFICO, PRAGA, NEUTRO
}