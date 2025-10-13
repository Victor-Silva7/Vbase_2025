package com.ifpr.androidapptemplate.data.model

/**
 * Modelo para resultados de busca pública
 */
data class PublicSearchResult(
    val plants: List<PublicPlanta> = emptyList(),
    val insects: List<PublicInseto> = emptyList(),
    val users: List<PublicUser> = emptyList(),
    val query: String = "",
    val totalResults: Int = 0,
    val searchTime: Long = System.currentTimeMillis()
) {
    fun isEmpty(): Boolean = totalResults == 0
    fun hasPlants(): Boolean = plants.isNotEmpty()
    fun hasInsects(): Boolean = insects.isNotEmpty()
    fun hasUsers(): Boolean = users.isNotEmpty()
    
    fun getAllItems(): List<SearchableItem> {
        val items = mutableListOf<SearchableItem>()
        plants.forEach { items.add(SearchableItem.PlantResult(it)) }
        insects.forEach { items.add(SearchableItem.InsectResult(it)) }
        users.forEach { items.add(SearchableItem.UserResult(it)) }
        return items.sortedByDescending { it.relevanceScore }
    }
}

/**
 * Item unificado de resultado de busca
 */
sealed class SearchableItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val imageUrl: String
    abstract val type: SearchItemType
    abstract val relevanceScore: Float
    abstract val timestamp: Long
    
    data class PlantResult(val plant: PublicPlanta) : SearchableItem() {
        override val id: String = plant.id
        override val title: String = plant.nome
        override val subtitle: String = plant.nomeCientifico.ifEmpty { plant.local }
        override val imageUrl: String = plant.imagens.firstOrNull() ?: ""
        override val type: SearchItemType = SearchItemType.PLANT
        override val relevanceScore: Float = plant.calculateRelevance()
        override val timestamp: Long = plant.timestamp
    }
    
    data class InsectResult(val insect: PublicInseto) : SearchableItem() {
        override val id: String = insect.id
        override val title: String = insect.nome
        override val subtitle: String = insect.nomeCientifico.ifEmpty { insect.local }
        override val imageUrl: String = insect.imagens.firstOrNull() ?: ""
        override val type: SearchItemType = SearchItemType.INSECT
        override val relevanceScore: Float = insect.calculateRelevance()
        override val timestamp: Long = insect.timestamp
    }
    
    data class UserResult(val user: PublicUser) : SearchableItem() {
        override val id: String = user.id
        override val title: String = user.nome
        override val subtitle: String = "${user.totalRegistros} registros"
        override val imageUrl: String = user.avatarUrl
        override val type: SearchItemType = SearchItemType.USER
        override val relevanceScore: Float = user.calculateRelevance()
        override val timestamp: Long = user.lastActivityTimestamp
    }
}

/**
 * Tipos de itens de busca
 */
enum class SearchItemType {
    PLANT,
    INSECT,
    USER,
    ALL
}

/**
 * Dados públicos de uma planta
 */
data class PublicPlanta(
    val id: String = "",
    val nome: String = "",
    val nomePopular: String = "",
    val nomeCientifico: String = "",
    val categoria: String = "",
    val local: String = "",
    val observacao: String = "",
    val imagens: List<String> = emptyList(),
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val timestamp: Long = 0L,
    val curtidas: Int = 0,
    val comentarios: Int = 0,
    val compartilhamentos: Int = 0,
    val visualizacoes: Int = 0,
    val coordenadas: Coordenadas? = null,
    val tags: List<String> = emptyList()
) {
    fun calculateRelevance(): Float {
        // Algoritmo simples de relevância baseado em interações
        val interactionScore = (curtidas * 3 + comentarios * 5 + compartilhamentos * 7) / 10f
        val freshnessScore = if (System.currentTimeMillis() - timestamp < 7 * 24 * 60 * 60 * 1000) 1.2f else 1.0f
        val completenessScore = (if (nomeCientifico.isNotEmpty()) 0.3f else 0f) +
                              (if (imagens.isNotEmpty()) 0.4f else 0f) +
                              (if (observacao.isNotEmpty()) 0.3f else 0f)
        
        return (interactionScore + completenessScore) * freshnessScore
    }
    
    fun getFormattedDate(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "agora"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}min"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
            else -> "${diff / (7 * 24 * 60 * 60 * 1000)}sem"
        }
    }
}

/**
 * Dados públicos de um inseto
 */
data class PublicInseto(
    val id: String = "",
    val nome: String = "",
    val nomePopular: String = "",
    val nomeCientifico: String = "",
    val categoria: String = "",
    val local: String = "",
    val observacao: String = "",
    val imagens: List<String> = emptyList(),
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val timestamp: Long = 0L,
    val curtidas: Int = 0,
    val comentarios: Int = 0,
    val compartilhamentos: Int = 0,
    val visualizacoes: Int = 0,
    val coordenadas: Coordenadas? = null,
    val tags: List<String> = emptyList()
) {
    fun calculateRelevance(): Float {
        // Mesmo algoritmo da planta
        val interactionScore = (curtidas * 3 + comentarios * 5 + compartilhamentos * 7) / 10f
        val freshnessScore = if (System.currentTimeMillis() - timestamp < 7 * 24 * 60 * 60 * 1000) 1.2f else 1.0f
        val completenessScore = (if (nomeCientifico.isNotEmpty()) 0.3f else 0f) +
                              (if (imagens.isNotEmpty()) 0.4f else 0f) +
                              (if (observacao.isNotEmpty()) 0.3f else 0f)
        
        return (interactionScore + completenessScore) * freshnessScore
    }
    
    fun getFormattedDate(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "agora"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}min"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
            else -> "${diff / (7 * 24 * 60 * 60 * 1000)}sem"
        }
    }
}

/**
 * Dados públicos de um usuário
 */
data class PublicUser(
    val id: String = "",
    val nome: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val totalRegistros: Int = 0,
    val totalPlantas: Int = 0,
    val totalInsetos: Int = 0,
    val seguidores: Int = 0,
    val seguindo: Int = 0,
    val verified: Boolean = false,
    val lastActivityTimestamp: Long = 0L,
    val registrationDate: Long = 0L,
    val especialidades: List<String> = emptyList()
) {
    fun calculateRelevance(): Float {
        val activityScore = totalRegistros * 0.1f
        val socialScore = (seguidores * 0.05f + seguindo * 0.02f)
        val verificationBonus = if (verified) 2.0f else 0f
        val activityRecency = if (System.currentTimeMillis() - lastActivityTimestamp < 30 * 24 * 60 * 60 * 1000) 1.5f else 1.0f
        
        return (activityScore + socialScore + verificationBonus) * activityRecency
    }
}

/**
 * Filtros de busca
 */
data class SearchFilters(
    val type: SearchItemType = SearchItemType.ALL,
    val location: String = "",
    val dateRange: DateRange = DateRange.ALL_TIME,
    val sortBy: SortOrder = SortOrder.RELEVANCE,
    val userId: String = "",
    val categoria: String = "",
    val hasImages: Boolean? = null,
    val hasScientificName: Boolean? = null,
    val minInteractions: Int = 0
) {
    fun isEmpty(): Boolean {
        return type == SearchItemType.ALL &&
               location.isEmpty() &&
               dateRange == DateRange.ALL_TIME &&
               userId.isEmpty() &&
               categoria.isEmpty() &&
               hasImages == null &&
               hasScientificName == null &&
               minInteractions == 0
    }
}

/**
 * Período de data para filtros
 */
enum class DateRange(val days: Int) {
    ALL_TIME(-1),
    LAST_WEEK(7),
    LAST_MONTH(30),
    LAST_YEAR(365)
}

/**
 * Ordem de classificação
 */
enum class SortOrder {
    RELEVANCE,
    DATE_DESC,
    DATE_ASC,
    POPULARITY,
    ALPHABETICAL
}