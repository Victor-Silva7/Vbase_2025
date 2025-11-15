package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.data.firebase.FirebaseDatabaseService
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.*

/**
 * Repository for managing plant and insect registrations with user-specific search
 * Implements user authentication and filtering for logged users
 */
class RegistroRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: RegistroRepository? = null
        
        fun getInstance(): RegistroRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RegistroRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val databaseService = FirebaseDatabaseService.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // LiveData for real-time updates
    private val _userPlants = MutableLiveData<List<Planta>>()
    val userPlants: LiveData<List<Planta>> = _userPlants
    
    private val _userInsects = MutableLiveData<List<Inseto>>()
    val userInsects: LiveData<List<Inseto>> = _userInsects
    
    private val _filteredPlants = MutableLiveData<List<Planta>>()
    val filteredPlants: LiveData<List<Planta>> = _filteredPlants
    
    private val _filteredInsects = MutableLiveData<List<Inseto>>()
    val filteredInsects: LiveData<List<Inseto>> = _filteredInsects
    
    private val _publicPlants = MutableLiveData<List<Planta>>()
    val publicPlants: LiveData<List<Planta>> = _publicPlants
    
    private val _publicInsects = MutableLiveData<List<Inseto>>()
    val publicInsects: LiveData<List<Inseto>> = _publicInsects
    
    // Event listeners for real-time updates
    private var plantsListener: ValueEventListener? = null
    private var insectsListener: ValueEventListener? = null
    
    // Current search parameters
    private var currentPlantQuery = ""
    private var currentInsectQuery = ""
    
    /**
     * Save plant registration
     */
    suspend fun savePlant(planta: Planta): Result<String> {
        return databaseService.savePlant(planta)
    }
    
    /**
     * Save insect registration
     */
    suspend fun saveInsect(inseto: Inseto): Result<String> {
        return databaseService.saveInsect(inseto)
    }
    
    /**
     * Update plant registration
     */
    suspend fun updatePlant(planta: Planta): Result<String> {
        return databaseService.updatePlant(planta)
    }
    
    /**
     * Update insect registration
     */
    suspend fun updateInsect(inseto: Inseto): Result<String> {
        return databaseService.updateInsect(inseto)
    }
    
    /**
     * Delete plant registration
     */
    suspend fun deletePlant(plantId: String): Result<Unit> {
        return databaseService.deletePlant(plantId)
    }
    
    /**
     * Delete insect registration
     */
    suspend fun deleteInsect(insectId: String): Result<Unit> {
        return databaseService.deleteInsect(insectId)
    }
    
    /**
     * Get current user's plants (cached with real-time updates)
     */
    fun getUserPlants(userId: String? = null, forceRefresh: Boolean = false) {
        if (forceRefresh || _userPlants.value.isNullOrEmpty()) {
            repositoryScope.launch {
                android.util.Log.d("RegistroRepository", "🔄 Buscando plantas... forceRefresh=$forceRefresh")
                val result = databaseService.getUserPlants(userId)
                result.onSuccess { plantas ->
                    android.util.Log.d("RegistroRepository", "✅ Plantas carregadas: ${plantas.size}")
                    _userPlants.postValue(plantas)
                    // Apply current search filter if any
                    if (currentPlantQuery.isNotEmpty()) {
                        applyPlantFilter(currentPlantQuery)
                    } else {
                        _filteredPlants.postValue(plantas)
                    }
                }.onFailure { exception ->
                    android.util.Log.e("RegistroRepository", "❌ Erro ao carregar plantas: ${exception.message}", exception)
                }
            }
        }
    }
    
    /**
     * Get current user's insects (cached with real-time updates)
     */
    fun getUserInsects(userId: String? = null, forceRefresh: Boolean = false) {
        if (forceRefresh || _userInsects.value.isNullOrEmpty()) {
            repositoryScope.launch {
                android.util.Log.d("RegistroRepository", "🔄 Buscando insetos... forceRefresh=$forceRefresh")
                val result = databaseService.getUserInsects(userId)
                result.onSuccess { insetos ->
                    android.util.Log.d("RegistroRepository", "✅ Insetos carregados: ${insetos.size}")
                    _userInsects.postValue(insetos)
                    // Apply current search filter if any
                    if (currentInsectQuery.isNotEmpty()) {
                        applyInsectFilter(currentInsectQuery)
                    } else {
                        _filteredInsects.postValue(insetos)
                    }
                }.onFailure { exception ->
                    android.util.Log.e("RegistroRepository", "❌ Erro ao carregar insetos: ${exception.message}", exception)
                }
            }
        }
    }
    
    /**
     * Search user's plants by query (user-specific search)
     */
    suspend fun searchUserPlants(
        query: String = "",
        category: PlantHealthCategory? = null,
        location: String = "",
        dateFrom: Long = 0L,
        dateTo: Long = Long.MAX_VALUE,
        userId: String? = null
    ): Result<List<Planta>> {
        return try {
            val targetUserId = userId ?: FirebaseConfig.getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val userPlantsResult = databaseService.getUserPlants(targetUserId)
            
            userPlantsResult.onSuccess { allPlants ->
                val filteredPlants = allPlants.filter { planta ->
                    val matchesQuery = query.isEmpty() || 
                        planta.nome.contains(query, ignoreCase = true) ||
                        planta.nomePopular.contains(query, ignoreCase = true) ||
                        planta.nomeCientifico.contains(query, ignoreCase = true) ||
                        planta.observacao.contains(query, ignoreCase = true) ||
                        planta.local.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category == null || planta.categoria == category
                    
                    val matchesLocation = location.isEmpty() || 
                        planta.local.contains(location, ignoreCase = true)
                    
                    val matchesDateRange = planta.timestamp in dateFrom..dateTo
                    
                    matchesQuery && matchesCategory && matchesLocation && matchesDateRange
                }
                
                return Result.success(filteredPlants.sortedByDescending { it.timestamp })
            }
            
            userPlantsResult
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search user's insects by query (user-specific search)
     */
    suspend fun searchUserInsects(
        query: String = "",
        category: InsectCategory? = null,
        location: String = "",
        dateFrom: Long = 0L,
        dateTo: Long = Long.MAX_VALUE,
        userId: String? = null
    ): Result<List<Inseto>> {
        return try {
            val targetUserId = userId ?: FirebaseConfig.getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))
            
            val userInsectsResult = databaseService.getUserInsects(targetUserId)
            
            userInsectsResult.onSuccess { allInsects ->
                val filteredInsects = allInsects.filter { inseto ->
                    val matchesQuery = query.isEmpty() || 
                        inseto.nome.contains(query, ignoreCase = true) ||
                        inseto.nomePopular.contains(query, ignoreCase = true) ||
                        inseto.nomeCientifico.contains(query, ignoreCase = true) ||
                        inseto.observacao.contains(query, ignoreCase = true) ||
                        inseto.local.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category == null || inseto.categoria == category
                    
                    val matchesLocation = location.isEmpty() || 
                        inseto.local.contains(location, ignoreCase = true)
                    
                    val matchesDateRange = inseto.timestamp in dateFrom..dateTo
                    
                    matchesQuery && matchesCategory && matchesLocation && matchesDateRange
                }
                
                return Result.success(filteredInsects.sortedByDescending { it.timestamp })
            }
            
            userInsectsResult
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Apply real-time filter to plants
     */
    private fun applyPlantFilter(query: String) {
        currentPlantQuery = query
        val currentPlants = _userPlants.value ?: return
        
        if (query.isEmpty()) {
            _filteredPlants.postValue(currentPlants)
            return
        }
        
        val filtered = currentPlants.filter { planta ->
            planta.nome.contains(query, ignoreCase = true) ||
            planta.nomePopular.contains(query, ignoreCase = true) ||
            planta.nomeCientifico.contains(query, ignoreCase = true) ||
            planta.observacao.contains(query, ignoreCase = true) ||
            planta.local.contains(query, ignoreCase = true)
        }
        
        _filteredPlants.postValue(filtered)
    }
    
    /**
     * Apply real-time filter to insects
     */
    private fun applyInsectFilter(query: String) {
        currentInsectQuery = query
        val currentInsects = _userInsects.value ?: return
        
        if (query.isEmpty()) {
            _filteredInsects.postValue(currentInsects)
            return
        }
        
        val filtered = currentInsects.filter { inseto ->
            inseto.nome.contains(query, ignoreCase = true) ||
            inseto.nomePopular.contains(query, ignoreCase = true) ||
            inseto.nomeCientifico.contains(query, ignoreCase = true) ||
            inseto.observacao.contains(query, ignoreCase = true) ||
            inseto.local.contains(query, ignoreCase = true)
        }
        
        _filteredInsects.postValue(filtered)
    }
    
    /**
     * Filter plants by query in real-time
     */
    fun filterPlants(query: String) {
        applyPlantFilter(query)
    }
    
    /**
     * Filter insects by query in real-time
     */
    fun filterInsects(query: String) {
        applyInsectFilter(query)
    }
    
    /**
     * Clear plant filter
     */
    fun clearPlantFilter() {
        currentPlantQuery = ""
        _filteredPlants.postValue(_userPlants.value ?: emptyList())
    }
    
    /**
     * Clear insect filter
     */
    fun clearInsectFilter() {
        currentInsectQuery = ""
        _filteredInsects.postValue(_userInsects.value ?: emptyList())
    }
    
    /**
     * Get public plants for community feed
     */
    fun getPublicPlants(limit: Int = 20, forceRefresh: Boolean = false) {
        if (forceRefresh || _publicPlants.value.isNullOrEmpty()) {
            repositoryScope.launch {
                val result = databaseService.getPublicPlants(limit)
                result.onSuccess { plantas ->
                    _publicPlants.postValue(plantas)
                }
            }
        }
    }
    
    /**
     * Get public insects for community feed
     */
    fun getPublicInsects(limit: Int = 20, forceRefresh: Boolean = false) {
        if (forceRefresh || _publicInsects.value.isNullOrEmpty()) {
            repositoryScope.launch {
                val result = databaseService.getPublicInsects(limit)
                result.onSuccess { insetos ->
                    _publicInsects.postValue(insetos)
                }
            }
        }
    }
    
    /**
     * Start listening for real-time updates to user's plants
     */
    fun startListeningToUserPlants(userId: String? = null) {
        stopListeningToUserPlants() // Stop existing listener
        
        Log.d("RegistroRepository", "Starting listener para plantas do usuário")
        plantsListener = databaseService.listenToUserPlants(userId) { plantas ->
            Log.d("RegistroRepository", "Plantas atualizadas: ${plantas.size} registros")
            _userPlants.postValue(plantas)
            // Apply current filter if any
            if (currentPlantQuery.isNotEmpty()) {
                applyPlantFilter(currentPlantQuery)
            } else {
                _filteredPlants.postValue(plantas)
            }
        }
    }
    
    /**
     * Start listening for real-time updates to user's insects
     */
    fun startListeningToUserInsects(userId: String? = null) {
        stopListeningToUserInsects() // Stop existing listener
        
        Log.d("RegistroRepository", "Starting listener para insetos do usuário")
        insectsListener = databaseService.listenToUserInsects(userId) { insetos ->
            Log.d("RegistroRepository", "Insetos atualizados: ${insetos.size} registros")
            _userInsects.postValue(insetos)
            // Apply current filter if any
            if (currentInsectQuery.isNotEmpty()) {
                applyInsectFilter(currentInsectQuery)
            } else {
                _filteredInsects.postValue(insetos)
            }
        }
    }
    
    /**
     * Stop listening for real-time updates to user's plants
     */
    fun stopListeningToUserPlants(userId: String? = null) {
        plantsListener?.let { listener ->
            databaseService.removeListener(userId, listener, isPlant = true)
            plantsListener = null
        }
    }
    
    /**
     * Stop listening for real-time updates to user's insects
     */
    fun stopListeningToUserInsects(userId: String? = null) {
        insectsListener?.let { listener ->
            databaseService.removeListener(userId, listener, isPlant = false)
            insectsListener = null
        }
    }
    
    /**
     * Get registration statistics for current user
     */
    suspend fun getRegistrationStats(userId: String? = null): Result<RegistrationStats> {
        return try {
            val targetUserId = userId ?: FirebaseConfig.getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))
                
            val plantsResult = databaseService.getUserPlants(targetUserId)
            val insectsResult = databaseService.getUserInsects(targetUserId)
            
            val plantas = plantsResult.getOrElse { emptyList() }
            val insetos = insectsResult.getOrElse { emptyList() }
            
            val stats = RegistrationStats(
                totalPlantas = plantas.size,
                totalInsetos = insetos.size,
                plantasSaudaveis = plantas.count { it.categoria == PlantHealthCategory.HEALTHY },
                plantasDoentes = plantas.count { it.categoria == PlantHealthCategory.SICK },
                insetosBenefico = insetos.count { it.categoria == InsectCategory.BENEFICIAL },
                insetosNeutro = insetos.count { it.categoria == InsectCategory.NEUTRAL },
                insetosPraga = insetos.count { it.categoria == InsectCategory.PEST },
                totalImagens = (plantas.sumOf { it.imagens.size } + insetos.sumOf { it.imagens.size }),
                ultimoRegistro = maxOf(
                    plantas.maxByOrNull { it.timestamp }?.timestamp ?: 0L,
                    insetos.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                )
            )
            
            Result.success(stats)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return FirebaseConfig.isUserAuthenticated()
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return FirebaseConfig.getCurrentUserId()
    }
    
    /**
     * Clear repository cache
     */
    fun clearCache() {
        _userPlants.value = emptyList()
        _userInsects.value = emptyList()
        _filteredPlants.value = emptyList()
        _filteredInsects.value = emptyList()
        _publicPlants.value = emptyList()
        _publicInsects.value = emptyList()
        currentPlantQuery = ""
        currentInsectQuery = ""
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopListeningToUserPlants()
        stopListeningToUserInsects()
        repositoryScope.cancel()
        clearCache()
    }
}

/**
 * Data class for registration statistics
 */
data class RegistrationStats(
    val totalPlantas: Int = 0,
    val totalInsetos: Int = 0,
    val plantasSaudaveis: Int = 0,
    val plantasDoentes: Int = 0,
    val insetosBenefico: Int = 0,
    val insetosNeutro: Int = 0,
    val insetosPraga: Int = 0,
    val totalImagens: Int = 0,
    val ultimoRegistro: Long = 0L
) {
    fun getTotalRegistros(): Int = totalPlantas + totalInsetos
    
    fun getPlantHealthPercentage(): Float {
        return if (totalPlantas > 0) {
            (plantasSaudaveis.toFloat() / totalPlantas.toFloat()) * 100
        } else 0f
    }
    
    fun getPestPercentage(): Float {
        return if (totalInsetos > 0) {
            (insetosPraga.toFloat() / totalInsetos.toFloat()) * 100
        } else 0f
    }
}