package com.ifpr.androidapptemplate.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * Firebase Realtime Database Service for V Group - Manejo Verde
 * Handles all database operations with proper error handling and data validation
 */
class FirebaseDatabaseService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseDatabaseService? = null
        
        fun getInstance(): FirebaseDatabaseService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseDatabaseService().also { INSTANCE = it }
            }
        }
    }
    
    private val database: FirebaseDatabase = FirebaseConfig.getDatabase()
    private val auth: FirebaseAuth = FirebaseConfig.getAuth()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Database references
    private val plantasRef = database.reference.child(FirebaseConfig.DatabasePaths.PLANTAS)
    private val insetosRef = database.reference.child(FirebaseConfig.DatabasePaths.INSETOS)
    private val usuariosRef = database.reference.child(FirebaseConfig.DatabasePaths.USUARIOS)
    private val publicPlantasRef = database.reference.child(FirebaseConfig.DatabasePaths.PUBLIC_PLANTAS)
    private val publicInsetosRef = database.reference.child(FirebaseConfig.DatabasePaths.PUBLIC_INSETOS)
    private val estatisticasRef = database.reference.child(FirebaseConfig.DatabasePaths.ESTATISTICAS)
    
    /**
     * Save plant registration to Firebase
     */
    suspend fun savePlant(planta: Planta): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Update plant with current user info
            val updatedPlanta = planta.copy(
                userId = userId,
                userName = getCurrentUserName() ?: "Usuario",
                timestamp = System.currentTimeMillis()
            )
            
            val plantData = updatedPlanta.toFirebaseMap()
            
            // Save to user's personal collection
            val userPlantsRef = usuariosRef.child(userId).child("plantas")
            userPlantsRef.child(planta.id).setValue(plantData).await()
            
            // Save to public collection for community features
            if (planta.visibilidade == VisibilidadeRegistro.PUBLICO) {
                publicPlantasRef.child(planta.id).setValue(plantData).await()
            }
            
            // Update user statistics
            updateUserPlantStatistics(userId, incrementBy = 1)
            
            // Update global statistics
            updateGlobalStatistics("plantas", incrementBy = 1)
            
            Result.success(planta.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save insect registration to Firebase
     */
    suspend fun saveInsect(inseto: Inseto): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Update insect with current user info
            val updatedInseto = inseto.copy(
                userId = userId,
                userName = getCurrentUserName() ?: "Usuario",
                timestamp = System.currentTimeMillis()
            )
            
            val insectData = updatedInseto.toFirebaseMap()
            
            // Save to user's personal collection
            val userInsectsRef = usuariosRef.child(userId).child("insetos")
            userInsectsRef.child(inseto.id).setValue(insectData).await()
            
            // Save to public collection for community features
            if (inseto.visibilidade == VisibilidadeRegistro.PUBLICO) {
                publicInsetosRef.child(inseto.id).setValue(insectData).await()
            }
            
            // Update user statistics
            updateUserInsectStatistics(userId, incrementBy = 1)
            
            // Update global statistics
            updateGlobalStatistics("insetos", incrementBy = 1)
            
            Result.success(inseto.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update existing plant registration
     */
    suspend fun updatePlant(planta: Planta): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Verify ownership
            if (planta.userId != userId) {
                return Result.failure(Exception("Permission denied: Not the owner"))
            }
            
            val updatedPlanta = planta.copy(
                timestampUltimaEdicao = System.currentTimeMillis()
            )
            
            val plantData = updatedPlanta.toFirebaseMap()
            
            // Update in user's collection
            val userPlantsRef = usuariosRef.child(userId).child("plantas")
            userPlantsRef.child(planta.id).setValue(plantData).await()
            
            // Update in public collection if public
            if (planta.visibilidade == VisibilidadeRegistro.PUBLICO) {
                publicPlantasRef.child(planta.id).setValue(plantData).await()
            } else {
                // Remove from public if made private
                publicPlantasRef.child(planta.id).removeValue().await()
            }
            
            Result.success(planta.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update existing insect registration
     */
    suspend fun updateInsect(inseto: Inseto): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Verify ownership
            if (inseto.userId != userId) {
                return Result.failure(Exception("Permission denied: Not the owner"))
            }
            
            val updatedInseto = inseto.copy(
                timestampUltimaEdicao = System.currentTimeMillis()
            )
            
            val insectData = updatedInseto.toFirebaseMap()
            
            // Update in user's collection
            val userInsectsRef = usuariosRef.child(userId).child("insetos")
            userInsectsRef.child(inseto.id).setValue(insectData).await()
            
            // Update in public collection if public
            if (inseto.visibilidade == VisibilidadeRegistro.PUBLICO) {
                publicInsetosRef.child(inseto.id).setValue(insectData).await()
            } else {
                // Remove from public if made private
                publicInsetosRef.child(inseto.id).removeValue().await()
            }
            
            Result.success(inseto.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete plant registration
     */
    suspend fun deletePlant(plantId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Delete from user's collection
            val userPlantsRef = usuariosRef.child(userId).child("plantas")
            userPlantsRef.child(plantId).removeValue().await()
            
            // Delete from public collection
            publicPlantasRef.child(plantId).removeValue().await()
            
            // Update user statistics
            updateUserPlantStatistics(userId, incrementBy = -1)
            
            // Update global statistics
            updateGlobalStatistics("plantas", incrementBy = -1)
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete insect registration
     */
    suspend fun deleteInsect(insectId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Delete from user's collection
            val userInsectsRef = usuariosRef.child(userId).child("insetos")
            userInsectsRef.child(insectId).removeValue().await()
            
            // Delete from public collection
            publicInsetosRef.child(insectId).removeValue().await()
            
            // Update user statistics
            updateUserInsectStatistics(userId, incrementBy = -1)
            
            // Update global statistics
            updateGlobalStatistics("insetos", incrementBy = -1)
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's plants
     */
    suspend fun getUserPlants(userId: String? = null): Result<List<Planta>> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val userPlantsRef = usuariosRef.child(targetUserId).child("plantas")
            val snapshot = userPlantsRef.get().await()
            
            val plantas = mutableListOf<Planta>()
            snapshot.children.forEach { childSnapshot ->
                val plantData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                try {
                    val planta = Planta.fromFirebaseMap(plantData)
                    plantas.add(planta)
                } catch (e: Exception) {
                    // Skip invalid data entries
                }
            }
            
            Result.success(plantas.sortedByDescending { it.timestamp })
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's insects
     */
    suspend fun getUserInsects(userId: String? = null): Result<List<Inseto>> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val userInsectsRef = usuariosRef.child(targetUserId).child("insetos")
            val snapshot = userInsectsRef.get().await()
            
            val insetos = mutableListOf<Inseto>()
            snapshot.children.forEach { childSnapshot ->
                val insectData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                try {
                    val inseto = Inseto.fromFirebaseMap(insectData)
                    insetos.add(inseto)
                } catch (e: Exception) {
                    // Skip invalid data entries
                }
            }
            
            Result.success(insetos.sortedByDescending { it.timestamp })
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get public plants for community feed
     */
    suspend fun getPublicPlants(limit: Int = 20): Result<List<Planta>> {
        return try {
            val snapshot = publicPlantasRef.orderByChild("timestamp").limitToLast(limit).get().await()
            
            val plantas = mutableListOf<Planta>()
            snapshot.children.forEach { childSnapshot ->
                val plantData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                try {
                    val planta = Planta.fromFirebaseMap(plantData)
                    plantas.add(planta)
                } catch (e: Exception) {
                    // Skip invalid data entries
                }
            }
            
            Result.success(plantas.sortedByDescending { it.timestamp })
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get public insects for community feed
     */
    suspend fun getPublicInsects(limit: Int = 20): Result<List<Inseto>> {
        return try {
            val snapshot = publicInsetosRef.orderByChild("timestamp").limitToLast(limit).get().await()
            
            val insetos = mutableListOf<Inseto>()
            snapshot.children.forEach { childSnapshot ->
                val insectData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                try {
                    val inseto = Inseto.fromFirebaseMap(insectData)
                    insetos.add(inseto)
                } catch (e: Exception) {
                    // Skip invalid data entries
                }
            }
            
            Result.success(insetos.sortedByDescending { it.timestamp })
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save user profile
     */
    suspend fun saveUserProfile(usuario: Usuario): Result<String> {
        return try {
            val userId = usuario.id.ifEmpty { getCurrentUserId() ?: return Result.failure(Exception("User not authenticated")) }
            
            val updatedUsuario = usuario.copy(
                id = userId,
                ultimoLogin = System.currentTimeMillis()
            )
            
            val userData = updatedUsuario.toFirebaseMap()
            usuariosRef.child(userId).setValue(userData).await()
            
            Result.success(userId)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String? = null): Result<Usuario?> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val snapshot = usuariosRef.child(targetUserId).get().await()
            val userData = snapshot.value as? Map<String, Any?>
            
            val usuario = userData?.let { Usuario.fromFirebaseMap(it) }
            Result.success(usuario)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user plant statistics
     */
    private suspend fun updateUserPlantStatistics(userId: String, incrementBy: Int) {
        try {
            val userStatsRef = usuariosRef.child(userId).child("estatisticas")
            val currentStats = userStatsRef.get().await().value as? Map<String, Any?> ?: emptyMap()
            
            val currentPlantas = (currentStats["totalPlantas"] as? Number)?.toInt() ?: 0
            val newTotal = maxOf(0, currentPlantas + incrementBy)
            
            userStatsRef.child("totalPlantas").setValue(newTotal).await()
            
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
    
    /**
     * Update user insect statistics
     */
    private suspend fun updateUserInsectStatistics(userId: String, incrementBy: Int) {
        try {
            val userStatsRef = usuariosRef.child(userId).child("estatisticas")
            val currentStats = userStatsRef.get().await().value as? Map<String, Any?> ?: emptyMap()
            
            val currentInsetos = (currentStats["totalInsetos"] as? Number)?.toInt() ?: 0
            val newTotal = maxOf(0, currentInsetos + incrementBy)
            
            userStatsRef.child("totalInsetos").setValue(newTotal).await()
            
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
    
    /**
     * Update global statistics
     */
    private suspend fun updateGlobalStatistics(type: String, incrementBy: Int) {
        try {
            val globalStatsRef = estatisticasRef.child("global")
            val currentStats = globalStatsRef.get().await().value as? Map<String, Any?> ?: emptyMap()
            
            val currentCount = (currentStats[type] as? Number)?.toInt() ?: 0
            val newTotal = maxOf(0, currentCount + incrementBy)
            
            globalStatsRef.child(type).setValue(newTotal).await()
            globalStatsRef.child("ultimaAtualizacao").setValue(System.currentTimeMillis()).await()
            
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }
    
    /**
     * Get current authenticated user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get current authenticated user name
     */
    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Listen for real-time updates to user's plants
     */
    fun listenToUserPlants(userId: String? = null, callback: (List<Planta>) -> Unit): ValueEventListener? {
        val targetUserId = userId ?: getCurrentUserId() ?: return null
        
        val userPlantsRef = usuariosRef.child(targetUserId).child("plantas")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plantas = mutableListOf<Planta>()
                var errorCount = 0
                
                snapshot.children.forEach { childSnapshot ->
                    val plantData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                    try {
                        val planta = Planta.fromFirebaseMap(plantData)
                        plantas.add(planta)
                    } catch (e: Exception) {
                        errorCount++
                        Log.e("FirebaseDB", "Erro ao desserializar planta: ${e.message}")
                    }
                }
                
                Log.d("FirebaseDB", "Listener: Carregadas ${plantas.size} plantas de $targetUserId (${errorCount} erros)")
                callback(plantas.sortedByDescending { it.timestamp })
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDB", "Listener cancelado para plantas: ${error.message}")
            }
        }
        
        Log.d("FirebaseDB", "Attaching listener para: usuarios/$targetUserId/plantas")
        userPlantsRef.addValueEventListener(listener)
        return listener
    }
    
    /**
     * Listen for real-time updates to user's insects
     */
    fun listenToUserInsects(userId: String? = null, callback: (List<Inseto>) -> Unit): ValueEventListener? {
        val targetUserId = userId ?: getCurrentUserId() ?: return null
        
        val userInsectsRef = usuariosRef.child(targetUserId).child("insetos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val insetos = mutableListOf<Inseto>()
                var errorCount = 0
                
                snapshot.children.forEach { childSnapshot ->
                    val insectData = childSnapshot.value as? Map<String, Any?> ?: return@forEach
                    try {
                        val inseto = Inseto.fromFirebaseMap(insectData)
                        insetos.add(inseto)
                    } catch (e: Exception) {
                        errorCount++
                        Log.e("FirebaseDB", "Erro ao desserializar inseto: ${e.message}")
                    }
                }
                
                Log.d("FirebaseDB", "Listener: Carregados ${insetos.size} insetos de $targetUserId (${errorCount} erros)")
                callback(insetos.sortedByDescending { it.timestamp })
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDB", "Listener cancelado para insetos: ${error.message}")
            }
        }
        
        Log.d("FirebaseDB", "Attaching listener para: usuarios/$targetUserId/insetos")
        userInsectsRef.addValueEventListener(listener)
        return listener
    }
    
    /**
     * Remove event listener
     */
    fun removeListener(userId: String?, listener: ValueEventListener, isPlant: Boolean = true) {
        val targetUserId = userId ?: getCurrentUserId() ?: return
        
        val ref = if (isPlant) {
            usuariosRef.child(targetUserId).child("plantas")
        } else {
            usuariosRef.child(targetUserId).child("insetos")
        }
        
        ref.removeEventListener(listener)
    }
}