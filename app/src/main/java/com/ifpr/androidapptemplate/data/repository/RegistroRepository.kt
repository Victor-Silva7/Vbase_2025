package com.ifpr.androidapptemplate.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.data.firebase.FirebaseDatabaseService
import com.ifpr.androidapptemplate.data.firebase.FirebaseStorageManager
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.*

/**
 * Repository for managing plant and insect registrations
 * Follows Repository pattern for clean architecture
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
    private val storageManager = FirebaseStorageManager.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // LiveData for real-time updates
    private val _userPlants = MutableLiveData<List<Planta>>()
    val userPlants: LiveData<List<Planta>> = _userPlants
    
    private val _userInsects = MutableLiveData<List<Inseto>>()
    val userInsects: LiveData<List<Inseto>> = _userInsects
    
    private val _publicPlants = MutableLiveData<List<Planta>>()
    val publicPlants: LiveData<List<Planta>> = _publicPlants
    
    private val _publicInsects = MutableLiveData<List<Inseto>>()
    val publicInsects: LiveData<List<Inseto>> = _publicInsects
    
    // Event listeners for real-time updates
    private var plantsListener: ValueEventListener? = null
    private var insectsListener: ValueEventListener? = null
    
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
     * Get user's plants (cached with real-time updates)
     */
    fun getUserPlants(userId: String? = null, forceRefresh: Boolean = false) {
        if (forceRefresh || _userPlants.value.isNullOrEmpty()) {
            repositoryScope.launch {
                val result = databaseService.getUserPlants(userId)
                result.onSuccess { plantas ->
                    _userPlants.postValue(plantas)
                }
            }
        }
    }
    
    /**
     * Get user's insects (cached with real-time updates)
     */
    fun getUserInsects(userId: String? = null, forceRefresh: Boolean = false) {
        if (forceRefresh || _userInsects.value.isNullOrEmpty()) {
            repositoryScope.launch {
                val result = databaseService.getUserInsects(userId)
                result.onSuccess { insetos ->
                    _userInsects.postValue(insetos)
                }
            }
        }
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
        
        plantsListener = databaseService.listenToUserPlants(userId) { plantas ->
            _userPlants.postValue(plantas)
        }
    }
    
    /**
     * Start listening for real-time updates to user's insects
     */
    fun startListeningToUserInsects(userId: String? = null) {
        stopListeningToUserInsects() // Stop existing listener
        
        insectsListener = databaseService.listenToUserInsects(userId) { insetos ->
            _userInsects.postValue(insetos)
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
     * Search plants by criteria
     */
    suspend fun searchPlants(
        query: String = "",
        category: PlantHealthCategory? = null,
        location: String = "",
        dateFrom: Long = 0L,
        dateTo: Long = Long.MAX_VALUE,
        userId: String? = null
    ): Result<List<Planta>> {
        return try {
            val allPlantsResult = if (userId != null) {
                databaseService.getUserPlants(userId)
            } else {
                databaseService.getPublicPlants(100) // Get more for filtering
            }
            
            allPlantsResult.onSuccess { allPlants ->
                val filteredPlants = allPlants.filter { planta ->
                    val matchesQuery = query.isEmpty() || 
                        planta.nome.contains(query, ignoreCase = true) ||
                        planta.nomePopular.contains(query, ignoreCase = true) ||
                        planta.nomeCientifico.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category == null || planta.categoria == category
                    
                    val matchesLocation = location.isEmpty() || 
                        planta.local.contains(location, ignoreCase = true)
                    
                    val matchesDateRange = planta.timestamp in dateFrom..dateTo
                    
                    matchesQuery && matchesCategory && matchesLocation && matchesDateRange
                }
                
                return Result.success(filteredPlants)
            }
            
            allPlantsResult
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search insects by criteria
     */
    suspend fun searchInsects(
        query: String = "",
        category: InsectCategory? = null,
        location: String = "",
        dateFrom: Long = 0L,
        dateTo: Long = Long.MAX_VALUE,
        userId: String? = null
    ): Result<List<Inseto>> {
        return try {
            val allInsectsResult = if (userId != null) {
                databaseService.getUserInsects(userId)
            } else {
                databaseService.getPublicInsects(100) // Get more for filtering
            }
            
            allInsectsResult.onSuccess { allInsects ->
                val filteredInsects = allInsects.filter { inseto ->
                    val matchesQuery = query.isEmpty() || 
                        inseto.nome.contains(query, ignoreCase = true) ||
                        inseto.nomePopular.contains(query, ignoreCase = true) ||
                        inseto.nomeCientifico.contains(query, ignoreCase = true)
                    
                    val matchesCategory = category == null || inseto.categoria == category
                    
                    val matchesLocation = location.isEmpty() || 
                        inseto.local.contains(location, ignoreCase = true)
                    
                    val matchesDateRange = inseto.timestamp in dateFrom..dateTo
                    
                    matchesQuery && matchesCategory && matchesLocation && matchesDateRange
                }
                
                return Result.success(filteredInsects)
            }
            
            allInsectsResult
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get registration statistics
     */
    suspend fun getRegistrationStats(userId: String? = null): Result<RegistrationStats> {
        return try {
            val plantsResult = databaseService.getUserPlants(userId)
            val insectsResult = databaseService.getUserInsects(userId)
            
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
                ultimoRegistro = maxOfOrNull(
                    plantas.maxOfOrNull { it.timestamp } ?: 0L,
                    insetos.maxOfOrNull { it.timestamp } ?: 0L
                ) ?: 0L
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
        return databaseService.isUserAuthenticated()
    }
    
    /**
     * Clear repository cache
     */
    fun clearCache() {
        _userPlants.value = emptyList()
        _userInsects.value = emptyList()
        _publicPlants.value = emptyList()
        _publicInsects.value = emptyList()
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