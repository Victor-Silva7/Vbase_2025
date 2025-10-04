package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.firebase.FirebaseStorageManager
import com.ifpr.androidapptemplate.data.firebase.FirebaseDatabaseService
import com.ifpr.androidapptemplate.data.model.Planta
import com.ifpr.androidapptemplate.data.model.PlantHealthCategory
import com.ifpr.androidapptemplate.utils.ImageUploadManager
import kotlinx.coroutines.launch
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
    
    // Firebase services
    private val database = FirebaseConfig.getDatabase()
    private val storageManager = FirebaseConfig.getStorageManager()
    private val databaseService = FirebaseConfig.getDatabaseService()
    private val imageUploadManager = ImageUploadManager.getInstance()
    
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
        
        // Create plant registration object using new data model
        val plantRegistration = Planta(
            id = Planta.generateId(),
            nome = nome.trim(),
            data = data,
            dataTimestamp = convertDateToTimestamp(data),
            local = local.trim(),
            categoria = _selectedCategory.value!!,
            observacao = observacao.trim(),
            imagens = _selectedImages.value?.map { it.toString() } ?: emptyList(),
            userId = getCurrentUserId(),
            userName = getCurrentUserName(),
            timestamp = System.currentTimeMillis(),
            tipo = "PLANTA"
        )
        
        // TODO: Save to Firebase
        saveToFirebase(plantRegistration)
    }

    private fun saveToFirebase(registration: Planta) {
        try {
            val plantId = registration.id
            val imageUris = registration.imagens.map { Uri.parse(it) }
            val context = appContext ?: throw IllegalStateException("Context not set")
            
            // Use enhanced ImageUploadManager for better compression and progress tracking
            if (imageUris.isNotEmpty()) {
                imageUploadManager.uploadPlantImages(
                    context = context,
                    plantId = plantId,
                    imageUris = imageUris,
                    onSuccess = { downloadUrls ->
                        // Save registration with image URLs
                        val updatedRegistration = registration.copy(imagens = downloadUrls)
                        saveRegistrationToDatabase(updatedRegistration)
                    },
                    onFailure = { exception ->
                        _isLoading.value = false
                        _errorMessage.value = "Erro ao fazer upload das imagens: ${exception.message}"
                    }
                )
            } else {
                // Save registration without images
                saveRegistrationToDatabase(registration)
            }
            
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Erro ao salvar: ${e.message}"
        }
    }
    
    private fun saveRegistrationToDatabase(registration: Planta) {
        // Use coroutines for async database operations
        viewModelScope.launch {
            try {
                val result = databaseService.savePlant(registration)
                
                result.onSuccess { plantId ->
                    _isLoading.value = false
                    _saveSuccess.value = true
                    clearFormData()
                }.onFailure { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Erro ao salvar registro: ${exception.message}"
                }
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro inesperado: ${e.message}"
            }
        }
    }
    
    private fun clearFormData() {
        _selectedCategory.value = null
        _selectedImages.value = mutableListOf()
        currentPhotoPath = null
        currentPhotoUri = null
    }

    private fun getCurrentUserId(): String {
        // TODO: Get from Firebase Auth
        return "user_placeholder"
    }
    
    private fun getCurrentUserName(): String {
        // TODO: Get from Firebase Auth
        return "Usuario Anonimo"
    }
    
    private fun convertDateToTimestamp(dateString: String): Long {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}