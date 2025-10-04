package com.ifpr.androidapptemplate.ui.registro

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.R
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
    
    // Permission request codes
    private val CAMERA_PERMISSION_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST = 101
    
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
        viewModel.setContext(this)
        
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
            
            // Update image counter
            binding.textImageCounter.text = "${images.size}/5"
            
            // Update counter colors based on limit
            val color = when {
                images.size == 5 -> getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                images.size >= 3 -> getColor(android.R.color.holo_orange_light)
                else -> getColor(com.ifpr.androidapptemplate.R.color.vgroup_text_secondary)
            }
            binding.textImageCounter.setTextColor(color)
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
            showCategorySelectionFeedback(getString(R.string.healthy_category_selected))
        }
        
        binding.cardDoente.setOnClickListener {
            viewModel.selectCategory(PlantHealthCategory.SICK)
            showCategorySelectionFeedback(getString(R.string.sick_category_selected))
        }
        
        // Tooltips for category explanation with animation feedback
        binding.iconSaudavel.setOnClickListener {
            animateIconClick(binding.iconSaudavel)
            showHealthTooltip(true)
        }
        
        binding.iconDoente.setOnClickListener {
            animateIconClick(binding.iconDoente)
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
        if (checkCameraPermission()) {
            val photoFile = viewModel.createImageFile()
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    photoFile
                )
                cameraLauncher.launch(photoURI)
            }
        } else {
            requestCameraPermission()
        }
    }

    private fun selectFromGallery() {
        if (checkStoragePermission()) {
            galleryLauncher.launch("image/*")
        } else {
            requestStoragePermission()
        }
    }

    private fun updateCategorySelection(category: PlantHealthCategory?) {
        // Reset both cards with animation
        animateCardSelection(binding.cardSaudavel, false)
        animateCardSelection(binding.cardDoente, false)
        
        // Highlight selected card with animation
        when (category) {
            PlantHealthCategory.HEALTHY -> {
                animateCardSelection(binding.cardSaudavel, true)
                binding.cardSaudavel.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                binding.cardSaudavel.strokeWidth = 4
            }
            PlantHealthCategory.SICK -> {
                animateCardSelection(binding.cardDoente, true)
                binding.cardDoente.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                binding.cardDoente.strokeWidth = 4
            }
            null -> {
                binding.cardSaudavel.strokeColor = getColor(android.R.color.transparent)
                binding.cardDoente.strokeColor = getColor(android.R.color.transparent)
                binding.cardSaudavel.strokeWidth = 0
                binding.cardDoente.strokeWidth = 0
            }
        }
    }
    
    private fun animateCardSelection(card: com.google.android.material.card.MaterialCardView, isSelected: Boolean) {
        val scaleValue = if (isSelected) 1.05f else 1.0f
        val elevationValue = if (isSelected) 8f else 2f
        
        card.animate()
            .scaleX(scaleValue)
            .scaleY(scaleValue)
            .setDuration(200)
            .start()
            
        card.cardElevation = elevationValue
    }
    
    private fun animateIconClick(icon: android.widget.ImageView) {
        icon.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                icon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    private fun showCategorySelectionFeedback(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    // Permission handling functions
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }
    
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_needed), Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFromGallery()
                } else {
                    Toast.makeText(this, getString(R.string.storage_permission_needed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showHealthTooltip(isHealthy: Boolean) {
        val title = getString(if (isHealthy) R.string.plant_healthy_title else R.string.plant_sick_title)
        val message = getString(if (isHealthy) R.string.plant_healthy_description else R.string.plant_sick_description)
        
        // Create and show custom tooltip dialog
        showCustomTooltip(title, message, isHealthy)
    }
    
    private fun showCustomTooltip(title: String, message: String, isHealthy: Boolean) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(if (isHealthy) com.ifpr.androidapptemplate.R.drawable.ic_saudavel_24dp else com.ifpr.androidapptemplate.R.drawable.ic_doente_24dp)
        builder.setPositiveButton(getString(R.string.understood)) { dialog, _ ->
            dialog.dismiss()
        }
        
        val dialog = builder.create()
        dialog.show()
        
        // Customize dialog appearance
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
        )
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