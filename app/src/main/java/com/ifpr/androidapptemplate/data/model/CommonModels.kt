package com.ifpr.androidapptemplate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Common enums and data classes used across the application
 */

/**
 * Plant health categories
 */
enum class PlantHealthCategory {
    HEALTHY,
    SICK
}

/**
 * Insect categories
 */
enum class InsectCategory {
    BENEFICIAL,
    NEUTRAL,
    PEST
}

/**
 * Registration visibility
 */
enum class VisibilidadeRegistro {
    PUBLICO,
    PRIVADO
}

/**
 * Base interface for registrations
 */
interface BaseRegistration {
    val id: String
    val nome: String
    val nomePopular: String
    val nomeCientifico: String
    val data: String
    val dataTimestamp: Long
    val local: String
    val observacao: String
    val imagens: List<String>
    val userId: String
    val userName: String
    val timestamp: Long
    val timestampUltimaEdicao: Long
    val tipo: String
    val visibilidade: VisibilidadeRegistro
}

/**
 * Geographic coordinates for location tracking
 */
@Parcelize
data class Coordenadas(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val precisao: Float = 0f,
    val altitude: Double = 0.0,
    val endereco: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * Check if coordinates are valid
     */
    fun isValid(): Boolean {
        return latitude != 0.0 && longitude != 0.0
    }
    
    /**
     * Get distance to another coordinate in meters
     */
    fun distanceTo(other: Coordenadas): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            latitude, longitude,
            other.latitude, other.longitude,
            results
        )
        return results[0]
    }
    
    /**
     * Get formatted coordinates string
     */
    fun getFormattedString(): String {
        return "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "precisao" to precisao,
            "altitude" to altitude,
            "endereco" to endereco,
            "timestamp" to timestamp
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): Coordenadas {
            return Coordenadas(
                latitude = map["latitude"] as? Double ?: 0.0,
                longitude = map["longitude"] as? Double ?: 0.0,
                precisao = (map["precisao"] as? Number)?.toFloat() ?: 0f,
                altitude = map["altitude"] as? Double ?: 0.0,
                endereco = map["endereco"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Weather conditions during observation
 */
@Parcelize
data class ClimaObservacao(
    val temperatura: Float = 0f,
    val umidade: Int = 0,
    val condicao: CondicaoClimatica = CondicaoClimatica.DESCONHECIDO,
    val vento: String = "",
    val precipitacao: String = "",
    val pressaoAtmosferica: String = "",
    val visibilidade: String = "",
    val observacoes: String = ""
) : Parcelable {
    
    /**
     * Get temperature display string
     */
    fun getTemperaturaDisplay(): String {
        return if (temperatura > 0) "${temperatura}°C" else "N/A"
    }
    
    /**
     * Get humidity display string
     */
    fun getUmidadeDisplay(): String {
        return if (umidade > 0) "$umidade%" else "N/A"
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "temperatura" to temperatura,
            "umidade" to umidade,
            "condicao" to condicao.name,
            "vento" to vento,
            "precipitacao" to precipitacao,
            "pressaoAtmosferica" to pressaoAtmosferica,
            "visibilidade" to visibilidade,
            "observacoes" to observacoes
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): ClimaObservacao {
            return ClimaObservacao(
                temperatura = (map["temperatura"] as? Number)?.toFloat() ?: 0f,
                umidade = (map["umidade"] as? Number)?.toInt() ?: 0,
                condicao = CondicaoClimatica.valueOf(
                    map["condicao"] as? String ?: CondicaoClimatica.DESCONHECIDO.name
                ),
                vento = map["vento"] as? String ?: "",
                precipitacao = map["precipitacao"] as? String ?: "",
                pressaoAtmosferica = map["pressaoAtmosferica"] as? String ?: "",
                visibilidade = map["visibilidade"] as? String ?: "",
                observacoes = map["observacoes"] as? String ?: ""
            )
        }
    }
}

/**
 * Weather conditions enum
 */
enum class CondicaoClimatica {
    ENSOLARADO,
    PARCIALMENTE_NUBLADO,
    NUBLADO,
    CHUVOSO,
    TEMPESTUOSO,
    NEBLINA,
    VENTO_FORTE,
    DESCONHECIDO
}

/**
 * Registration status for content moderation
 */
enum class StatusRegistro {
    ATIVO,          // Active and visible
    INATIVO,        // Temporarily hidden
    PENDENTE,       // Pending review
    REJEITADO,      // Rejected by moderation
    ARQUIVADO       // Archived by user
}

/**
 * Validation information for registrations
 */
@Parcelize
data class ValidacaoRegistro(
    val validado: Boolean = false,
    val validadoPor: String = "",
    val dataValidacao: Long = 0L,
    val nivelConfianca: NivelConfianca = NivelConfianca.NAO_VALIDADO,
    val comentariosValidacao: String = "",
    val fontesReferencia: List<String> = emptyList(),
    val especialistaResponsavel: String = ""
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "validado" to validado,
            "validadoPor" to validadoPor,
            "dataValidacao" to dataValidacao,
            "nivelConfianca" to nivelConfianca.name,
            "comentariosValidacao" to comentariosValidacao,
            "fontesReferencia" to fontesReferencia,
            "especialistaResponsavel" to especialistaResponsavel
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): ValidacaoRegistro {
            return ValidacaoRegistro(
                validado = map["validado"] as? Boolean ?: false,
                validadoPor = map["validadoPor"] as? String ?: "",
                dataValidacao = map["dataValidacao"] as? Long ?: 0L,
                nivelConfianca = NivelConfianca.valueOf(
                    map["nivelConfianca"] as? String ?: NivelConfianca.NAO_VALIDADO.name
                ),
                comentariosValidacao = map["comentariosValidacao"] as? String ?: "",
                fontesReferencia = (map["fontesReferencia"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                especialistaResponsavel = map["especialistaResponsavel"] as? String ?: ""
            )
        }
    }
}

/**
 * Confidence levels for validation
 */
enum class NivelConfianca {
    NAO_VALIDADO,       // Not validated
    BAIXA_CONFIANCA,    // Low confidence
    MEDIA_CONFIANCA,    // Medium confidence
    ALTA_CONFIANCA,     // High confidence
    VALIDADO_ESPECIALISTA // Validated by expert
}

/**
 * Interaction statistics for social features
 */
@Parcelize
data class EstatisticasInteracao(
    val visualizacoes: Int = 0,
    val curtidas: Int = 0,
    val comentarios: Int = 0,
    val compartilhamentos: Int = 0,
    val favoritado: Int = 0,
    val denuncias: Int = 0,
    val pontuacaoQualidade: Float = 0f,
    val engajamento: Float = 0f
) : Parcelable {
    
    /**
     * Calculate engagement rate
     */
    fun calcularEngajamento(): Float {
        if (visualizacoes == 0) return 0f
        val interacoes = curtidas + comentarios + compartilhamentos + favoritado
        return (interacoes.toFloat() / visualizacoes.toFloat()) * 100
    }
    
    /**
     * Check if content is popular
     */
    fun isPopular(): Boolean {
        return curtidas >= 10 && engajamento >= 5.0f
    }
    
    /**
     * Check if content needs review (high reports)
     */
    fun needsReview(): Boolean {
        return denuncias >= 3 || (denuncias > 0 && visualizacoes > 0 && (denuncias.toFloat() / visualizacoes.toFloat()) > 0.1f)
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "visualizacoes" to visualizacoes,
            "curtidas" to curtidas,
            "comentarios" to comentarios,
            "compartilhamentos" to compartilhamentos,
            "favoritado" to favoritado,
            "denuncias" to denuncias,
            "pontuacaoQualidade" to pontuacaoQualidade,
            "engajamento" to engajamento
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): EstatisticasInteracao {
            return EstatisticasInteracao(
                visualizacoes = (map["visualizacoes"] as? Number)?.toInt() ?: 0,
                curtidas = (map["curtidas"] as? Number)?.toInt() ?: 0,
                comentarios = (map["comentarios"] as? Number)?.toInt() ?: 0,
                compartilhamentos = (map["compartilhamentos"] as? Number)?.toInt() ?: 0,
                favoritado = (map["favoritado"] as? Number)?.toInt() ?: 0,
                denuncias = (map["denuncias"] as? Number)?.toInt() ?: 0,
                pontuacaoQualidade = (map["pontuacaoQualidade"] as? Number)?.toFloat() ?: 0f,
                engajamento = (map["engajamento"] as? Number)?.toFloat() ?: 0f
            )
        }
    }
}

/**
 * Registration types enum for filtering and organization
 */
enum class TipoRegistro {
    PLANTA,
    INSETO,
    OBSERVACAO_GERAL,
    EVENTO_CLIMATICO,
    PRATICA_SUSTENTAVEL
}

/**
 * Base interface for all registrations
 */
interface RegistroBase {
    val id: String
    val nome: String
    val data: String
    val local: String
    val categoria: Any
    val observacao: String
    val imagens: List<String>
    val userId: String
    val timestamp: Long
    val tipo: String
    val status: StatusRegistro
    val visibilidade: VisibilidadeRegistro
    
    fun toFirebaseMap(): Map<String, Any?>
    fun getFormattedDate(): String
    fun getPrimaryImageUrl(): String
    fun hasImages(): Boolean
    fun isOwnedBy(currentUserId: String): Boolean
    fun isEditable(): Boolean
}