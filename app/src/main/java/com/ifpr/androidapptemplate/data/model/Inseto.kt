package com.ifpr.androidapptemplate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data model for Insect registration in V Group - Manejo Verde
 * Represents an insect observation with all associated data
 */
@Parcelize
data class Inseto(
    override val id: String = generateId(),
    override val nome: String = "",
    override val nomePopular: String = "",
    override val nomeCientifico: String = "",
    override val data: String = "",
    override val dataTimestamp: Long = System.currentTimeMillis(),
    override val local: String = "",
    val coordenadas: Coordenadas? = null,
    val categoria: InsectCategory = InsectCategory.NEUTRAL,
    override val observacao: String = "",
    override val imagens: List<String> = emptyList(),
    val thumbnailUrl: String = "",
    override val userId: String = "",
    override val userName: String = "",
    val userProfileImage: String = "",
    override val timestamp: Long = System.currentTimeMillis(),
    override val timestampUltimaEdicao: Long = System.currentTimeMillis(),
    override val tipo: String = "INSETO",
    val status: StatusRegistro = StatusRegistro.ATIVO,
    override val visibilidade: VisibilidadeRegistro = VisibilidadeRegistro.PRIVADO,
    val tags: List<String> = emptyList(),
    val clima: ClimaObservacao? = null,
    val caracteristicas: CaracteristicasInseto? = null,
    val impactoObservado: ImpactoInseto? = null,
    val validacao: ValidacaoRegistro? = null,
    val estatisticas: EstatisticasInteracao = EstatisticasInteracao()
) : Parcelable, BaseRegistration {
    
    companion object {
        /**
         * Gera um ID único para o inseto
         */
        fun generateId(): String {
            return "insect_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        }

        /**
         * Cria objeto Inseto a partir de dados do Firebase
         */
        fun fromFirebaseMap(map: Map<String, Any?>): Inseto {
            return Inseto(
                id = map["id"] as? String ?: generateId(),
                nome = map["nome"] as? String ?: "",
                nomePopular = map["nomePopular"] as? String ?: "",
                nomeCientifico = map["nomeCientifico"] as? String ?: "",
                data = map["data"] as? String ?: "",
                dataTimestamp = (map["dataTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                local = map["local"] as? String ?: "",
                coordenadas = (map["coordenadas"] as? Map<String, Any?>)?.let { 
                    Coordenadas.fromMap(it) 
                },
                categoria = try {
                    InsectCategory.valueOf(map["categoria"] as? String ?: "NEUTRAL")
                } catch (e: Exception) {
                    InsectCategory.NEUTRAL
                },
                observacao = map["observacao"] as? String ?: "",
                imagens = (map["imagens"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userProfileImage = map["userProfileImage"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                timestampUltimaEdicao = (map["timestampUltimaEdicao"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                tipo = map["tipo"] as? String ?: "INSETO",
                status = StatusRegistro.valueOf(
                    map["status"] as? String ?: StatusRegistro.ATIVO.name
                ),
                visibilidade = try {
                    VisibilidadeRegistro.valueOf(map["visibilidade"] as? String ?: "PRIVADO")
                } catch (e: Exception) {
                    VisibilidadeRegistro.PRIVADO
                },
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                clima = (map["clima"] as? Map<String, Any?>)?.let { 
                    ClimaObservacao.fromMap(it) 
                },
                caracteristicas = (map["caracteristicas"] as? Map<String, Any?>)?.let { 
                    CaracteristicasInseto.fromMap(it) 
                },
                impactoObservado = (map["impactoObservado"] as? Map<String, Any?>)?.let { 
                    ImpactoInseto.fromMap(it) 
                },
                validacao = (map["validacao"] as? Map<String, Any?>)?.let { 
                    ValidacaoRegistro.fromMap(it) 
                },
                estatisticas = (map["estatisticas"] as? Map<String, Any?>)?.let { 
                    EstatisticasInteracao.fromMap(it) 
                } ?: EstatisticasInteracao()
            )
        }
    }

    /**
     * Converte o objeto para um mapa para salvar no Firebase
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
            "impactoObservado" to impactoObservado?.toMap(),
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
     * Check if insect has images
     */
    fun hasImages(): Boolean = imagens.isNotEmpty()

    /**
     * Obtém a primeira imagem disponível
     */
    fun getFirstImage(): String? = imagens.firstOrNull()

    /**
     * Verifica se tem múltiplas imagens
     */
    fun hasMultipleImages(): Boolean = imagens.size > 1

    /**
     * Obtém texto de status do inseto
     */
    fun getStatusText(): String {
        return when (categoria) {
            InsectCategory.BENEFICIAL -> "Benéfico"
            InsectCategory.NEUTRAL -> "Neutro"
            InsectCategory.PEST -> "Praga"
        }
    }
    
    /**
     * Get category description for tooltips
     */
    fun getCategoryDescription(): String {
        return when (categoria) {
            InsectCategory.BENEFICIAL -> "Polinizadores, predadores naturais de pragas, controladores biológicos"
            InsectCategory.NEUTRAL -> "Não causam danos significativos, parte do ecossistema natural"
            InsectCategory.PEST -> "Causam danos às plantas, reduzem produtividade, requerem controle"
        }
    }
    
    /**
     * Get category color for UI
     */
    fun getCategoryColor(): String {
        return when (categoria) {
            InsectCategory.BENEFICIAL -> "#4CAF50"  // Green
            InsectCategory.NEUTRAL -> "#FF9800"    // Orange
            InsectCategory.PEST -> "#F44336"       // Red
        }
    }

    /**
     * Obtém cor do status
     */
    fun getStatusColor(): String {
        return when (categoria) {
            InsectCategory.BENEFICIAL -> "#2196f3"
            InsectCategory.NEUTRAL -> "#9e9e9e"
            InsectCategory.PEST -> "#ff5722"
        }
    }

    /**
     * Obtém ícone do status
     */
    fun getStatusIcon(): String {
        return when (categoria) {
            InsectCategory.BENEFICIAL -> "ic_benefico_24dp"
            InsectCategory.NEUTRAL -> "ic_neutro_24dp"
            InsectCategory.PEST -> "ic_praga_24dp"
        }
    }
    
    /**
     * Check if insect belongs to current user
     */
    fun isOwnedBy(currentUserId: String): Boolean = userId == currentUserId
    
    /**
     * Check if insect is editable
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
    
    /**
     * Check if observation requires attention (pest category)
     */
    fun requiresAttention(): Boolean = categoria == InsectCategory.PEST

    /**
     * Verifica se o inseto é público
     */
    fun isPublic(): Boolean = visibilidade == VisibilidadeRegistro.PUBLICO

    /**
     * Obtém resumo do inseto para exibição
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        if (nomePopular.isNotEmpty()) parts.add(nomePopular)
        if (nomeCientifico.isNotEmpty()) parts.add("($nomeCientifico)")
        if (local.isNotEmpty()) parts.add("em $local")
        return parts.joinToString(" ")
    }

    /**
     * Verifica se é considerado uma praga
     */
    fun isPest(): Boolean = categoria == InsectCategory.PEST

    /**
     * Verifica se é benéfico
     */
    fun isBeneficial(): Boolean = categoria == InsectCategory.BENEFICIAL
}


/**
 * Insect characteristics data class
 */
@Parcelize
data class CaracteristicasInseto(
    val tamanho: String = "",
    val cor: String = "",
    val formato: String = "",
    val tipoAsa: String = "",
    val tipoAntena: String = "",
    val numeroSegmentos: String = "",
    val habitat: String = "",
    val comportamento: String = "",
    val estagioCicloVida: String = "",
    val observacoesAdicionais: String = ""
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "tamanho" to tamanho,
            "cor" to cor,
            "formato" to formato,
            "tipoAsa" to tipoAsa,
            "tipoAntena" to tipoAntena,
            "numeroSegmentos" to numeroSegmentos,
            "habitat" to habitat,
            "comportamento" to comportamento,
            "estagioCicloVida" to estagioCicloVida,
            "observacoesAdicionais" to observacoesAdicionais
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): CaracteristicasInseto {
            return CaracteristicasInseto(
                tamanho = map["tamanho"] as? String ?: "",
                cor = map["cor"] as? String ?: "",
                formato = map["formato"] as? String ?: "",
                tipoAsa = map["tipoAsa"] as? String ?: "",
                tipoAntena = map["tipoAntena"] as? String ?: "",
                numeroSegmentos = map["numeroSegmentos"] as? String ?: "",
                habitat = map["habitat"] as? String ?: "",
                comportamento = map["comportamento"] as? String ?: "",
                estagioCicloVida = map["estagioCicloVida"] as? String ?: "",
                observacoesAdicionais = map["observacoesAdicionais"] as? String ?: ""
            )
        }
    }
}

/**
 * Impact observed from insect (especially for pest category)
 */
@Parcelize
data class ImpactoInseto(
    val tipoDano: String = "",
    val nivelSeveridade: NivelSeveridade = NivelSeveridade.BAIXO,
    val areaAfetada: String = "",
    val plantasAfetadas: List<String> = emptyList(),
    val acaoTomada: String = "",
    val efetividadeControle: String = "",
    val observacoes: String = ""
) : Parcelable {
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "tipoDano" to tipoDano,
            "nivelSeveridade" to nivelSeveridade.name,
            "areaAfetada" to areaAfetada,
            "plantasAfetadas" to plantasAfetadas,
            "acaoTomada" to acaoTomada,
            "efetividadeControle" to efetividadeControle,
            "observacoes" to observacoes
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): ImpactoInseto {
            return ImpactoInseto(
                tipoDano = map["tipoDano"] as? String ?: "",
                nivelSeveridade = NivelSeveridade.valueOf(
                    map["nivelSeveridade"] as? String ?: NivelSeveridade.BAIXO.name
                ),
                areaAfetada = map["areaAfetada"] as? String ?: "",
                plantasAfetadas = (map["plantasAfetadas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                acaoTomada = map["acaoTomada"] as? String ?: "",
                efetividadeControle = map["efetividadeControle"] as? String ?: "",
                observacoes = map["observacoes"] as? String ?: ""
            )
        }
    }
}

/**
 * Severity levels for pest impact
 */
enum class NivelSeveridade {
    BAIXO,      // Low impact
    MEDIO,      // Medium impact
    ALTO,       // High impact
    CRITICO     // Critical impact requiring immediate action
}
