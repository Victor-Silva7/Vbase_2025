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
import com.ifpr.androidapptemplate.databinding.ActivityRegistroInsetoBinding
import com.ifpr.androidapptemplate.ui.registro.InsectCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegistroInsetoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroInsetoBinding
    private lateinit var viewModel: RegistroInsetoViewModel
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
        
        binding = ActivityRegistroInsetoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[RegistroInsetoViewModel::class.java]
        viewModel.setContext(this)
        
        setupUI()
        setupObservers()
        setupClickListeners()
        setupImageRecyclerView()
        
        // Set current date as default
        binding.editTextData.setText(dateFormatter.format(selectedDate.time))
    }

    private fun setupUI() {
        // Auto-capitalize words (title case) for name, location and observations
        binding.editTextNome.addTextChangedListener(CapitalizeTextWatcher())
        binding.editTextLocal.addTextChangedListener(CapitalizeTextWatcher())
        binding.editTextObservacao.addTextChangedListener(CapitalizeTextWatcher())
        
        // Add real-time validation
        setupRealTimeValidation()
    }
    
    private fun setupRealTimeValidation() {
        // Nome validation
        binding.editTextNome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateNome(s.toString())
            }
        })
        
        // Local validation
        binding.editTextLocal.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateLocal(s.toString())
            }
        })
        
        // Observacao validation for insects
        binding.editTextObservacao.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateObservacaoForCategory(s.toString())
            }
        })
        
        // Focus change listeners for better UX
        binding.editTextNome.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateNome(binding.editTextNome.text.toString())
            }
        }
        
        binding.editTextLocal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateLocal(binding.editTextLocal.text.toString())
            }
        }
        
        binding.editTextObservacao.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateObservacaoForCategory(binding.editTextObservacao.text.toString())
            }
        }
    }
    
    private fun validateObservacaoForCategory(observacao: String) {
        val category = viewModel.selectedCategory.value ?: return
        val trimmedObservacao = observacao.trim()
        
        when (category) {
            InsectCategory.PEST -> {
                if (trimmedObservacao.length < 10) {
                    binding.inputLayoutObservacao.error = getString(R.string.error_observacao_required_pest)
                } else {
                    binding.inputLayoutObservacao.error = null
                }
            }
            InsectCategory.BENEFICIAL -> {
                if (trimmedObservacao.length < 5) {
                    binding.inputLayoutObservacao.error = getString(R.string.error_observacao_suggested_beneficial)
                } else {
                    binding.inputLayoutObservacao.error = null
                }
            }
            InsectCategory.NEUTRAL -> {
                // Less strict for neutral insects
                binding.inputLayoutObservacao.error = null
            }
        }
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
        binding.cardBenefico.setOnClickListener {
            viewModel.selectCategory(InsectCategory.BENEFICIAL)
            showCategorySelectionFeedback(getString(R.string.beneficial_category_selected))
            // Re-validate observation when category changes
            validateObservacaoForCategory(binding.editTextObservacao.text.toString())
        }
        
        binding.cardNeutro.setOnClickListener {
            viewModel.selectCategory(InsectCategory.NEUTRAL)
            showCategorySelectionFeedback(getString(R.string.neutral_category_selected))
            // Re-validate observation when category changes
            validateObservacaoForCategory(binding.editTextObservacao.text.toString())
        }
        
        binding.cardPraga.setOnClickListener {
            viewModel.selectCategory(InsectCategory.PEST)
            showCategorySelectionFeedback(getString(R.string.pest_category_selected))
            // Re-validate observation when category changes
            validateObservacaoForCategory(binding.editTextObservacao.text.toString())
        }
        
        // Tooltips for category explanation with animation feedback
        binding.iconBenefico.setOnClickListener {
            animateIconClick(binding.iconBenefico)
            showInsectTooltip(InsectCategory.BENEFICIAL)
        }
        
        binding.iconNeutro.setOnClickListener {
            animateIconClick(binding.iconNeutro)
            showInsectTooltip(InsectCategory.NEUTRAL)
        }
        
        binding.iconPraga.setOnClickListener {
            animateIconClick(binding.iconPraga)
            showInsectTooltip(InsectCategory.PEST)
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
            layoutManager = LinearLayoutManager(this@RegistroInsetoActivity, 
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

    private fun updateCategorySelection(category: InsectCategory?) {
        // Reset all cards with animation
        animateCardSelection(binding.cardBenefico, false)
        animateCardSelection(binding.cardNeutro, false)
        animateCardSelection(binding.cardPraga, false)
        
        // Highlight selected card with animation
        when (category) {
            InsectCategory.BENEFICIAL -> {
                animateCardSelection(binding.cardBenefico, true)
                binding.cardBenefico.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                binding.cardBenefico.strokeWidth = 4
            }
            InsectCategory.NEUTRAL -> {
                animateCardSelection(binding.cardNeutro, true)
                binding.cardNeutro.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                binding.cardNeutro.strokeWidth = 4
            }
            InsectCategory.PEST -> {
                animateCardSelection(binding.cardPraga, true)
                binding.cardPraga.strokeColor = getColor(com.ifpr.androidapptemplate.R.color.vgroup_green)
                binding.cardPraga.strokeWidth = 4
            }
            null -> {
                binding.cardBenefico.strokeColor = getColor(android.R.color.transparent)
                binding.cardNeutro.strokeColor = getColor(android.R.color.transparent)
                binding.cardPraga.strokeColor = getColor(android.R.color.transparent)
                binding.cardBenefico.strokeWidth = 0
                binding.cardNeutro.strokeWidth = 0
                binding.cardPraga.strokeWidth = 0
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

    private fun showInsectTooltip(category: InsectCategory) {
        val (title, message, iconRes) = when (category) {
            InsectCategory.BENEFICIAL -> Triple(
                getString(R.string.insect_beneficial_title),
                getString(R.string.insect_beneficial_description),
                com.ifpr.androidapptemplate.R.drawable.ic_benefico_24dp
            )
            InsectCategory.NEUTRAL -> Triple(
                getString(R.string.insect_neutral_title),
                getString(R.string.insect_neutral_description),
                com.ifpr.androidapptemplate.R.drawable.ic_neutro_24dp
            )
            InsectCategory.PEST -> Triple(
                getString(R.string.insect_pest_title),
                getString(R.string.insect_pest_description),
                com.ifpr.androidapptemplate.R.drawable.ic_praga_24dp
            )
        }
        
        // Create and show custom tooltip dialog
        showCustomTooltip(title, message, iconRes)
    }
    
    private fun showCustomTooltip(title: String, message: String, iconRes: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setIcon(iconRes)
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
            // Show validation summary for insects
            showValidationSummary(nome, data, local, observacao)
            viewModel.saveRegistration(nome, data, local, observacao)
        } else {
            // Provide helpful guidance on what needs to be fixed
            showValidationGuidance()
        }
    }
    
    private fun showValidationSummary(nome: String, data: String, local: String, observacao: String) {
        val category = viewModel.selectedCategory.value
        val images = viewModel.selectedImages.value?.size ?: 0
        
        val summary = when (category) {
            InsectCategory.BENEFICIAL -> "Registrando inseto benéfico: $nome"
            InsectCategory.NEUTRAL -> "Registrando inseto neutro: $nome"
            InsectCategory.PEST -> "Registrando inseto praga: $nome"
            null -> "Registrando inseto: $nome"
        }
        
        Toast.makeText(this, summary, Toast.LENGTH_SHORT).show()
    }
    
    private fun showValidationGuidance() {
        val errors = mutableListOf<String>()
        
        if (binding.inputLayoutNome.error != null) {
            errors.add("• Verifique o nome do inseto")
        }
        if (binding.inputLayoutData.error != null) {
            errors.add("• Verifique a data")
        }
        if (binding.inputLayoutLocal.error != null) {
            errors.add("• Verifique o local")
        }
        if (viewModel.selectedCategory.value == null) {
            errors.add("• Selecione a categoria do inseto")
        }
        if (binding.inputLayoutObservacao.error != null) {
            errors.add("• Complete a observação")
        }
        
        val images = viewModel.selectedImages.value?.size ?: 0
        if (images == 0) {
            errors.add("• Adicione pelo menos 1 imagem")
        }
        
        if (errors.isNotEmpty()) {
            val message = "Por favor, corrija:\n" + errors.joinToString("\n")
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun validateForm(nome: String, data: String, local: String): Boolean {
        var isValid = true
        
        // Validate nome
        if (!validateNome(nome)) {
            isValid = false
        }
        
        // Validate data
        if (!validateData(data)) {
            isValid = false
        }
        
        // Validate local
        if (!validateLocal(local)) {
            isValid = false
        }
        
        // Validate category
        if (!validateCategory()) {
            isValid = false
        }
        
        // Validate images for insects
        if (!validateInsectImages()) {
            isValid = false
        }
        
        return isValid
    }
    
    private fun validateInsectImages(): Boolean {
        val images = viewModel.selectedImages.value ?: mutableListOf()
        val category = viewModel.selectedCategory.value
        
        when {
            images.isEmpty() -> {
                showImageValidationError(getString(R.string.error_images_required_insect))
                return false
            }
            category == InsectCategory.PEST && images.size < 2 -> {
                showImageValidationError(getString(R.string.error_images_minimum_pest))
                return false
            }
            category == InsectCategory.BENEFICIAL && images.size < 1 -> {
                showImageValidationError(getString(R.string.error_images_minimum_beneficial))
                return false
            }
            else -> {
                return true
            }
        }
    }
    
    private fun showImageValidationError(message: String) {
        // Highlight image section with error indication
        binding.textImagensTitle.setTextColor(getColor(android.R.color.holo_red_light))
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Clear error after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            binding.textImagensTitle.setTextColor(getColor(com.ifpr.androidapptemplate.R.color.vgroup_green))
        }, 3000)
    }
    
    private fun validateNome(nome: String): Boolean {
        val trimmedNome = nome.trim()
        
        when {
            trimmedNome.isEmpty() -> {
                binding.inputLayoutNome.error = getString(R.string.error_nome_required_insect)
                return false
            }
            trimmedNome.length < 2 -> {
                binding.inputLayoutNome.error = getString(R.string.error_nome_too_short_insect)
                return false
            }
            trimmedNome.length > 50 -> {
                binding.inputLayoutNome.error = getString(R.string.error_nome_too_long_insect)
                return false
            }
            !isValidInsectName(trimmedNome) -> {
                binding.inputLayoutNome.error = getString(R.string.error_nome_invalid_chars_insect)
                return false
            }
            !hasValidInsectPattern(trimmedNome) -> {
                binding.inputLayoutNome.error = getString(R.string.error_nome_pattern_insect)
                return false
            }
            else -> {
                binding.inputLayoutNome.error = null
                return true
            }
        }
    }
    
    private fun isValidInsectName(nome: String): Boolean {
        // Allow letters, spaces, hyphens, and parentheses (common in insect names)
        return nome.matches(Regex("^[a-zA-ZÀ-ſ\\s\\-()]+$"))
    }
    
    private fun hasValidInsectPattern(nome: String): Boolean {
        // Check for common insect naming patterns
        val words = nome.split(" ").filter { it.isNotEmpty() }
        
        // Must have at least one word
        if (words.isEmpty()) return false
        
        // Check for suspicious patterns that don't match insect names
        val suspiciousPatterns = listOf(
            Regex("^\\d+"), // Starting with numbers
            Regex("[!@#$%^&*+=<>?/|\\\\]"), // Special characters
            Regex("(.)\\1{4,}") // Same character repeated 5+ times
        )
        
        return !suspiciousPatterns.any { pattern -> 
            nome.contains(pattern)
        }
    }
    
    private fun validateData(data: String): Boolean {
        when {
            data.trim().isEmpty() -> {
                binding.inputLayoutData.error = getString(R.string.error_data_required)
                return false
            }
            !isValidDateFormat(data) -> {
                binding.inputLayoutData.error = getString(R.string.error_data_invalid_format)
                return false
            }
            isFutureDate(data) -> {
                binding.inputLayoutData.error = getString(R.string.error_data_future)
                return false
            }
            else -> {
                binding.inputLayoutData.error = null
                return true
            }
        }
    }
    
    private fun validateLocal(local: String): Boolean {
        val trimmedLocal = local.trim()
        
        when {
            trimmedLocal.isEmpty() -> {
                binding.inputLayoutLocal.error = getString(R.string.error_local_required)
                return false
            }
            trimmedLocal.length < 2 -> {
                binding.inputLayoutLocal.error = getString(R.string.error_local_too_short)
                return false
            }
            trimmedLocal.length > 30 -> {
                binding.inputLayoutLocal.error = getString(R.string.error_local_too_long)
                return false
            }
            !trimmedLocal.matches(Regex("^[a-zA-ZÀ-ſ\\s]+$")) -> {
                binding.inputLayoutLocal.error = getString(R.string.error_local_invalid_chars)
                return false
            }
            else -> {
                binding.inputLayoutLocal.error = null
                return true
            }
        }
    }
    
    private fun validateCategory(): Boolean {
        val category = viewModel.selectedCategory.value
        return if (category == null) {
            showCategoryError()
            false
        } else {
            // Additional validation based on category type
            if (!validateCategorySpecificRules(category)) {
                false
            } else {
                clearCategoryError()
                true
            }
        }
    }
    
    private fun validateCategorySpecificRules(category: InsectCategory): Boolean {
        val observacao = binding.editTextObservacao.text.toString().trim()
        
        // For pest insects, observation is more important
        if (category == InsectCategory.PEST && observacao.length < 10) {
            binding.inputLayoutObservacao.error = getString(R.string.error_observacao_required_pest)
            return false
        }
        
        // For beneficial insects, encourage detailed observation
        if (category == InsectCategory.BENEFICIAL && observacao.length < 5) {
            binding.inputLayoutObservacao.error = getString(R.string.error_observacao_suggested_beneficial)
            // Don't return false, just show warning
        }
        
        binding.inputLayoutObservacao.error = null
        return true
    }
    
    private fun isValidDateFormat(date: String): Boolean {
        return try {
            dateFormatter.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isFutureDate(date: String): Boolean {
        return try {
            val inputDate = dateFormatter.parse(date)
            val today = Calendar.getInstance().time
            inputDate?.after(today) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun showCategoryError() {
        // Animate all category cards to indicate error
        binding.cardBenefico.strokeColor = getColor(android.R.color.holo_red_light)
        binding.cardNeutro.strokeColor = getColor(android.R.color.holo_red_light)
        binding.cardPraga.strokeColor = getColor(android.R.color.holo_red_light)
        binding.cardBenefico.strokeWidth = 2
        binding.cardNeutro.strokeWidth = 2
        binding.cardPraga.strokeWidth = 2
        
        Toast.makeText(this, getString(R.string.error_category_required_insect), Toast.LENGTH_SHORT).show()
        
        // Clear error after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            clearCategoryError()
        }, 3000)
    }
    
    private fun clearCategoryError() {
        if (viewModel.selectedCategory.value == null) {
            binding.cardBenefico.strokeColor = getColor(android.R.color.transparent)
            binding.cardNeutro.strokeColor = getColor(android.R.color.transparent)
            binding.cardPraga.strokeColor = getColor(android.R.color.transparent)
            binding.cardBenefico.strokeWidth = 0
            binding.cardNeutro.strokeWidth = 0
            binding.cardPraga.strokeWidth = 0
        }
    }
}
