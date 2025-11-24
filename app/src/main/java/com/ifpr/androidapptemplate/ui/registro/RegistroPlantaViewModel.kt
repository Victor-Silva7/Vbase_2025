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
import com.ifpr.androidapptemplate.data.model.PostagemFeed
import com.ifpr.androidapptemplate.data.model.TipoPostagem
import com.ifpr.androidapptemplate.data.model.UsuarioPostagem
import com.ifpr.androidapptemplate.data.repository.RegistroRepository
import com.ifpr.androidapptemplate.utils.ImageUploadManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
    private var imagePickerManager: ImagePickerManager? = null
    
    // Firebase services
    private val database = FirebaseConfig.getDatabase()
    private val storageManager = FirebaseConfig.getStorageManager()
    private val databaseService = FirebaseConfig.getDatabaseService()
    private val imageUploadManager = ImageUploadManager.getInstance()
    private val repository = RegistroRepository.getInstance()
    
    // Serviço simplificado de rede social
    private val socialService = com.ifpr.androidapptemplate.data.firebase.SimpleSocialService.getInstance()
    
    // Maximum number of images allowed
    private val maxImages = 1

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
    
    fun setImagePickerManager(manager: ImagePickerManager) {
        imagePickerManager = manager
    }

    fun addImageFromCamera() {
        val uri = imagePickerManager?.getCurrentPhotoUri()
        uri?.let {
            addImageToList(it)
        } ?: run {
            // Fallback to old method
            currentPhotoUri?.let { oldUri ->
                addImageToList(oldUri)
                currentPhotoUri = null
                currentPhotoPath = null
            }
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
        android.util.Log.d("RegistroPlantaVM", "🔥 saveRegistration() CHAMADO!")
        android.util.Log.d("RegistroPlantaVM", "🔥 nome: $nome")
        android.util.Log.d("RegistroPlantaVM", "🔥 data: $data")
        android.util.Log.d("RegistroPlantaVM", "🔥 local: $local")
        
        _isLoading.value = true
        
        // Validate required fields
        if (nome.isEmpty() || data.isEmpty() || local.isEmpty()) {
            android.util.Log.e("RegistroPlantaVM", "❌ Validação falhou: campos vazios")
            _errorMessage.value = "DEBUG: Campos obrigatórios vazios"
            _isLoading.value = false
            return
        }
        
        if (_selectedCategory.value == null) {
            android.util.Log.e("RegistroPlantaVM", "❌ Validação falhou: categoria não selecionada")
            _errorMessage.value = "DEBUG: Selecione uma categoria (Saudável ou Doente)"
            _isLoading.value = false
            return
        }
        
        android.util.Log.d("RegistroPlantaVM", "✅ Validações OK, criando objeto Planta...")
        
        // Create plant registration object using new data model
        val plantRegistration = Planta(
            id = Planta.generateId(),
            nome = nome.trim(),
            data = data,
            dataTimestamp = convertDateToTimestamp(data),
            local = local.trim(),
            categoria = _selectedCategory.value!!,
            observacao = observacao.trim(),
            imagens = emptyList(), // Will be populated after image upload
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
            android.util.Log.d("RegistroPlantaVM", "🔥 saveToFirebase() INICIADO")
            val plantId = registration.id
            val imageUris = _selectedImages.value ?: emptyList()
            val context = appContext ?: throw IllegalStateException("Context not set")
            
            android.util.Log.d("RegistroPlantaVM", "📸 Número de imagens: ${imageUris.size}")
            
            // ✅ CORRIGIDO: Salvar metadados PRIMEIRO, depois as imagens
            if (imageUris.isNotEmpty()) {
                android.util.Log.d("RegistroPlantaVM", "📤 Passo 1: Salvando metadados da planta...")
                
                // Salvar metadados primeiro (sem imagens)
                viewModelScope.launch {
                    val result = databaseService.savePlant(registration.copy(imagens = emptyList()))
                    
                    result.onSuccess {
                        android.util.Log.d("RegistroPlantaVM", "✅ Metadados salvos! Passo 2: Uploading ${imageUris.size} imagens...")
                        
                        // Agora salvar as imagens Base64
                        imageUploadManager.uploadPlantImages(
                            context = context,
                            plantId = plantId,
                            imageUris = imageUris,
                            onSuccess = { imageIds ->
                                android.util.Log.d("RegistroPlantaVM", "✅ Upload concluído! ${imageIds.size} imagens salvas")
                                android.util.Log.d("RegistroPlantaVM", "✅ IDs: $imageIds")
                                
                                // ✅ CORRIGIDO: Atualizar Firebase com os IDs das imagens
                                val updatedRegistration = registration.copy(imagens = imageIds)
                                viewModelScope.launch {
                                    android.util.Log.d("RegistroPlantaVM", "📤 Passo 3: Atualizando Firebase com imagensIds...")
                                    val updateResult = databaseService.savePlant(updatedRegistration)
                                    
                                    updateResult.onSuccess {
                                        android.util.Log.d("RegistroPlantaVM", "✅ Firebase atualizado com imagensIds!")
                                        // Agora finalizar
                                        finalizarSalvamento(updatedRegistration, hasUploadedImages = true)
                                    }.onFailure { ex ->
                                        android.util.Log.e("RegistroPlantaVM", "❌ Erro ao atualizar IDs: ${ex.message}", ex)
                                        // Mesmo com erro, tentar finalizar
                                        finalizarSalvamento(updatedRegistration, hasUploadedImages = true)
                                    }
                                }
                            },
                            onFailure = { exception ->
                                android.util.Log.e("RegistroPlantaVM", "❌ ERRO no upload: ${exception.message}", exception)
                                viewModelScope.launch(Dispatchers.Main) {
                                    _isLoading.value = false
                                    _errorMessage.value = "Erro ao fazer upload das imagens: ${exception.message}"
                                }
                            }
                        )
                    }.onFailure { exception ->
                        android.util.Log.e("RegistroPlantaVM", "❌ ERRO ao salvar metadados: ${exception.message}", exception)
                        viewModelScope.launch(Dispatchers.Main) {
                            _isLoading.value = false
                            _errorMessage.value = "Erro ao salvar: ${exception.message}"
                        }
                    }
                }
            } else {
                android.util.Log.d("RegistroPlantaVM", "⚠️ Nenhuma imagem selecionada, salvando sem imagens")
                // Save registration without images
                saveRegistrationToDatabase(registration, hasUploadedImages = false)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("RegistroPlantaVM", "❌ ERRO FATAL em saveToFirebase: ${e.message}", e)
            _isLoading.value = false
            _errorMessage.value = "Erro ao salvar: ${e.message}"
        }
    }
    
    /**
     * Finaliza o salvamento criando postagem e atualizando UI
     */
    private fun finalizarSalvamento(registration: Planta, hasUploadedImages: Boolean) {
        viewModelScope.launch {
            try {
                android.util.Log.d("RegistroPlantaVM", "✅ Finalizando salvamento...")
                
                // Criar postagem no feed
                try {
                    criarPostagemDoRegistro(registration, hasUploadedImages)
                } catch (e: Exception) {
                    android.util.Log.e("RegistroPlantaVM", "⚠️ Erro ao criar postagem (não crítico): ${e.message}", e)
                }
                
                // Force refresh repository
                android.util.Log.d("RegistroPlantaVM", "🔄 Forçando refresh do repositório...")
                try {
                    repository.getUserPlants(forceRefresh = true)
                } catch (e: Exception) {
                    android.util.Log.e("RegistroPlantaVM", "⚠️ Erro ao atualizar repositório: ${e.message}", e)
                }
                
                android.util.Log.d("RegistroPlantaVM", "✅ SALVAMENTO COMPLETO!")
                
                // Notificar sucesso
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _saveSuccess.value = true
                }
                clearFormData()
                
            } catch (e: Exception) {
                android.util.Log.e("RegistroPlantaVM", "❌ Erro ao finalizar: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "Erro ao finalizar salvamento: ${e.message}"
                }
            }
        }
    }
    
    private fun saveRegistrationToDatabase(registration: Planta, hasUploadedImages: Boolean = false) {
        // Use coroutines for async database operations
        viewModelScope.launch {
            try {
                android.util.Log.d("RegistroPlantaVM", "🔥 SALVANDO PLANTA: ${registration.id}")
                android.util.Log.d("RegistroPlantaVM", "🔥 USER ID: ${registration.userId}")
                android.util.Log.d("RegistroPlantaVM", "🔥 USER NAME: ${registration.userName}")
                android.util.Log.d("RegistroPlantaVM", "🔥 NOME: ${registration.nome}")
                android.util.Log.d("RegistroPlantaVM", "🔥 LOCAL: ${registration.local}")
                android.util.Log.d("RegistroPlantaVM", "🔥 CATEGORIA: ${registration.categoria}")
                android.util.Log.d("RegistroPlantaVM", "🔥 IMAGENS: ${registration.imagens.size}")
                android.util.Log.d("RegistroPlantaVM", "🔥 IDs das imagens: ${registration.imagens}")
                android.util.Log.d("RegistroPlantaVM", "🔥 Tem imagens: $hasUploadedImages")
                
                // ✅ CORRIGIDO: Manter os IDs das imagens para não sobrescrever o nó com Base64
                val result = databaseService.savePlant(registration)
                
                result.onSuccess { plantId ->
                    android.util.Log.d("RegistroPlantaVM", "✅ PLANTA SALVA COM SUCESSO! ID: $plantId")
                    
                    // Criar postagem após salvar o registro (com try-catch)
                    try {
                        criarPostagemDoRegistro(registration, hasUploadedImages)
                    } catch (e: Exception) {
                        android.util.Log.e("RegistroPlantaVM", "⚠️ Erro ao criar postagem (não crítico): ${e.message}", e)
                    }
                    
                    // Force refresh repository to load newly saved registration
                    android.util.Log.d("RegistroPlantaVM", "🔄 Forçando refresh do repositório...")
                    try {
                        repository.getUserPlants(forceRefresh = true)
                    } catch (e: Exception) {
                        android.util.Log.e("RegistroPlantaVM", "⚠️ Erro ao atualizar repositório: ${e.message}", e)
                    }

                    android.util.Log.d("RegistroPlantaVM", "✅ SALVAMENTO COMPLETO!")
                    
                    // IMPORTANTE: Garantir que o sucesso seja notificado
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _saveSuccess.value = true
                    }
                    clearFormData()
                }.onFailure { exception ->
                    android.util.Log.e("RegistroPlantaVM", "❌ ERRO AO SALVAR: ${exception.message}", exception)
                    exception.printStackTrace()
                    
                    // Garantir que erro seja exibido na UI thread
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        
                        val errorMsg = when {
                            exception.message?.contains("auth") == true || 
                            exception.message?.contains("authenticated") == true -> 
                                "❌ Erro de autenticação: Faça login novamente"
                            exception.message?.contains("permission") == true || 
                            exception.message?.contains("denied") == true -> 
                                "❌ Sem permissão: Verifique as regras do Firebase"
                            exception.message?.contains("network") == true -> 
                                "❌ Erro de conexão: Verifique sua internet"
                            else -> 
                                "❌ Erro ao salvar: ${exception.message}"
                        }
                        
                        _errorMessage.value = errorMsg
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("RegistroPlantaVM", "❌ ERRO INESPERADO: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "❌ Erro inesperado: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Cria uma PostagemFeed a partir de um registro de Planta
     * A postagem é automaticamente compartilhada no feed público
     */
    private fun criarPostagemDoRegistro(registration: Planta, hasUploadedImages: Boolean = false) {
        // Usar GlobalScope para não cancelar quando sair da tela
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                // Buscar foto do usuário do Firebase Auth
                val currentUser = FirebaseConfig.getAuth().currentUser
                val userPhotoUrl = currentUser?.photoUrl?.toString() ?: ""
                
                // Buscar a primeira imagem Base64 da planta (se houver)
                var imageBase64 = ""
                if (hasUploadedImages && registration.imagens.isNotEmpty()) {
                    android.util.Log.d("RegistroPlantaVM", "🖼️ Planta tem ${registration.imagens.size} imagens")
                    android.util.Log.d("RegistroPlantaVM", "🖼️ Primeira imagem ID: ${registration.imagens.firstOrNull()}")
                    android.util.Log.d("RegistroPlantaVM", "🖼️ Buscando primeira imagem da planta no Firebase...")
                    
                    val realtimeManager = FirebaseConfig.getRealtimeDatabaseImageManager()
                    
                    // Tentar buscar a imagem com retry (máximo 3 tentativas)
                    var tentativas = 0
                    while (tentativas < 3 && imageBase64.isEmpty()) {
                        if (tentativas > 0) {
                            android.util.Log.d("RegistroPlantaVM", "⏳ Tentativa ${tentativas + 1}/3...")
                            kotlinx.coroutines.delay(500)
                        }
                        
                        android.util.Log.d("RegistroPlantaVM", "📞 CHAMANDO getFirstPlantImage(${registration.id})")
                        val imageResult = realtimeManager.getFirstPlantImage(registration.id)
                        android.util.Log.d("RegistroPlantaVM", "📦 Result recebido: success=${imageResult.isSuccess}, failure=${imageResult.isFailure}")
                        
                        imageResult.onSuccess { base64 ->
                            android.util.Log.d("RegistroPlantaVM", "📦 onSuccess chamado: isEmpty=${base64.isEmpty()}, length=${base64.length}")
                            if (base64.isNotEmpty()) {
                                imageBase64 = base64
                                android.util.Log.d("RegistroPlantaVM", "✅ Imagem Base64 recuperada (${base64.length} chars)")
                            }
                        }.onFailure { exception ->
                            android.util.Log.e("RegistroPlantaVM", "⚠️ Erro na tentativa ${tentativas + 1}: ${exception.message}")
                        }
                        tentativas++
                    }
                    
                    if (imageBase64.isEmpty()) {
                        android.util.Log.e("RegistroPlantaVM", "❌ Não foi possível recuperar imagem após 3 tentativas")
                    }
                } else {
                    android.util.Log.d("RegistroPlantaVM", "⚠️ Registro sem imagens (hasUploadedImages=$hasUploadedImages)")
                }
                
                val usuario = UsuarioPostagem(
                    id = registration.userId,
                    nome = registration.userName,
                    nomeExibicao = registration.userName,
                    avatarUrl = userPhotoUrl,
                    isVerificado = false,
                    totalRegistros = 0,
                    totalCurtidas = 0
                )
                
                val postagem = PostagemFeed(
                    id = registration.id, // Usar mesmo ID para rastreamento
                    tipo = TipoPostagem.PLANTA,
                    usuario = usuario,
                    titulo = registration.nome,
                    descricao = registration.observacao,
                    imageUrl = imageBase64, // Usar Base64 em vez de ID
                    localizacao = "", // Localização removida para privacidade
                    dataPostagem = registration.timestamp
                )
                
                // Salvar postagem no feed público usando serviço simplificado
                val result = socialService.salvarPostagem(postagem)
                
                result.onSuccess {
                    android.util.Log.d("RegistroPlantaVM", "✅ Postagem criada com sucesso: ${postagem.id}")
                }.onFailure { exception ->
                    android.util.Log.e("RegistroPlantaVM", "❌ Erro ao criar postagem", exception)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("RegistroPlantaVM", "Erro ao criar postagem", e)
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
        return FirebaseConfig.getAuth().currentUser?.uid ?: "user_placeholder"
    }
    
    private fun getCurrentUserName(): String {
        return FirebaseConfig.getAuth().currentUser?.displayName ?: "Usuario Anonimo"
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