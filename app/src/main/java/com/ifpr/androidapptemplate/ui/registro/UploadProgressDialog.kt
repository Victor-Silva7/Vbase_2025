package com.ifpr.androidapptemplate.ui.registro

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.ifpr.androidapptemplate.databinding.DialogUploadProgressBinding
import com.ifpr.androidapptemplate.utils.ImageUploadManager
import com.ifpr.androidapptemplate.utils.UploadStatus

/**
 * Upload Progress Dialog for V Group - Manejo Verde
 * Shows detailed upload progress with the ability to cancel
 */
class UploadProgressDialog(
    context: Context,
    private val lifecycleOwner: LifecycleOwner
) : Dialog(context) {
    
    private lateinit var binding: DialogUploadProgressBinding
    private val uploadManager = ImageUploadManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DialogUploadProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupDialog()
        observeUploadProgress()
    }
    
    private fun setupDialog() {
        // Make dialog non-cancelable by touch
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        
        // Setup cancel button
        binding.buttonCancel.setOnClickListener {
            uploadManager.cancelUploads()
            dismiss()
        }
        
        // Initial state
        binding.progressBar.progress = 0
        binding.textProgress.text = "Preparando upload..."
        binding.textDetails.text = "Aguarde enquanto processamos suas imagens"
    }
    
    private fun observeUploadProgress() {
        uploadManager.uploadProgress.observe(lifecycleOwner) { progress ->
            binding.progressBar.progress = progress.progress
            binding.textProgress.text = "${progress.progress}%"
            binding.textDetails.text = progress.currentStep
        }
        
        uploadManager.uploadStatus.observe(lifecycleOwner) { status ->
            when (status) {
                UploadStatus.STARTING -> {
                    binding.textProgress.text = "Iniciando..."
                    binding.textDetails.text = "Preparando imagens para upload"
                    binding.buttonCancel.isEnabled = true
                }
                UploadStatus.COMPRESSING -> {
                    binding.textDetails.text = \"Comprimindo imagens para otimizar qualidade\"
                    binding.buttonCancel.isEnabled = true
                }
                UploadStatus.UPLOADING -> {
                    binding.textDetails.text = \"Enviando imagens para o servidor\"
                    binding.buttonCancel.isEnabled = true
                }
                UploadStatus.SUCCESS -> {
                    binding.progressBar.progress = 100
                    binding.textProgress.text = \"100%\"
                    binding.textDetails.text = \"Upload concluído com sucesso!\"
                    binding.buttonCancel.text = \"Fechar\"
                    binding.buttonCancel.isEnabled = true
                    // Auto dismiss after 1 second
                    binding.root.postDelayed({ dismiss() }, 1000)
                }
                UploadStatus.FAILED -> {
                    binding.textProgress.text = \"Erro\"
                    binding.textDetails.text = \"Falha no upload. Tente novamente.\"
                    binding.buttonCancel.text = \"Fechar\"
                    binding.buttonCancel.isEnabled = true
                }
                UploadStatus.CANCELLED -> {
                    dismiss()
                }
                else -> {
                    // IDLE state
                }
            }
        }
    }
}