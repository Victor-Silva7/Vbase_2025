package com.ifpr.androidapptemplate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * User data model for V Group - Manejo Verde
 * Represents a user profile with all associated data and preferences
 */
@Parcelize
data class Usuario(
    val id: String = "",
    val email: String = "",
    val nome: String = "",
    val nomeUsuario: String = "",
    val bio: String = "",
    val fotoPerfil: String = "",
    val telefone: String = "",
    val localizacao: String = "",
    val coordenadas: Coordenadas? = null,
    val dataNascimento: String = "",
    val dataRegistro: Long = System.currentTimeMillis(),
    val ultimoLogin: Long = 0L,
    val ativo: Boolean = true,
    val verificado: Boolean = false,
    val nivelExperiencia: NivelExperiencia = NivelExperiencia.INICIANTE,
    val especializacoes: List<String> = emptyList(),
    val interessesAreas: List<String> = emptyList(),
    val configuracoes: ConfiguracoesUsuario = ConfiguracoesUsuario(),
    val estatisticas: EstatisticasUsuario = EstatisticasUsuario(),
    val conquistas: List<Conquista> = emptyList(),
    val seguidores: List<String> = emptyList(),
    val seguindo: List<String> = emptyList(),
    val favoritosPublicos: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) : Parcelable {
    
    /**
     * Convert to Firebase-compatible map
     */
    fun toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "nome" to nome,
            "nomeUsuario" to nomeUsuario,
            "bio" to bio,
            "fotoPerfil" to fotoPerfil,
            "telefone" to telefone,
            "localizacao" to localizacao,
            "coordenadas" to coordenadas?.toMap(),
            "dataNascimento" to dataNascimento,
            "dataRegistro" to dataRegistro,
            "ultimoLogin" to ultimoLogin,
            "ativo" to ativo,
            "verificado" to verificado,
            "nivelExperiencia" to nivelExperiencia.name,
            "especializacoes" to especializacoes,
            "interessesAreas" to interessesAreas,
            "configuracoes" to configuracoes.toMap(),
            "estatisticas" to estatisticas.toMap(),
            "conquistas" to conquistas.map { it.toMap() },
            "seguidores" to seguidores,
            "seguindo" to seguindo,
            "favoritosPublicos" to favoritosPublicos,
            "tags" to tags
        )
    }
    
    /**
     * Get display name (prefer nome, fallback to nomeUsuario)
     */
    fun getDisplayName(): String {
        return nome.ifEmpty { nomeUsuario.ifEmpty { "Usuário" } }
    }
    
    /**
     * Get formatted registration date
     */
    fun getDataRegistroFormatada(): String {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(Date(dataRegistro))
        } catch (e: Exception) {
            "Data não disponível"
        }
    }
    
    /**
     * Get time since last login in days
     */
    fun getDaysSinceLastLogin(): Int {
        if (ultimoLogin == 0L) return -1
        val daysDiff = (System.currentTimeMillis() - ultimoLogin) / (1000 * 60 * 60 * 24)
        return daysDiff.toInt()
    }
    
    /**
     * Check if user is active recently (logged in within 30 days)
     */
    fun isActiveRecently(): Boolean {
        val daysSince = getDaysSinceLastLogin()
        return daysSince in 0..30
    }
    
    /**
     * Get total registrations count
     */
    fun getTotalRegistros(): Int {
        return estatisticas.totalPlantas + estatisticas.totalInsetos
    }
    
    /**
     * Check if user is following another user
     */
    fun isFollowing(userId: String): Boolean {
        return seguindo.contains(userId)
    }
    
    /**
     * Check if user is followed by another user
     */
    fun isFollowedBy(userId: String): Boolean {
        return seguidores.contains(userId)
    }
    
    /**
     * Get followers count
     */
    fun getSeguidoresCount(): Int = seguidores.size
    
    /**
     * Get following count
     */
    fun getSeguindoCount(): Int = seguindo.size
    
    /**
     * Check if user has expertise in area
     */
    fun hasExpertiseIn(area: String): Boolean {
        return especializacoes.any { it.equals(area, ignoreCase = true) }
    }
    
    /**
     * Get profile completion percentage
     */
    fun getProfileCompleteness(): Int {
        var completed = 0
        val total = 10
        
        if (nome.isNotEmpty()) completed++
        if (bio.isNotEmpty()) completed++
        if (fotoPerfil.isNotEmpty()) completed++
        if (localizacao.isNotEmpty()) completed++
        if (dataNascimento.isNotEmpty()) completed++
        if (especializacoes.isNotEmpty()) completed++
        if (interessesAreas.isNotEmpty()) completed++
        if (telefone.isNotEmpty()) completed++
        if (coordenadas != null) completed++
        if (verificado) completed++
        
        return (completed * 100) / total
    }
    
    companion object {
        /**
         * Create from Firebase map
         */
        fun fromFirebaseMap(map: Map<String, Any?>): Usuario {
            return Usuario(
                id = map["id"] as? String ?: "",
                email = map["email"] as? String ?: "",
                nome = map["nome"] as? String ?: "",
                nomeUsuario = map["nomeUsuario"] as? String ?: "",
                bio = map["bio"] as? String ?: "",
                fotoPerfil = map["fotoPerfil"] as? String ?: "",
                telefone = map["telefone"] as? String ?: "",
                localizacao = map["localizacao"] as? String ?: "",
                coordenadas = (map["coordenadas"] as? Map<String, Any?>)?.let { 
                    Coordenadas.fromMap(it) 
                },
                dataNascimento = map["dataNascimento"] as? String ?: "",
                dataRegistro = map["dataRegistro"] as? Long ?: System.currentTimeMillis(),
                ultimoLogin = map["ultimoLogin"] as? Long ?: 0L,
                ativo = map["ativo"] as? Boolean ?: true,
                verificado = map["verificado"] as? Boolean ?: false,
                nivelExperiencia = NivelExperiencia.valueOf(
                    map["nivelExperiencia"] as? String ?: NivelExperiencia.INICIANTE.name
                ),
                especializacoes = (map["especializacoes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                interessesAreas = (map["interessesAreas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                configuracoes = (map["configuracoes"] as? Map<String, Any?>)?.let { 
                    ConfiguracoesUsuario.fromMap(it) 
                } ?: ConfiguracoesUsuario(),
                estatisticas = (map["estatisticas"] as? Map<String, Any?>)?.let { 
                    EstatisticasUsuario.fromMap(it) 
                } ?: EstatisticasUsuario(),
                conquistas = (map["conquistas"] as? List<*>)?.mapNotNull { conquista ->
                    (conquista as? Map<String, Any?>)?.let { Conquista.fromMap(it) }
                } ?: emptyList(),
                seguidores = (map["seguidores"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                seguindo = (map["seguindo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                favoritosPublicos = (map["favoritosPublicos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}

/**
 * Experience levels for users
 */
enum class NivelExperiencia {
    INICIANTE,      // Beginner (0-10 registrations)
    INTERMEDIARIO,  // Intermediate (11-50 registrations)
    AVANCADO,       // Advanced (51-200 registrations)
    ESPECIALISTA,   // Expert (200+ registrations)
    PESQUISADOR     // Researcher (verified expert)
}

/**
 * User preferences and settings
 */
@Parcelize
data class ConfiguracoesUsuario(
    val notificacoesPush: Boolean = true,
    val notificacoesEmail: Boolean = true,
    val perfilPublico: Boolean = true,
    val localizacaoPublica: Boolean = false,
    val permitirMensagensPrivadas: Boolean = true,
    val mostrarEstatisticas: Boolean = true,
    val idioma: String = "pt-BR",
    val tema: String = "escuro",
    val qualidadeImagem: String = "alta",
    val backupAutomatico: Boolean = true,
    val sincronizacaoWiFi: Boolean = true,
    val modoCampo: Boolean = false
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "notificacoesPush" to notificacoesPush,
            "notificacoesEmail" to notificacoesEmail,
            "perfilPublico" to perfilPublico,
            "localizacaoPublica" to localizacaoPublica,
            "permitirMensagensPrivadas" to permitirMensagensPrivadas,
            "mostrarEstatisticas" to mostrarEstatisticas,
            "idioma" to idioma,
            "tema" to tema,
            "qualidadeImagem" to qualidadeImagem,
            "backupAutomatico" to backupAutomatico,
            "sincronizacaoWiFi" to sincronizacaoWiFi,
            "modoCampo" to modoCampo
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): ConfiguracoesUsuario {
            return ConfiguracoesUsuario(
                notificacoesPush = map["notificacoesPush"] as? Boolean ?: true,
                notificacoesEmail = map["notificacoesEmail"] as? Boolean ?: true,
                perfilPublico = map["perfilPublico"] as? Boolean ?: true,
                localizacaoPublica = map["localizacaoPublica"] as? Boolean ?: false,
                permitirMensagensPrivadas = map["permitirMensagensPrivadas"] as? Boolean ?: true,
                mostrarEstatisticas = map["mostrarEstatisticas"] as? Boolean ?: true,
                idioma = map["idioma"] as? String ?: "pt-BR",
                tema = map["tema"] as? String ?: "escuro",
                qualidadeImagem = map["qualidadeImagem"] as? String ?: "alta",
                backupAutomatico = map["backupAutomatico"] as? Boolean ?: true,
                sincronizacaoWiFi = map["sincronizacaoWiFi"] as? Boolean ?: true,
                modoCampo = map["modoCampo"] as? Boolean ?: false
            )
        }
    }
}

/**
 * User statistics and achievements
 */
@Parcelize
data class EstatisticasUsuario(
    val totalPlantas: Int = 0,
    val totalInsetos: Int = 0,
    val totalFotos: Int = 0,
    val totalCurtidas: Int = 0,
    val totalComentarios: Int = 0,
    val totalCompartilhamentos: Int = 0,
    val diasConsecutivos: Int = 0,
    val pontuacao: Int = 0,
    val nivel: Int = 1,
    val experiencia: Int = 0,
    val precisaoIdentificacao: Float = 0f,
    val contributionsValidadas: Int = 0,
    val distanciaPercorrida: Float = 0f,
    val locaisVisitados: Int = 0
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "totalPlantas" to totalPlantas,
            "totalInsetos" to totalInsetos,
            "totalFotos" to totalFotos,
            "totalCurtidas" to totalCurtidas,
            "totalComentarios" to totalComentarios,
            "totalCompartilhamentos" to totalCompartilhamentos,
            "diasConsecutivos" to diasConsecutivos,
            "pontuacao" to pontuacao,
            "nivel" to nivel,
            "experiencia" to experiencia,
            "precisaoIdentificacao" to precisaoIdentificacao,
            "contributionsValidadas" to contributionsValidadas,
            "distanciaPercorrida" to distanciaPercorrida,
            "locaisVisitados" to locaisVisitados
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): EstatisticasUsuario {
            return EstatisticasUsuario(
                totalPlantas = (map["totalPlantas"] as? Number)?.toInt() ?: 0,
                totalInsetos = (map["totalInsetos"] as? Number)?.toInt() ?: 0,
                totalFotos = (map["totalFotos"] as? Number)?.toInt() ?: 0,
                totalCurtidas = (map["totalCurtidas"] as? Number)?.toInt() ?: 0,
                totalComentarios = (map["totalComentarios"] as? Number)?.toInt() ?: 0,
                totalCompartilhamentos = (map["totalCompartilhamentos"] as? Number)?.toInt() ?: 0,
                diasConsecutivos = (map["diasConsecutivos"] as? Number)?.toInt() ?: 0,
                pontuacao = (map["pontuacao"] as? Number)?.toInt() ?: 0,
                nivel = (map["nivel"] as? Number)?.toInt() ?: 1,
                experiencia = (map["experiencia"] as? Number)?.toInt() ?: 0,
                precisaoIdentificacao = (map["precisaoIdentificacao"] as? Number)?.toFloat() ?: 0f,
                contributionsValidadas = (map["contributionsValidadas"] as? Number)?.toInt() ?: 0,
                distanciaPercorrida = (map["distanciaPercorrida"] as? Number)?.toFloat() ?: 0f,
                locaisVisitados = (map["locaisVisitados"] as? Number)?.toInt() ?: 0
            )
        }
    }
}

/**
 * User achievements and badges
 */
@Parcelize
data class Conquista(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val icone: String = "",
    val dataConquista: Long = 0L,
    val categoria: CategoriaConquista = CategoriaConquista.GERAL,
    val raridade: RaridadeConquista = RaridadeConquista.COMUM,
    val pontos: Int = 0,
    val progresso: Int = 0,
    val meta: Int = 0,
    val desbloqueada: Boolean = false
) : Parcelable {
    
    /**
     * Get progress percentage
     */
    fun getProgressoPercentual(): Int {
        return if (meta > 0) (progresso * 100) / meta else 0
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nome" to nome,
            "descricao" to descricao,
            "icone" to icone,
            "dataConquista" to dataConquista,
            "categoria" to categoria.name,
            "raridade" to raridade.name,
            "pontos" to pontos,
            "progresso" to progresso,
            "meta" to meta,
            "desbloqueada" to desbloqueada
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): Conquista {
            return Conquista(
                id = map["id"] as? String ?: "",
                nome = map["nome"] as? String ?: "",
                descricao = map["descricao"] as? String ?: "",
                icone = map["icone"] as? String ?: "",
                dataConquista = map["dataConquista"] as? Long ?: 0L,
                categoria = CategoriaConquista.valueOf(
                    map["categoria"] as? String ?: CategoriaConquista.GERAL.name
                ),
                raridade = RaridadeConquista.valueOf(
                    map["raridade"] as? String ?: RaridadeConquista.COMUM.name
                ),
                pontos = (map["pontos"] as? Number)?.toInt() ?: 0,
                progresso = (map["progresso"] as? Number)?.toInt() ?: 0,
                meta = (map["meta"] as? Number)?.toInt() ?: 0,
                desbloqueada = map["desbloqueada"] as? Boolean ?: false
            )
        }
    }
}

/**
 * Achievement categories
 */
enum class CategoriaConquista {
    GERAL,
    PLANTAS,
    INSETOS,
    FOTOGRAFIA,
    SOCIAL,
    EXPLORACAO,
    CONHECIMENTO,
    CONTRIBUICAO
}

/**
 * Achievement rarity levels
 */
enum class RaridadeConquista {
    COMUM,      // Common achievements
    RARO,       // Rare achievements
    EPICO,      // Epic achievements
    LENDARIO    // Legendary achievements
}