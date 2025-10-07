package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistroPlantaViewModel : ViewModel() {

    private val _selectedCategory = MutableLiveData<PlantHealthCategory?>()
    val selectedCategory: LiveData<PlantHealthCategory?> = _selectedCategory

    private val _selectedImages = MutableLiveData<MutableList<Uri>>()
    val selectedImages: LiveData<MutableList<Uri>> = _selectedImages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentPhotoPath: String? = null
    private var currentPhotoUri: Uri? = null
    private var appContext: Context? = null
    
    // Maximum number of images allowed
    private val maxImages = 5

    init {
        _selectedImages.value = mutableListOf()
        _isLoading.value = false
        _saveSuccess.value = false
        _errorMessage.value = ""
    }

    fun selectCategory(category: PlantHealthCategory) {
        _selectedCategory.value = category
    }
    
    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    fun addImageFromCamera() {
        currentPhotoUri?.let { uri ->
            addImageToList(uri)
            currentPhotoUri = null
            currentPhotoPath = null
        }
    }

    fun addImagesFromGallery(uris: List<Uri>) {
        val currentList = _selectedImages.value ?: mutableListOf()
        
        val availableSlots = maxImages - currentList.size
        if (availableSlots <= 0) {
            _errorMessage.value = "Máximo de $maxImages imagens permitidas"
            return
        }
        
        val imagesToAdd = uris.take(availableSlots)
        currentList.addAll(imagesToAdd)
        _selectedImages.value = currentList
        
        if (uris.size > availableSlots) {
            _errorMessage.value = "Adicionadas ${imagesToAdd.size} imagens. Limite de $maxImages atingido."
        } else {
            // Clear any previous error and show success message
            _errorMessage.value = "${imagesToAdd.size} imagem(ns) adicionada(s)"
            // Clear the message after a short delay
            clearErrorAfterDelay()
        }
    }
    
    private fun clearErrorAfterDelay() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _errorMessage.value = ""
        }, 2000)
    }

    private fun addImageToList(uri: Uri) {
        val currentList = _selectedImages.value ?: mutableListOf()
        
        if (currentList.size >= maxImages) {
            _errorMessage.value = "Máximo de $maxImages imagens permitidas"
            return
        }
        
        // Check if image already exists
        if (currentList.contains(uri)) {
            _errorMessage.value = "Esta imagem já foi adicionada"
            return
        }
        
        currentList.add(uri)
        _selectedImages.value = currentList
        
        // Show success feedback
        _errorMessage.value = "Imagem adicionada (${currentList.size}/$maxImages)"
        clearErrorAfterDelay()
    }

    fun removeImage(uri: Uri) {
        val currentList = _selectedImages.value ?: mutableListOf()
        if (currentList.remove(uri)) {
            _selectedImages.value = currentList
            _errorMessage.value = "Imagem removida (${currentList.size}/$maxImages)"
            clearErrorAfterDelay()
        }
    }

    fun createImageFile(): File? {
        return try {
            val context = appContext ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "PLANT_${timeStamp}_"
            
            // Use external files directory for pictures
            val storageDir = File(context.getExternalFilesDir(null), "Pictures")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            
            currentPhotoPath = imageFile.absolutePath
            currentPhotoUri = Uri.fromFile(imageFile)
            imageFile
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao criar arquivo de imagem: ${e.message}"
            null
        }
    }

    fun saveRegistration(nome: String, data: String, local: String, observacao: String) {
        _isLoading.value = true
        
        // Validate required fields
        if (nome.isEmpty() || data.isEmpty() || local.isEmpty()) {
            _errorMessage.value = "Campos obrigatórios não preenchidos"
            _isLoading.value = false
            return
        }
        
        if (_selectedCategory.value == null) {
            _errorMessage.value = "Selecione uma categoria"
            _isLoading.value = false
            return
        }
        
        // Create plant registration object
        val plantRegistration = PlantRegistration(
            id = generateId(),
            nome = nome.trim(),
            data = data,
            local = local.trim(),
            categoria = _selectedCategory.value!!,
            observacao = observacao.trim(),
            imagens = _selectedImages.value?.toList() ?: emptyList(),
            userId = getCurrentUserId(), // Would get from Firebase Auth
            timestamp = System.currentTimeMillis()
        )
        
        // TODO: Save to Firebase
        saveToFirebase(plantRegistration)
    }

    private fun saveToFirebase(registration: PlantRegistration) {
        // Simulate Firebase save operation
        try {
            // TODO: Implement actual Firebase save
            // 1. Upload images to Firebase Storage
            // 2. Save registration data to Realtime Database
            // 3. Update user statistics
            
            // For now, simulate success after delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                _isLoading.value = false
                _saveSuccess.value = true
            }, 2000)
            
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Erro ao salvar: ${e.message}"
        }
    }

    private fun generateId(): String {
        return "plant_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun getCurrentUserId(): String {
        // TODO: Get from Firebase Auth
        return "user_placeholder"
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}

// Data class for plant registration
data class PlantRegistration(
    val id: String,
    val nome: String,
    val data: String,
    val local: String,
    val categoria: PlantHealthCategory,
    val observacao: String,
    val imagens: List<Uri>,
    val userId: String,
    val timestamp: Long
)