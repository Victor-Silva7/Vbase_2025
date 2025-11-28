package com.victor.vgroup.ui.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.victor.vgroup.R
import kotlinx.coroutines.launch
import java.io.InputStream

class AiLogicFragment : Fragment() {

    private lateinit var promptInput: EditText
    private lateinit var resultText: TextView
    private lateinit var generateButton: Button
    private lateinit var model: GenerativeModel
    
    // Adicione variáveis e botão de imagem (logo abaixo da variável model: GenerativeModel):
    private lateinit var imageButton: Button
    private var imageUri: Uri? = null
    private lateinit var itemImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_logic, container, false)

        promptInput = view.findViewById(R.id.prompt_input)
        resultText = view.findViewById(R.id.result_text)
        generateButton = view.findViewById(R.id.btn_generate)
        
        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
            
        // Vincule o botão e configure o seletor de imagem (adicione logo ACIMA de generateButton.setOnClickListener):
        imageButton = view.findViewById(R.id.btn_select_image)
        itemImageView = view.findViewById(R.id.bitmapImageView)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this).load(imageUri).into(itemImageView)
                resultText.text = "Imagem selecionada. Pronto para gerar."
            } else {
                resultText.text = "Nenhuma imagem selecionada."
            }
        }

        imageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Atualize generateButton.setOnClickListener, substitua pela implementação:
        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isNotEmpty() && imageUri != null) {
                resultText.text = "Aguardando resposta..."
                try {
                    val bitmap = uriToBitmap(imageUri!!)
                    if (bitmap != null) {
                        generateFromPrompt(prompt, bitmap)
                    } else {
                        resultText.text = "Erro ao carregar a imagem."
                    }
                } catch (e: Exception) {
                    resultText.text = "Erro ao processar imagem: ${e.message}"
                }
            } else if (imageUri == null) {
                resultText.text = "Selecione uma imagem."
            } else {
                resultText.text = "Digite um prompt para continuar."
            }
        }

        return view
    }

    // Implemente generateFromPromptAndImage:
    private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                // Provide a prompt that includes the image specified above and text
                val promptImage = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = model.generateContent(promptImage)
                resultText.text = response.text ?: "Nenhuma resposta recebida."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta: ${e.message}"
            }
        }
    }

    // Função para converter URI em Bitmap
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}