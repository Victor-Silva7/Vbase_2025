package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.firebase.FirebaseStorageManager
import com.ifpr.androidapptemplate.data.model.Inseto
import com.ifpr.androidapptemplate.data.model.InsectCategory
import com.ifpr.androidapptemplate.utils.ImageUploadManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistroInsetoViewModel : ViewModel() {

    private lateinit var context: Context
    
    private val _selectedCategory = MutableLiveData<InsectCategory?>()
    val selectedCategory: LiveData<InsectCategory?> = _selectedCategory
    
    private val _selectedImages = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val selectedImages: LiveData<MutableList<Uri>> = _selectedImages
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> = _errorMessage
    
    private var currentPhotoPath: String? = null
    
    // Firebase references
    private val database = FirebaseConfig.getDatabase()
    private val storageManager = FirebaseConfig.getStorageManager()
    private val imageUploadManager = ImageUploadManager.getInstance()
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    fun selectCategory(category: InsectCategory) {
        _selectedCategory.value = category
    }
    
    fun addImageFromCamera() {
        currentPhotoPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                addImageToList(uri)
            }
        }
    }
    
    fun addImagesFromGallery(uris: List<Uri>) {
        val currentList = _selectedImages.value ?: mutableListOf()
        val availableSlots = 5 - currentList.size
        
        if (availableSlots <= 0) {
            _errorMessage.value = "Máximo de 5 imagens permitidas"
            return
        }
        
        val imagesToAdd = uris.take(availableSlots)
        
        imagesToAdd.forEach { uri ->
            if (!currentList.contains(uri)) {
                currentList.add(uri)
            }
        }
        
        _selectedImages.value = currentList
        
        if (uris.size > availableSlots) {
            _errorMessage.value = "Apenas ${availableSlots} imagens foram adicionadas (limite de 5)"
        }
    }
    
    private fun addImageToList(uri: Uri) {
        val currentList = _selectedImages.value ?: mutableListOf()
        
        if (currentList.size >= 5) {
            _errorMessage.value = "Máximo de 5 imagens permitidas"
            return
        }
        
        if (!currentList.contains(uri)) {
            currentList.add(uri)
            _selectedImages.value = currentList
        } else {
            _errorMessage.value = "Esta imagem já foi adicionada"
        }
    }
    
    fun removeImage(uri: Uri) {
        val currentList = _selectedImages.value ?: mutableListOf()
        currentList.remove(uri)
        _selectedImages.value = currentList
    }
    
    fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "INSETO_${timeStamp}_"
            val storageDir = context.getExternalFilesDir(null)
            
            File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao criar arquivo para foto: ${e.message}"
            null
        }
    }
    
    fun saveRegistration(nome: String, data: String, local: String, observacao: String) {
        _isLoading.value = true
        
        val category = _selectedCategory.value
        if (category == null) {
            _errorMessage.value = "Selecione uma categoria para o inseto"
            _isLoading.value = false
            return
        }
        
        val registroId = database.reference.child("insetos").push().key
        if (registroId == null) {
            _errorMessage.value = "Erro ao gerar ID do registro"
            _isLoading.value = false
            return
        }
        
        // Create insect registration object
        val registro = hashMapOf(
            "id" to registroId,
            "nome" to nome,
            "data" to data,
            "local" to local,
            "categoria" to category.name,
            "observacao" to observacao,
            "timestamp" to System.currentTimeMillis(),
            "tipo" to "INSETO"
        )
        
        // Upload images first, then save registration
        val images = _selectedImages.value ?: mutableListOf()
        if (images.isNotEmpty()) {
            imageUploadManager.uploadInsectImages(
                context = context,
                insectId = registroId,
                imageUris = images,
                onSuccess = { downloadUrls ->
                    // Save registration with image URLs
                    saveRegistrationToDatabase(registro, downloadUrls)
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Erro ao fazer upload das imagens: ${exception.message}"
                }
            )
        } else {
            // Save registration without images
            saveRegistrationToDatabase(registro, emptyList())
        }
    }
    
    private fun saveRegistrationToDatabase(registro: HashMap<String, Any>, imageUrls: List<String>) {
        if (imageUrls.isNotEmpty()) {
            registro["imagens"] = imageUrls
        }
        
        val registroId = registro["id"] as String
        val userId = "user_placeholder" // TODO: Get from Firebase Auth
        
        // Save to both user's personal collection and public collection
        val userInsectsRef = database.reference.child(FirebaseConfig.DatabasePaths.userInsetos(userId))
        val publicInsectsRef = database.reference.child(FirebaseConfig.DatabasePaths.PUBLIC_INSETOS)
        
        // Save to user's collection
        userInsectsRef.child(registroId).setValue(registro)
            .addOnSuccessListener {
                // Save to public collection (for community features)
                publicInsectsRef.child(registroId).setValue(registro)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        _saveSuccess.value = true
                        clearForm()
                    }
                    .addOnFailureListener { exception ->
                        _isLoading.value = false
                        _errorMessage.value = "Erro ao salvar na coleção pública: ${exception.message}"
                    }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Erro ao salvar registro: ${exception.message}"
            }
    }
    
    private fun clearForm() {
        _selectedCategory.value = null
        _selectedImages.value = mutableListOf()
        currentPhotoPath = null
    }
}