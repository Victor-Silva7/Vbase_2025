package com.victor.vgroup.ui.registro

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.victor.vgroup.R
import com.victor.vgroup.databinding.ActivityRegistroInsetoBinding
import com.victor.vgroup.data.model.InsectCategory
import java.text.SimpleDateFormat
import java.util.*

class RegistroInsetoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroInsetoBinding
    private lateinit var viewModel: RegistroInsetoViewModel
    private lateinit var imageAdapter: SelectedImageAdapter
    
    // Helper classes
    private lateinit var formValidator: FormValidator
    private lateinit var imagePickerManager: ImagePickerManager
    private lateinit var animationHelper: UIAnimationHelper
    
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRegistroInsetoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeHelpers()
        initializeViewModel()
        setupUI()
        setupObservers()
        setupClickListeners()
        setupImageRecyclerView()
        
        binding.editTextData.setText(dateFormatter.format(selectedDate.time))
    }
    
    private fun initializeHelpers() {
        formValidator = FormValidator(this)
        animationHelper = UIAnimationHelper(this)
        imagePickerManager = ImagePickerManager(
            activity = this,
            onImagesSelected = { uris -> viewModel.addImagesFromGallery(uris) },
            onImageFromCamera = { viewModel.addImageFromCamera() }
        )
    }
    
    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this)[RegistroInsetoViewModel::class.java]
        viewModel.setContext(this)
        viewModel.setImagePickerManager(imagePickerManager)
    }

    private fun setupUI() {
        // Real-time validation
        setupRealTimeValidation()
    }
    
    private fun setupRealTimeValidation() {
        binding.editTextNome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.inputLayoutNome.error = formValidator.validateNome(s.toString())
            }
        })
        
        binding.editTextLocal.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.inputLayoutLocal.error = formValidator.validateLocal(s.toString())
            }
        })
        
        binding.editTextData.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.inputLayoutData.error = formValidator.validateData(s.toString())
            }
        })
        
        // Observacao validation based on category
        binding.editTextObservacao.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateObservacaoForCategory(s.toString())
            }
        })
    }
    
    private fun validateObservacaoForCategory(observacao: String) {
        val category = viewModel.selectedCategory.value
        val trimmedObservacao = observacao.trim()
        
        binding.inputLayoutObservacao.error = when (category) {
            InsectCategory.PEST -> {
                if (trimmedObservacao.isEmpty()) 
                    "Descreva brevemente a praga observada" 
                else null
            }
            InsectCategory.BENEFICIAL -> {
                if (trimmedObservacao.isEmpty()) 
                    "Descreva brevemente o benefício observado" 
                else null
            }
            InsectCategory.NEUTRAL -> {
                if (trimmedObservacao.isEmpty()) 
                    "Adicione pelo menos uma observação" 
                else null
            }
            null -> null
        }
    }

    private fun setupObservers() {
        viewModel.selectedCategory.observe(this) { category ->
            updateCategorySelection(category)
            // Re-validate observation when category changes
            validateObservacaoForCategory(binding.editTextObservacao.text.toString())
        }
        
        viewModel.selectedImages.observe(this) { images ->
            imageAdapter.updateImages(images)
            binding.recyclerViewImages.visibility = if (images.isNotEmpty()) 
                android.view.View.VISIBLE else android.view.View.GONE
            
            binding.textImageCounter.text = "${images.size}/1"
            
            val color = when {
                images.size == 1 -> getColor(R.color.vgroup_green)
                else -> getColor(R.color.vgroup_text_secondary)
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
        binding.editTextData.setOnClickListener { showDatePicker() }
        binding.buttonCamera.setOnClickListener { imagePickerManager.takePhoto() }
        binding.buttonGaleria.setOnClickListener { imagePickerManager.selectFromGallery() }
        
        binding.cardBenefico.setOnClickListener {
            viewModel.selectCategory(InsectCategory.BENEFICIAL)
            animationHelper.showCategorySelectionFeedback("Inseto Benéfico selecionado")
        }
        
        binding.cardPraga.setOnClickListener {
            viewModel.selectCategory(InsectCategory.PEST)
            animationHelper.showCategorySelectionFeedback("Praga selecionada")
        }
        
        binding.cardNeutro.setOnClickListener {
            viewModel.selectCategory(InsectCategory.NEUTRAL)
            animationHelper.showCategorySelectionFeedback("Inseto Neutro selecionado")
        }
        
        binding.iconBenefico.setOnClickListener {
            animationHelper.animateIconClick(binding.iconBenefico)
            showInsectTooltip(InsectCategory.BENEFICIAL)
        }
        
        binding.iconPraga.setOnClickListener {
            animationHelper.animateIconClick(binding.iconPraga)
            showInsectTooltip(InsectCategory.PEST)
        }
        
        binding.iconNeutro.setOnClickListener {
            animationHelper.animateIconClick(binding.iconNeutro)
            showInsectTooltip(InsectCategory.NEUTRAL)
        }
        
        binding.buttonSalvar.setOnClickListener { saveRegistration() }
    }
    
    private fun setupImageRecyclerView() {
        imageAdapter = SelectedImageAdapter(this) { imageUri ->
            viewModel.removeImage(imageUri)
        }
        
        binding.recyclerViewImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(
                this@RegistroInsetoActivity, 
                LinearLayoutManager.HORIZONTAL, 
                false
            )
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                binding.editTextData.setText(dateFormatter.format(selectedDate.time))
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    private fun updateCategorySelection(category: InsectCategory?) {
        animationHelper.animateCardSelection(binding.cardBenefico, category == InsectCategory.BENEFICIAL)
        animationHelper.animateCardSelection(binding.cardPraga, category == InsectCategory.PEST)
        animationHelper.animateCardSelection(binding.cardNeutro, category == InsectCategory.NEUTRAL)
    }

    private fun showInsectTooltip(category: InsectCategory) {
        val (title, message) = when (category) {
            InsectCategory.BENEFICIAL -> Pair(
                "Inseto Benéfico",
                "Insetos que ajudam no controle de pragas ou na polinização, como joaninhas e abelhas."
            )
            InsectCategory.PEST -> Pair(
                "Praga",
                "Insetos que causam danos às plantas, como pulgões, lagartas e cochonilhas."
            )
            InsectCategory.NEUTRAL -> Pair(
                "Inseto Neutro",
                "Insetos que não causam danos significativos nem trazem benefícios diretos às plantas."
            )
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Entendi") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveRegistration() {
        val nome = binding.editTextNome.text.toString()
        val data = binding.editTextData.text.toString()
        val local = binding.editTextLocal.text.toString()
        val observacao = binding.editTextObservacao.text.toString()
        val categorySelected = viewModel.selectedCategory.value != null
        
        // Validate all fields
        binding.inputLayoutNome.error = formValidator.validateNome(nome)
        binding.inputLayoutData.error = formValidator.validateData(data)
        binding.inputLayoutLocal.error = formValidator.validateLocal(local)
        validateObservacaoForCategory(observacao)
        
        if (!categorySelected) {
            animationHelper.showCategoryError(
                listOf(binding.cardBenefico, binding.cardPraga, binding.cardNeutro),
                getString(R.string.error_category_required)
            )
        }
        
        // Check if form is valid
        if (!formValidator.validateForm(nome, data, local, categorySelected)) {
            Toast.makeText(this, "Por favor, corrija os erros no formulário", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Additional validation for observation based on category
        if (binding.inputLayoutObservacao.error != null) {
            animationHelper.shakeView(binding.inputLayoutObservacao)
            Toast.makeText(this, "Por favor, complete a observação conforme a categoria", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save registration
        viewModel.saveRegistration(
            nome = nome.trim(),
            data = data.trim(),
            local = local.trim(),
            observacao = observacao.trim()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePickerManager.onRequestPermissionsResult(requestCode, grantResults)
    }
}
