package com.ifpr.androidapptemplate.data.model

import java.util.*

/**
 * Data class para representar um inseto registrado
 */
data class Inseto(
    override val id: String = generateId(),
    override val nome: String = "",
    override val nomePopular: String = "",
    override val nomeCientifico: String = "",
    override val data: String = "",
    override val dataTimestamp: Long = System.currentTimeMillis(),
    override val local: String = "",
    val categoria: InsectCategory = InsectCategory.NEUTRAL,
    override val observacao: String = "",
    override val imagens: List<String> = emptyList(),
    override val userId: String = "",
    override val userName: String = "",
    override val timestamp: Long = System.currentTimeMillis(),
    override val timestampUltimaEdicao: Long = System.currentTimeMillis(),
    override val tipo: String = "INSETO",
    override val visibilidade: VisibilidadeRegistro = VisibilidadeRegistro.PRIVADO
) : BaseRegistration {

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
                categoria = try {
                    InsectCategory.valueOf(map["categoria"] as? String ?: "NEUTRAL")
                } catch (e: Exception) {
                    InsectCategory.NEUTRAL
                },
                observacao = map["observacao"] as? String ?: "",
                imagens = (map["imagens"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                timestampUltimaEdicao = (map["timestampUltimaEdicao"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                tipo = map["tipo"] as? String ?: "INSETO",
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

