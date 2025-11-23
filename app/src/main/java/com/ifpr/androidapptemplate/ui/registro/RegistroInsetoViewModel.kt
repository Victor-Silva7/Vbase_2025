package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.ifpr.androidapptemplate.data.firebase.FirebaseConfig
import com.ifpr.androidapptemplate.data.firebase.FirebaseStorageManager
import com.ifpr.androidapptemplate.data.firebase.FirebaseDatabaseService
import com.ifpr.androidapptemplate.data.model.Inseto
import com.ifpr.androidapptemplate.data.model.InsectCategory
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
    private var imagePickerManager: ImagePickerManager? = null
    
    // Firebase references
    private val database = FirebaseConfig.getDatabase()
    private val storageManager = FirebaseConfig.getStorageManager()
    private val databaseService = FirebaseConfig.getDatabaseService()
    
    // Serviço simplificado de rede social
    private val socialService = com.ifpr.androidapptemplate.data.firebase.SimpleSocialService.getInstance()
    private val imageUploadManager = ImageUploadManager.getInstance()
    private val repository = RegistroRepository.getInstance()
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    fun setImagePickerManager(manager: ImagePickerManager) {
        imagePickerManager = manager
    }
    
    fun selectCategory(category: InsectCategory) {
        _selectedCategory.value = category
    }
    
    fun addImageFromCamera() {
        val uri = imagePickerManager?.getCurrentPhotoUri()
        uri?.let {
            addImageToList(it)
        } ?: run {
            // Fallback to old method
            currentPhotoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val fallbackUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    addImageToList(fallbackUri)
                }
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
        
        // Create insect registration object using new data model
        val registro = Inseto(
            id = Inseto.generateId(),
            nome = nome,
            data = data,
            dataTimestamp = convertDateToTimestamp(data),
            local = local,
            categoria = category,
            observacao = observacao,
            imagens = emptyList(), // Will be populated after image upload
            userId = getCurrentUserId(),
            userName = getCurrentUserName(),
            timestamp = System.currentTimeMillis(),
            tipo = "INSETO"
        )
        
        // Upload images first, then save registration
        val images = _selectedImages.value ?: mutableListOf()
        android.util.Log.d("RegistroInsetoVM", "📸 Número de imagens: ${images.size}")
        
        if (images.isNotEmpty()) {
            android.util.Log.d("RegistroInsetoVM", "📤 Iniciando upload de ${images.size} imagens...")
            imageUploadManager.uploadInsectImages(
                context = context,
                insectId = registro.id,
                imageUris = images,
                onSuccess = { imageIds ->
                    android.util.Log.d("RegistroInsetoVM", "✅ Upload concluído! ${imageIds.size} imagens salvas")
                    // NÃO salvar os IDs no registro - as imagens ficam em nó separado
                    // Mas passar os IDs para controle interno
                    val updatedRegistro = registro.copy(imagens = imageIds)
                    saveRegistrationToDatabase(updatedRegistro, hasUploadedImages = true)
                },
                onFailure = { exception ->
                    android.util.Log.e("RegistroInsetoVM", "❌ ERRO no upload: ${exception.message}", exception)
                    viewModelScope.launch(Dispatchers.Main) {
                        _isLoading.value = false
                        _errorMessage.value = "Erro ao fazer upload das imagens: ${exception.message}"
                    }
                }
            )
        } else {
            android.util.Log.d("RegistroInsetoVM", "⚠️ Nenhuma imagem selecionada, salvando sem imagens")
            // Save registration without images
            saveRegistrationToDatabase(registro, hasUploadedImages = false)
        }
    }
    
    private fun saveRegistrationToDatabase(registration: Inseto, hasUploadedImages: Boolean = false) {
        // Use coroutines for async database operations
        viewModelScope.launch {
            try {
                android.util.Log.d("RegistroInsetoVM", "🔥 SALVANDO INSETO: ${registration.id}")
                android.util.Log.d("RegistroInsetoVM", "🔥 USER ID: ${registration.userId}")
                android.util.Log.d("RegistroInsetoVM", "🔥 USER NAME: ${registration.userName}")
                android.util.Log.d("RegistroInsetoVM", "🔥 Tem imagens: $hasUploadedImages")
                android.util.Log.d("RegistroInsetoVM", "🔥 IDs das imagens: ${registration.imagens}")
                
                // ✅ CORREÇÃO: Salvar COM a lista de imagens
                val result = databaseService.saveInsect(registration)
                
                result.onSuccess { insectId ->
                    android.util.Log.d("RegistroInsetoVM", "✅ INSETO SALVO COM SUCESSO! ID: $insectId")
                    
                    // Criar postagem após salvar o registro (com try-catch)
                    try {
                        criarPostagemDoRegistro(registration, hasUploadedImages)
                    } catch (e: Exception) {
                        android.util.Log.e("RegistroInsetoVM", "⚠️ Erro ao criar postagem (não crítico): ${e.message}", e)
                    }
                    
                    // Force refresh repository to load newly saved registration
                    try {
                        repository.getUserInsects(forceRefresh = true)
                    } catch (e: Exception) {
                        android.util.Log.e("RegistroInsetoVM", "⚠️ Erro ao atualizar repositório: ${e.message}", e)
                    }
                    
                    android.util.Log.d("RegistroInsetoVM", "✅ SALVAMENTO COMPLETO!")
                    
                    // IMPORTANTE: Garantir que o sucesso seja notificado
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _saveSuccess.value = true
                    }
                    clearForm()
                }.onFailure { exception ->
                    android.util.Log.e("RegistroInsetoVM", "❌ ERRO AO SALVAR: ${exception.message}", exception)
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
                android.util.Log.e("RegistroInsetoVM", "❌ ERRO INESPERADO: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _errorMessage.value = "❌ Erro inesperado: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Cria uma PostagemFeed a partir de um registro de Inseto
     * A postagem é automaticamente compartilhada no feed público
     */
    private fun criarPostagemDoRegistro(registration: Inseto, hasUploadedImages: Boolean = false) {
        // Usar GlobalScope para não cancelar quando sair da tela
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                // Buscar foto do usuário do Firebase Auth
                val currentUser = FirebaseConfig.getAuth().currentUser
                val userPhotoUrl = currentUser?.photoUrl?.toString() ?: ""
                
                // Buscar a primeira imagem Base64 do inseto (se houver)
                var imageBase64 = ""
                if (hasUploadedImages && registration.imagens.isNotEmpty()) {
                    android.util.Log.d("RegistroInsetoVM", "🖼️ Inseto tem ${registration.imagens.size} imagens")
                    android.util.Log.d("RegistroInsetoVM", "🖼️ Primeira imagem ID: ${registration.imagens.firstOrNull()}")
                    android.util.Log.d("RegistroInsetoVM", "🖼️ Buscando primeira imagem do inseto no Firebase...")
                    
                    val realtimeManager = FirebaseConfig.getRealtimeDatabaseImageManager()
                    
                    // Tentar buscar a imagem com retry (máximo 3 tentativas)
                    var tentativas = 0
                    while (tentativas < 3 && imageBase64.isEmpty()) {
                        if (tentativas > 0) {
                            android.util.Log.d("RegistroInsetoVM", "⏳ Tentativa ${tentativas + 1}/3...")
                            kotlinx.coroutines.delay(500)
                        }
                        
                        android.util.Log.d("RegistroInsetoVM", "📞 CHAMANDO getFirstInsectImage(${registration.id})")
                        val imageResult = realtimeManager.getFirstInsectImage(registration.id)
                        android.util.Log.d("RegistroInsetoVM", "📦 Result recebido: success=${imageResult.isSuccess}, failure=${imageResult.isFailure}")
                        
                        imageResult.onSuccess { base64 ->
                            android.util.Log.d("RegistroInsetoVM", "📦 onSuccess chamado: isEmpty=${base64.isEmpty()}, length=${base64.length}")
                            if (base64.isNotEmpty()) {
                                imageBase64 = base64
                                android.util.Log.d("RegistroInsetoVM", "✅ Imagem Base64 recuperada (${base64.length} chars)")
                            }
                        }.onFailure { exception ->
                            android.util.Log.e("RegistroInsetoVM", "⚠️ Erro na tentativa ${tentativas + 1}: ${exception.message}")
                        }
                        tentativas++
                    }
                    
                    if (imageBase64.isEmpty()) {
                        android.util.Log.e("RegistroInsetoVM", "❌ Não foi possível recuperar imagem após 3 tentativas")
                    }
                } else {
                    android.util.Log.d("RegistroInsetoVM", "⚠️ Registro sem imagens (hasUploadedImages=$hasUploadedImages)")
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
                    tipo = TipoPostagem.INSETO,
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
                    android.util.Log.d("RegistroInsetoVM", "✅ Postagem criada com sucesso: ${postagem.id}")
                }.onFailure { exception ->
                    android.util.Log.e("RegistroInsetoVM", "❌ Erro ao criar postagem", exception)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("RegistroInsetoVM", "Erro ao criar postagem", e)
            }
        }
    }
    
    private fun convertDateToTimestamp(dateString: String): Long {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    private fun getCurrentUserId(): String {
        return FirebaseConfig.getAuth().currentUser?.uid ?: "user_placeholder"
    }
    
    private fun getCurrentUserName(): String {
        return FirebaseConfig.getAuth().currentUser?.displayName ?: "Usuario Anonimo"
    }
    
    private fun clearForm() {
        _selectedCategory.value = null
        _selectedImages.value = mutableListOf()
        currentPhotoPath = null
    }
}
