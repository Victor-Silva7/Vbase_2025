package com.victor.vgroup.utils

/**
 * Utilitário centralizado para cálculos de relevância
 * Elimina duplicação em PublicSearchModels
 */
object RelevanceUtils {
    
    /**
     * Calcula score de relevância baseado em interações sociais
     * 
     * @param curtidas Número de curtidas
     * @param comentarios Número de comentários
     * @param compartilhamentos Número de compartilhamentos
     * @return Score de interação normalizado
     */
    fun calculateInteractionScore(
        curtidas: Int,
        comentarios: Int,
        compartilhamentos: Int
    ): Float {
        return (curtidas * 3 + comentarios * 5 + compartilhamentos * 7) / 10f
    }
    
    /**
     * Calcula score de completude do conteúdo
     * 
     * @param hasScientificName Se tem nome científico
     * @param hasImages Se tem imagens
     * @param hasObservation Se tem observação
     * @return Score de completude (0.0 a 1.0)
     */
    fun calculateCompletenessScore(
        hasScientificName: Boolean,
        hasImages: Boolean,
        hasObservation: Boolean
    ): Float {
        var score = 0f
        if (hasScientificName) score += 0.3f
        if (hasImages) score += 0.4f
        if (hasObservation) score += 0.3f
        return score
    }
    
    /**
     * Calcula relevância completa de um registro (planta ou inseto)
     * 
     * @param curtidas Número de curtidas
     * @param comentarios Número de comentários
     * @param compartilhamentos Número de compartilhamentos
     * @param timestamp Timestamp do registro
     * @param nomeCientifico Nome científico (vazio se não tiver)
     * @param imagens Lista de imagens
     * @param observacao Observação (vazio se não tiver)
     * @return Score de relevância final
     */
    fun calculateRelevance(
        curtidas: Int,
        comentarios: Int,
        compartilhamentos: Int,
        timestamp: Long,
        nomeCientifico: String,
        imagens: List<String>,
        observacao: String
    ): Float {
        val interactionScore = calculateInteractionScore(curtidas, comentarios, compartilhamentos)
        val freshnessScore = TimeUtils.getFreshnessScore(timestamp)
        val completenessScore = calculateCompletenessScore(
            hasScientificName = nomeCientifico.isNotEmpty(),
            hasImages = imagens.isNotEmpty(),
            hasObservation = observacao.isNotEmpty()
        )
        
        return (interactionScore + freshnessScore + completenessScore) / 3
    }
    
    /**
     * Calcula relevância para usuário
     * 
     * @param totalRegistros Total de registros do usuário
     * @param seguidores Número de seguidores
     * @param seguindo Número de pessoas seguindo
     * @param verified Se é verificado
     * @return Score de relevância do usuário
     */
    fun calculateUserRelevance(
        totalRegistros: Int,
        seguidores: Int,
        seguindo: Int,
        verified: Boolean
    ): Float {
        val activityScore = totalRegistros * 0.1f
        val socialScore = (seguidores * 0.05f + seguindo * 0.02f)
        val verificationBonus = if (verified) 2.0f else 0f
        
        return activityScore + socialScore + verificationBonus
    }
}
