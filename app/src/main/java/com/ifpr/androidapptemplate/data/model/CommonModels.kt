package com.ifpr.androidapptemplate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Common data models shared between Plants and Insects in V Group - Manejo Verde
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
 * Simplified to only likes and comments
 */
@Parcelize
data class EstatisticasInteracao(
    val curtidas: Int = 0,
    val comentarios: Int = 0
) : Parcelable {
    
    /**
     * Check if content is popular
     */
    fun isPopular(): Boolean {
        return curtidas >= 10
    }
    
    /**
     * Get total interactions
     */
    fun getTotalInteracoes(): Int {
        return curtidas + comentarios
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "curtidas" to curtidas,
            "comentarios" to comentarios
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): EstatisticasInteracao {
            return EstatisticasInteracao(
                curtidas = (map["curtidas"] as? Number)?.toInt() ?: 0,
                comentarios = (map["comentarios"] as? Number)?.toInt() ?: 0
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