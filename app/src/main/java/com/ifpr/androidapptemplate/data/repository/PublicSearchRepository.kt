package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Repository para busca de registros públicos
 * Gerencia buscas globais na comunidade
 */
class PublicSearchRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: PublicSearchRepository? = null

        fun getInstance(): PublicSearchRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PublicSearchRepository().also { INSTANCE = it }
            }
        }
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // LiveData para resultados de busca
    private val _searchResults = MutableLiveData<PublicSearchResult>()
    val searchResults: LiveData<PublicSearchResult> = _searchResults

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _searchSuggestions = MutableLiveData<List<String>>()
    val searchSuggestions: LiveData<List<String>> = _searchSuggestions

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val _popularSearches = MutableLiveData<List<String>>()
    val popularSearches: LiveData<List<String>> = _popularSearches

    // Cache de resultados para melhor performance
    private val searchCache = mutableMapOf<String, PublicSearchResult>()
    private val maxCacheSize = 50

    init {
        loadRecentSearches()
        loadPopularSearches()
    }

    /**
     * Busca global por registros públicos
     */
    suspend fun searchPublicRecords(
        query: String,
        filters: SearchFilters = SearchFilters()
    ): Result<PublicSearchResult> {
        return try {
            _isSearching.value = true
            
            // Verificar cache primeiro
            val cacheKey = createCacheKey(query, filters)
            searchCache[cacheKey]?.let { cachedResult ->
                // Se o resultado é recente (menos de 5 minutos), usar cache
                if (System.currentTimeMillis() - cachedResult.searchTime < 5 * 60 * 1000) {
                    _searchResults.postValue(cachedResult)
                    _isSearching.postValue(false)
                    return Result.success(cachedResult)
                }
            }

            // Simular delay de rede (remover quando integrar Firebase real)
            delay(1000)

            // TODO: Integrar com Firebase Database
            val mockResult = createMockSearchResult(query, filters)
            
            // Cache do resultado
            cacheSearchResult(cacheKey, mockResult)
            
            // Salvar busca recente
            addToRecentSearches(query)
            
            _searchResults.postValue(mockResult)
            _isSearching.postValue(false)
            
            Result.success(mockResult)
            
        } catch (e: Exception) {
            _isSearching.postValue(false)
            Result.failure(e)
        }
    }

    /**
     * Busca com debouncing para sugestões em tempo real
     */
    fun searchWithDebouncing(query: String, delayMs: Long = 300) {
        repositoryScope.launch {
            delay(delayMs)
            if (query.length >= 2) {
                val suggestions = generateSearchSuggestions(query)
                _searchSuggestions.postValue(suggestions)
            } else {
                _searchSuggestions.postValue(emptyList())
            }
        }
    }

    /**
     * Limpar resultados de busca
     */
    fun clearSearchResults() {
        _searchResults.value = PublicSearchResult()
        _searchSuggestions.value = emptyList()
    }

    /**
     * Limpar histórico de buscas
     */
    fun clearSearchHistory() {
        _recentSearches.value = emptyList()
        // TODO: Limpar do SharedPreferences/Firebase
    }

    /**
     * Obter trending/populares
     */
    suspend fun getTrendingSearches(): Result<List<String>> {
        return try {
            // TODO: Buscar do Firebase Analytics
            val trending = listOf(
                "Rosa vermelha",
                "Joaninha",
                "Plantas medicinais",
                "Borboletas",
                "Jardim vertical",
                "Pragas comuns",
                "Plantas nativas",
                "Insetos benéficos"
            )
            _popularSearches.postValue(trending)
            Result.success(trending)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Criar chave de cache
     */
    private fun createCacheKey(query: String, filters: SearchFilters): String {
        return "${query.lowercase()}_${filters.hashCode()}"
    }

    /**
     * Cache do resultado com limite de tamanho
     */
    private fun cacheSearchResult(key: String, result: PublicSearchResult) {
        if (searchCache.size >= maxCacheSize) {
            // Remove o mais antigo
            val oldestKey = searchCache.keys.first()
            searchCache.remove(oldestKey)
        }
        searchCache[key] = result
    }

    /**
     * Gerar sugestões de busca
     */
    private fun generateSearchSuggestions(query: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Sugestões baseadas em plantas comuns
        val plantSuggestions = listOf(
            "Rosa", "Margarida", "Girassol", "Orquídea", "Violeta",
            "Samambaia", "Cactos", "Suculentas", "Plantas medicinais"
        ).filter { it.contains(query, ignoreCase = true) }
        
        // Sugestões baseadas em insetos comuns
        val insectSuggestions = listOf(
            "Joaninha", "Borboleta", "Abelha", "Formiga", "Libélula",
            "Grilo", "Gafanhoto", "Besouro", "Insetos benéficos"
        ).filter { it.contains(query, ignoreCase = true) }
        
        suggestions.addAll(plantSuggestions)
        suggestions.addAll(insectSuggestions)
        
        return suggestions.take(8)
    }

    /**
     * Adicionar à lista de buscas recentes
     */
    private fun addToRecentSearches(query: String) {
        val current = _recentSearches.value?.toMutableList() ?: mutableListOf()
        
        // Remover se já existe
        current.remove(query)
        
        // Adicionar no início
        current.add(0, query)
        
        // Limitar a 10 itens
        if (current.size > 10) {
            current.removeAt(current.size - 1)
        }
        
        _recentSearches.postValue(current)
        
        // TODO: Salvar no SharedPreferences
    }

    /**
     * Carregar buscas recentes do storage
     */
    private fun loadRecentSearches() {
        // TODO: Carregar do SharedPreferences
        val mockRecent = listOf(
            "Rosa do jardim",
            "Joaninha vermelha",
            "Plantas do quintal"
        )
        _recentSearches.postValue(mockRecent)
    }

    /**
     * Carregar buscas populares
     */
    private fun loadPopularSearches() {
        repositoryScope.launch {
            getTrendingSearches()
        }
    }

    /**
     * Criar resultado mock para desenvolvimento
     */
    private fun createMockSearchResult(query: String, filters: SearchFilters): PublicSearchResult {
        val mockPlants = createMockPlants(query, filters)
        val mockInsects = createMockInsects(query, filters)
        val mockUsers = createMockUsers(query, filters)
        
        return PublicSearchResult(
            plants = mockPlants,
            insects = mockInsects,
            users = mockUsers,
            query = query,
            totalResults = mockPlants.size + mockInsects.size + mockUsers.size,
            searchTime = System.currentTimeMillis()
        )
    }

    /**
     * Criar plantas mock filtradas
     */
    private fun createMockPlants(query: String, filters: SearchFilters): List<PublicPlanta> {
        if (filters.type == SearchItemType.INSECT || filters.type == SearchItemType.USER) {
            return emptyList()
        }

        val allMockPlants = listOf(
            PublicPlanta(
                id = "plant_1", nome = "Rosa Vermelha", nomeCientifico = "Rosa damascena",
                categoria = "Ornamental", local = "Jardim Central", 
                observacao = "Linda rosa florescendo no jardim",
                imagens = listOf("https://example.com/rosa1.jpg"),
                userId = "user1", userName = "João Silva", timestamp = System.currentTimeMillis() - 3600000,
                curtidas = 23, comentarios = 5, compartilhamentos = 2
            ),
            PublicPlanta(
                id = "plant_2", nome = "Girassol Gigante", nomeCientifico = "Helianthus annuus",
                categoria = "Ornamental", local = "Campo do Norte",
                observacao = "Girassol impressionante de 3 metros de altura",
                imagens = listOf("https://example.com/girassol1.jpg"),
                userId = "user2", userName = "Maria Santos", timestamp = System.currentTimeMillis() - 7200000,
                curtidas = 45, comentarios = 12, compartilhamentos = 8
            ),
            PublicPlanta(
                id = "plant_3", nome = "Orquídea Roxa", nomeCientifico = "Cattleya violacea",
                categoria = "Ornamental", local = "Estufa Tropical",
                observacao = "Orquídea rara em floração",
                imagens = listOf("https://example.com/orquidea1.jpg"),
                userId = "user3", userName = "Pedro Oliveira", timestamp = System.currentTimeMillis() - 14400000,
                curtidas = 67, comentarios = 18, compartilhamentos = 15
            )
        )

        return allMockPlants.filter { plant ->
            plant.nome.contains(query, ignoreCase = true) ||
            plant.nomeCientifico.contains(query, ignoreCase = true) ||
            plant.observacao.contains(query, ignoreCase = true) ||
            plant.local.contains(query, ignoreCase = true)
        }
    }

    /**
     * Criar insetos mock filtrados
     */
    private fun createMockInsects(query: String, filters: SearchFilters): List<PublicInseto> {
        if (filters.type == SearchItemType.PLANT || filters.type == SearchItemType.USER) {
            return emptyList()
        }

        val allMockInsects = listOf(
            PublicInseto(
                id = "insect_1", nome = "Joaninha Vermelha", nomeCientifico = "Coccinella septempunctata",
                categoria = "Benéfico", local = "Jardim das Rosas",
                observacao = "Joaninha controlando pulgões naturalmente",
                imagens = listOf("https://example.com/joaninha1.jpg"),
                userId = "user4", userName = "Ana Costa", timestamp = System.currentTimeMillis() - 1800000,
                curtidas = 34, comentarios = 7, compartilhamentos = 4
            ),
            PublicInseto(
                id = "insect_2", nome = "Borboleta Azul", nomeCientifico = "Morpho peleides",
                categoria = "Polinizador", local = "Trilha da Mata",
                observacao = "Borboleta rara avistada na trilha",
                imagens = listOf("https://example.com/borboleta1.jpg"),
                userId = "user5", userName = "Carlos Lima", timestamp = System.currentTimeMillis() - 5400000,
                curtidas = 89, comentarios = 23, compartilhamentos = 19
            )
        )

        return allMockInsects.filter { insect ->
            insect.nome.contains(query, ignoreCase = true) ||
            insect.nomeCientifico.contains(query, ignoreCase = true) ||
            insect.observacao.contains(query, ignoreCase = true) ||
            insect.local.contains(query, ignoreCase = true)
        }
    }

    /**
     * Criar usuários mock filtrados
     */
    private fun createMockUsers(query: String, filters: SearchFilters): List<PublicUser> {
        if (filters.type == SearchItemType.PLANT || filters.type == SearchItemType.INSECT) {
            return emptyList()
        }

        val allMockUsers = listOf(
            PublicUser(
                id = "user1", nome = "João Silva", username = "@joao_botanico",
                avatarUrl = "https://example.com/avatar1.jpg",
                bio = "Especialista em plantas ornamentais",
                totalRegistros = 156, totalPlantas = 120, totalInsetos = 36,
                seguidores = 2340, seguindo = 890, verified = true,
                lastActivityTimestamp = System.currentTimeMillis() - 3600000,
                especialidades = listOf("Rosas", "Orquídeas", "Plantas Medicinais")
            ),
            PublicUser(
                id = "user2", nome = "Maria Santos", username = "@maria_verde",
                avatarUrl = "https://example.com/avatar2.jpg",
                bio = "Amante da natureza e fotografa",
                totalRegistros = 89, totalPlantas = 45, totalInsetos = 44,
                seguidores = 1250, seguindo = 567, verified = false,
                lastActivityTimestamp = System.currentTimeMillis() - 7200000,
                especialidades = listOf("Fotografia", "Borboletas", "Jardins")
            )
        )

        return allMockUsers.filter { user ->
            user.nome.contains(query, ignoreCase = true) ||
            user.username.contains(query, ignoreCase = true) ||
            user.bio.contains(query, ignoreCase = true) ||
            user.especialidades.any { it.contains(query, ignoreCase = true) }
        }
    }
}