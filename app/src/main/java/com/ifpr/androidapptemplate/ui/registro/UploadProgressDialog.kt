package com.ifpr.androidapptemplate.ui.registro

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.ifpr.androidapptemplate.databinding.DialogUploadProgressBinding
import com.ifpr.androidapptemplate.utils.ImageUploadManager

/**
 * Upload Progress Dialog for V Group - Manejo Verde
 * Shows detailed upload progress
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
        
        // Initial state
        binding.progressBar.progress = 0
        binding.textProgress.text = "Preparando upload..."
        binding.textDetails.text = "Aguarde enquanto processamos suas imagens"
    }
    
    private fun observeUploadProgress() {
        uploadManager.uploadProgress.observe(lifecycleOwner) { progress ->
            binding.progressBar.progress = progress.progress.toInt()
            binding.textProgress.text = "${progress.progress.toInt()}%"
            binding.textDetails.text = "Imagem ${progress.currentStep}"
        }
        
        uploadManager.uploadStatus.observe(lifecycleOwner) { status ->
            when (status) {
                ImageUploadManager.UploadStatus.STARTING -> {
                    binding.textProgress.text = "Iniciando..."
                    binding.textDetails.text = "Preparando imagens para upload"
                }
                ImageUploadManager.UploadStatus.UPLOADING -> {
                    binding.textDetails.text = "Enviando imagens para o servidor"
                }
                ImageUploadManager.UploadStatus.SUCCESS -> {
                    binding.progressBar.progress = 100
                    binding.textProgress.text = "100%"
                    binding.textDetails.text = "Upload concluído com sucesso!"
                    binding.root.postDelayed({ dismiss() }, 1500)
                }
                ImageUploadManager.UploadStatus.ERROR -> {
                    binding.textProgress.text = "Erro"
                    binding.textDetails.text = "Falha no upload. Tente novamente."
                    binding.root.postDelayed({ dismiss() }, 2000)
                }
            }
        }
    }
}