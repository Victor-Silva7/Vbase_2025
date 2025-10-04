package com.ifpr.androidapptemplate.data.model

import java.util.*

/**
 * Data class para representar uma planta registrada
 */
data class Planta(
    override val id: String = generateId(),
    override val nome: String = "",
    override val nomePopular: String = "",
    override val nomeCientifico: String = "",
    override val data: String = "",
    override val dataTimestamp: Long = System.currentTimeMillis(),
    override val local: String = "",
    val categoria: PlantHealthCategory = PlantHealthCategory.HEALTHY,
    override val observacao: String = "",
    override val imagens: List<String> = emptyList(),
    override val userId: String = "",
    override val userName: String = "",
    override val timestamp: Long = System.currentTimeMillis(),
    override val timestampUltimaEdicao: Long = System.currentTimeMillis(),
    override val tipo: String = "PLANTA",
    override val visibilidade: VisibilidadeRegistro = VisibilidadeRegistro.PRIVADO
) : BaseRegistration {

    companion object {
        /**
         * Gera um ID único para a planta
         */
        fun generateId(): String {
            return "plant_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        }

        /**
         * Cria objeto Planta a partir de dados do Firebase
         */
        fun fromFirebaseMap(map: Map<String, Any?>): Planta {
            return Planta(
                id = map["id"] as? String ?: generateId(),
                nome = map["nome"] as? String ?: "",
                nomePopular = map["nomePopular"] as? String ?: "",
                nomeCientifico = map["nomeCientifico"] as? String ?: "",
                data = map["data"] as? String ?: "",
                dataTimestamp = (map["dataTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                local = map["local"] as? String ?: "",
                categoria = try {
                    PlantHealthCategory.valueOf(map["categoria"] as? String ?: "HEALTHY")
                } catch (e: Exception) {
                    PlantHealthCategory.HEALTHY
                },
                observacao = map["observacao"] as? String ?: "",
                imagens = (map["imagens"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                timestampUltimaEdicao = (map["timestampUltimaEdicao"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                tipo = map["tipo"] as? String ?: "PLANTA",
                visibilidade = try {
                    VisibilidadeRegistro.valueOf(map["visibilidade"] as? String ?: "PRIVADO")
                } catch (e: Exception) {
                    VisibilidadeRegistro.PRIVADO
                }
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
            "categoria" to categoria.name,
            "observacao" to observacao,
            "imagens" to imagens,
            "userId" to userId,
            "userName" to userName,
            "timestamp" to timestamp,
            "timestampUltimaEdicao" to timestampUltimaEdicao,
            "tipo" to tipo,
            "visibilidade" to visibilidade.name
        )
    }

    /**
     * Obtém a primeira imagem disponível
     */
    fun getFirstImage(): String? = imagens.firstOrNull()

    /**
     * Verifica se tem múltiplas imagens
     */
    fun hasMultipleImages(): Boolean = imagens.size > 1

    /**
     * Obtém texto de status da planta
     */
    fun getStatusText(): String {
        return when (categoria) {
            PlantHealthCategory.HEALTHY -> "Saudável"
            PlantHealthCategory.SICK -> "Doente"
        }
    }

    /**
     * Obtém cor do status
     */
    fun getStatusColor(): String {
        return when (categoria) {
            PlantHealthCategory.HEALTHY -> "#4caf50"
            PlantHealthCategory.SICK -> "#f44336"
        }
    }

    /**
     * Verifica se a planta é pública
     */
    fun isPublic(): Boolean = visibilidade == VisibilidadeRegistro.PUBLICO

    /**
     * Obtém resumo da planta para exibição
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        if (nomePopular.isNotEmpty()) parts.add(nomePopular)
        if (nomeCientifico.isNotEmpty()) parts.add("($nomeCientifico)")
        if (local.isNotEmpty()) parts.add("em $local")
        return parts.joinToString(" ")
    }
}

/**
 * Plant characteristics data class
 */
data class CaracteristicasPlanta(
    val altura: String = "",
    val diametroTronco: String = "",
    val tipoFolha: String = "",
    val corFlor: String = "",
    val tempoFlorada: String = "",
    val tipoFruto: String = "",
    val habitat: String = "",
    val observacoesAdicionais: String = ""
) {
    
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