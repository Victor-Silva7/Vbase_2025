package com.ifpr.androidapptemplate.ui.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.ActivityRegistroPlantaBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistroPlantaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroPlantaBinding
    private lateinit var viewModel: RegistroPlantaViewModel
    private lateinit var imageAdapter: SelectedImageAdapter
    
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Activity Result Launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.addImageFromCamera()
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImagesFromGallery(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRegistroPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[RegistroPlantaViewModel::class.java]
        
        setupUI()
        setupObservers()
        setupClickListeners()
        setupImageRecyclerView()
        
        // Set current date as default
        binding.editTextData.setText(dateFormatter.format(selectedDate.time))
    }

    private fun setupUI() {
        // Auto-capitalize first character for name and location
        binding.editTextNome.addTextChangedListener(CapitalizeTextWatcher())
        binding.editTextLocal.addTextChangedListener(CapitalizeTextWatcher())
        binding.editTextObservacao.addTextChangedListener(CapitalizeTextWatcher())
    }

    private fun setupObservers() {
        viewModel.selectedCategory.observe(this) { category ->
            updateCategorySelection(category)
        }
        
        viewModel.selectedImages.observe(this) { images ->
            imageAdapter.updateImages(images)
            binding.recyclerViewImages.visibility = if (images.isNotEmpty()) 
                android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.buttonSalvar.isEnabled = !isLoading
            binding.buttonSalvar.text = if (isLoading) "Salvando..." else "Salvar Registro"
        }
        
        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.iconBack.setOnClickListener {
            finish()
        }
        
        binding.editTextData.setOnClickListener {
            showDatePicker()
        }
        
        binding.buttonCamera.setOnClickListener {
            takePhoto()
        }
        
        binding.buttonGaleria.setOnClickListener {
            selectFromGallery()
        }
        
        // Category selection with tooltips
        binding.cardSaudavel.setOnClickListener {
            viewModel.selectCategory(PlantHealthCategory.HEALTHY)
        }
        
        binding.cardDoente.setOnClickListener {
            viewModel.selectCategory(PlantHealthCategory.SICK)
        }
        
        // Tooltips for category explanation
        binding.iconSaudavel.setOnClickListener {
            showHealthTooltip(true)
        }
        
        binding.iconDoente.setOnClickListener {
            showHealthTooltip(false)
        }
        
        binding.buttonSalvar.setOnClickListener {
            saveRegistration()
        }
    }
    
    private fun setupImageRecyclerView() {
        imageAdapter = SelectedImageAdapter { imageUri ->
            viewModel.removeImage(imageUri)
        }
        
        binding.recyclerViewImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@RegistroPlantaActivity, 
                LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                binding.editTextData.setText(dateFormatter.format(selectedDate.time))
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun takePhoto() {
        val photoFile = viewModel.createImageFile()
        if (photoFile != null) {
            val photoURI = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoURI)
        }
    }

    private fun selectFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun updateCategorySelection(category: PlantHealthCategory?) {
        // Reset both cards
        binding.cardSaudavel.strokeColor = getColor(android.R.color.transparent)
        binding.cardDoente.strokeColor = getColor(android.R.color.transparent)
        
        // Highlight selected card
        when (category) {
            PlantHealthCategory.HEALTHY -> {
                binding.cardSaudavel.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
            }
            PlantHealthCategory.SICK -> {
                binding.cardDoente.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
            }
            null -> { /* No selection */ }
        }
    }

    private fun showHealthTooltip(isHealthy: Boolean) {
        val message = if (isHealthy) {
            "Planta Saudável: Apresenta folhas verdes, sem manchas, pragas ou sinais de doença. Crescimento normal e vigoroso."
        } else {
            "Planta Doente: Apresenta sinais de doença como folhas amareladas, manchas, pragas, murcha ou crescimento anormal."
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveRegistration() {
        val nome = binding.editTextNome.text.toString().trim()
        val data = binding.editTextData.text.toString().trim()
        val local = binding.editTextLocal.text.toString().trim()
        val observacao = binding.editTextObservacao.text.toString().trim()
        
        if (validateForm(nome, data, local)) {
            viewModel.saveRegistration(nome, data, local, observacao)
        }
    }

    private fun validateForm(nome: String, data: String, local: String): Boolean {
        when {
            nome.isEmpty() -> {
                binding.inputLayoutNome.error = "Nome é obrigatório"
                return false
            }
            data.isEmpty() -> {
                binding.inputLayoutData.error = "Data é obrigatória"
                return false
            }
            local.isEmpty() -> {
                binding.inputLayoutLocal.error = "Local é obrigatório"
                return false
            }
            viewModel.selectedCategory.value == null -> {
                Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                // Clear any previous errors
                binding.inputLayoutNome.error = null
                binding.inputLayoutData.error = null
                binding.inputLayoutLocal.error = null
                return true
            }
        }
    }
}

// Enum for plant health categories
enum class PlantHealthCategory {
    HEALTHY, SICK
}

// Text watcher for auto-capitalization
class CapitalizeTextWatcher : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    
    override fun afterTextChanged(s: android.text.Editable?) {
        if (s != null && s.isNotEmpty() && s[0].isLowerCase()) {
            s.replace(0, 1, s[0].uppercaseChar().toString())
        }
    }
}