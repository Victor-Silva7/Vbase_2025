package com.ifpr.androidapptemplate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data model for Plant registration in V Group - Manejo Verde
 * Represents a plant observation with all associated data
 */
@Parcelize
data class Planta(
    val id: String = "",
    val nome: String = "",
    val nomePopular: String = "",
    val nomeCientifico: String = "",
    val data: String = "",
    val dataTimestamp: Long = 0L,
    val local: String = "",
    val coordenadas: Coordenadas? = null,
    val categoria: PlantHealthCategory = PlantHealthCategory.HEALTHY,
    val observacao: String = "",
    val imagens: List<String> = emptyList(),
    val thumbnailUrl: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val timestampUltimaEdicao: Long = 0L,
    val tipo: String = "PLANTA",
    val status: StatusRegistro = StatusRegistro.ATIVO,
    val visibilidade: VisibilidadeRegistro = VisibilidadeRegistro.PUBLICO,
    val tags: List<String> = emptyList(),
    val clima: ClimaObservacao? = null,
    val caracteristicas: CaracteristicasPlanta? = null,
    val validacao: ValidacaoRegistro? = null,
    val estatisticas: EstatisticasInteracao = EstatisticasInteracao()
) : Parcelable {
    
    /**
     * Convert to Firebase-compatible map
     */
    fun toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nome" to nome,
            "nomePopular" to nomePopular,
            "nomeCientifico" to nomeCientifico,
            "data" to data,
            "dataTimestamp" to dataTimestamp,
            "local" to local,
            "coordenadas" to coordenadas?.toMap(),
            "categoria" to categoria.name,
            "observacao" to observacao,
            "imagens" to imagens,
            "thumbnailUrl" to thumbnailUrl,
            "userId" to userId,
            "userName" to userName,
            "userProfileImage" to userProfileImage,
            "timestamp" to timestamp,
            "timestampUltimaEdicao" to timestampUltimaEdicao,
            "tipo" to tipo,
            "status" to status.name,
            "visibilidade" to visibilidade.name,
            "tags" to tags,
            "clima" to clima?.toMap(),
            "caracteristicas" to caracteristicas?.toMap(),
            "validacao" to validacao?.toMap(),
            "estatisticas" to estatisticas.toMap()
        )
    }
    
    /**
     * Get formatted date for display
     */
    fun getFormattedDate(): String {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (data.isNotEmpty()) data else formatter.format(Date(timestamp))
        } catch (e: Exception) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
    
    /**
     * Get primary image URL
     */
    fun getPrimaryImageUrl(): String {
        return thumbnailUrl.ifEmpty { imagens.firstOrNull() ?: "" }
    }
    
    /**
     * Check if plant has images
     */
    fun hasImages(): Boolean = imagens.isNotEmpty()
    
    /**
     * Get health status display text
     */
    fun getHealthStatusText(): String {
        return when (categoria) {
            PlantHealthCategory.HEALTHY -> "Saudável"
            PlantHealthCategory.SICK -> "Doente"
        }
    }
    
    /**
     * Check if plant belongs to current user
     */
    fun isOwnedBy(currentUserId: String): Boolean = userId == currentUserId
    
    /**
     * Check if plant is editable
     */
    fun isEditable(): Boolean = status == StatusRegistro.ATIVO
    
    /**
     * Get days since observation
     */
    fun getDaysSinceObservation(): Int {
        val observationDate = if (dataTimestamp > 0) dataTimestamp else timestamp
        val daysDiff = (System.currentTimeMillis() - observationDate) / (1000 * 60 * 60 * 24)
        return daysDiff.toInt()
    }
    
    companion object {
        /**
         * Create from Firebase map
         */
        fun fromFirebaseMap(map: Map<String, Any?>): Planta {
            return Planta(
                id = map["id"] as? String ?: "",
                nome = map["nome"] as? String ?: "",
                nomePopular = map["nomePopular"] as? String ?: "",
                nomeCientifico = map["nomeCientifico"] as? String ?: "",
                data = map["data"] as? String ?: "",
                dataTimestamp = map["dataTimestamp"] as? Long ?: 0L,
                local = map["local"] as? String ?: "",
                coordenadas = (map["coordenadas"] as? Map<String, Any?>)?.let { 
                    Coordenadas.fromMap(it) 
                },
                categoria = PlantHealthCategory.valueOf(
                    map["categoria"] as? String ?: PlantHealthCategory.HEALTHY.name
                ),
                observacao = map["observacao"] as? String ?: "",
                imagens = (map["imagens"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userProfileImage = map["userProfileImage"] as? String ?: "",
                timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis(),
                timestampUltimaEdicao = map["timestampUltimaEdicao"] as? Long ?: 0L,
                tipo = map["tipo"] as? String ?: "PLANTA",
                status = StatusRegistro.valueOf(
                    map["status"] as? String ?: StatusRegistro.ATIVO.name
                ),
                visibilidade = VisibilidadeRegistro.valueOf(
                    map["visibilidade"] as? String ?: VisibilidadeRegistro.PUBLICO.name
                ),
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                clima = (map["clima"] as? Map<String, Any?>)?.let { 
                    ClimaObservacao.fromMap(it) 
                },
                caracteristicas = (map["caracteristicas"] as? Map<String, Any?>)?.let { 
                    CaracteristicasPlanta.fromMap(it) 
                },
                validacao = (map["validacao"] as? Map<String, Any?>)?.let { 
                    ValidacaoRegistro.fromMap(it) 
                },
                estatisticas = (map["estatisticas"] as? Map<String, Any?>)?.let { 
                    EstatisticasInteracao.fromMap(it) 
                } ?: EstatisticasInteracao()
            )
        }
        
        /**
         * Generate unique ID for plant
         */
        fun generateId(): String {
            return "plant_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }
    }
}

/**
 * Plant health categories following project specifications
 */
enum class PlantHealthCategory {
    HEALTHY,    // Saudável
    SICK        // Doente
}

/**
 * Plant characteristics data class
 */
@Parcelize
data class CaracteristicasPlanta(
    val altura: String = "",
    val diametroTronco: String = "",
    val tipoFolha: String = "",
    val corFlor: String = "",
    val tempoFlorada: String = "",
    val tipoFruto: String = "",
    val habitat: String = "",
    val observacoesAdicionais: String = ""
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "altura" to altura,
            "diametroTronco" to diametroTronco,
            "tipoFolha" to tipoFolha,
            "corFlor" to corFlor,
            "tempoFlorada" to tempoFlorada,
            "tipoFruto" to tipoFruto,
            "habitat" to habitat,
            "observacoesAdicionais" to observacoesAdicionais
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): CaracteristicasPlanta {
            return CaracteristicasPlanta(
                altura = map["altura"] as? String ?: "",
                diametroTronco = map["diametroTronco"] as? String ?: "",
                tipoFolha = map["tipoFolha"] as? String ?: "",
                corFlor = map["corFlor"] as? String ?: "",
                tempoFlorada = map["tempoFlorada"] as? String ?: "",
                tipoFruto = map["tipoFruto"] as? String ?: "",
                habitat = map["habitat"] as? String ?: "",
                observacoesAdicionais = map["observacoesAdicionais"] as? String ?: ""
            )
        }
    }
}