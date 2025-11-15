# 🔧 TROUBLESHOOTING - Firebase AI Logic

## Problemas Comuns e Soluções

---

## ❌ Erro: "Model not found: gemini-2.5-flash"

### Causa
API Gemini não ativada no Firebase Console

### Solução
1. Acesse: https://console.firebase.google.com
2. Projeto: `teste20251-ab84a`
3. Build → AI → Ativar API Gemini
4. Aguarde 1-2 minutos
5. Reinicie o app

---

## ❌ Erro: "Authentication required"

### Causa
Credenciais do Firebase não configuradas corretamente

### Solução
```
1. Verifique google-services.json
2. Confirme projeto Firebase está correto
3. Verify SHA-1 fingerprint no Firebase Console
4. Fazer Build > Clean Build
5. Rebuild project
```

### Verificar SHA-1
```bash
# No Terminal do Android Studio:
./gradlew signingReport
```

---

## ❌ Erro: "Permission denied: READ_EXTERNAL_STORAGE"

### Causa
Permissão de leitura não foi concedida em runtime

### Solução
```kotlin
// Solicitar permissão em runtime (Android 6.0+)
if (ContextCompat.checkSelfPermission(context,
    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
}
```

### Ou no Manifest (já configurado):
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

---

## ❌ Erro: "Image not loading from gallery"

### Causa
URI da imagem inválida ou Glide não conseguiu carregar

### Solução
```kotlin
// Adicionar tratamento de erro
Glide.with(this)
    .load(imageUri)
    .placeholder(R.drawable.ic_image_placeholder)
    .error(R.drawable.ic_error_24dp)
    .into(itemImageView)
```

---

## ❌ Erro: "Response is null or empty"

### Causa
1. Modelo retornou vazio
2. Timeout na requisição
3. Limite de quota atingido

### Solução
```kotlin
val response = model.generateContent(promptImage)
if (response.text.isNullOrEmpty()) {
    resultText.text = "Erro: Resposta vazia. Tente novamente."
} else {
    resultText.text = response.text
}
```

### Verificar Quotas
```
Firebase Console → AI → Quotas
```

---

## ❌ Erro: "Timeout - Operation timed out"

### Causa
Rede lenta ou servidor sobrecarregado

### Solução
```kotlin
private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
    lifecycleScope.launch(Dispatchers.Main) {
        try {
            withTimeoutOrNull(30000) { // 30 segundos de timeout
                val promptImage = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = model.generateContent(promptImage)
                resultText.text = response.text ?: "Sem resposta"
            } ?: run {
                resultText.text = "Timeout. Tente novamente."
            }
        } catch (e: Exception) {
            resultText.text = "Erro: ${e.message}"
        }
    }
}
```

---

## ❌ Erro: "FAB não aparece no Feed"

### Causa
FAB não foi adicionado ao XML ou binding não está funcionando

### Solução
1. Verificar `fragment_feed.xml`:
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_ai"
    ... />
```

2. Verificar `FeedFragment.kt`:
```kotlin
binding.fabAi.setOnClickListener {
    val intent = Intent(requireContext(), AiLogicActivity::class.java)
    startActivity(intent)
}
```

3. Se ainda não aparecer:
```kotlin
// Force rebuild binding
_binding = FragmentFeedBinding.bind(view)
```

---

## ❌ Erro: "Model not responding to images"

### Causa
Bitmap não está sendo enviado corretamente

### Solução
```kotlin
private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
    lifecycleScope.launch {
        try {
            // Verificar se bitmap é válido
            if (bitmap.width == 0 || bitmap.height == 0) {
                resultText.text = "Erro: Imagem inválida"
                return@launch
            }
            
            val promptImage = content {
                image(bitmap)
                text(prompt)
            }
            val response = model.generateContent(promptImage)
            resultText.text = response.text ?: "Sem resposta"
        } catch (e: Exception) {
            resultText.text = "Erro: ${e.message}"
        }
    }
}
```

---

## ⚠️ Aviso: "Slow Response"

### Causa
Normal do Gemini (2-5 segundos)

### Solução
```kotlin
// Mostrar indicador de loading
resultText.text = "⏳ Processando... Aguarde 2-5 segundos"

// Depois que receber resposta
resultText.text = response.text
```

---

## ⚠️ Aviso: "High memory usage"

### Causa
Imagem muito grande está sendo processada

### Solução
```kotlin
// Comprimir bitmap antes de enviar
fun compressBitmap(bitmap: Bitmap, quality: Int = 70): Bitmap {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    val data = baos.toByteArray()
    return BitmapFactory.decodeByteArray(data, 0, data.size)
}

// Usar bitmap comprimido
val compressedBitmap = compressBitmap(bitmap)
generateFromPrompt(prompt, compressedBitmap)
```

---

## ❌ Erro: "Unsupported image format"

### Causa
Formato de imagem não suportado

### Solução
Formatos suportados:
- ✅ JPEG
- ✅ PNG
- ✅ GIF
- ✅ WebP

Se importar de câmera, garantir que salva em JPEG:
```kotlin
bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
```

---

## ❌ Erro: "Rate limit exceeded (429)"

### Causa
Muitas requisições em pouco tempo

### Solução
```kotlin
// Implementar delay entre requisições
private var lastRequestTime = 0L
private val MIN_REQUEST_INTERVAL = 2000L // 2 segundos

generateButton.setOnClickListener {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
        resultText.text = "Aguarde antes de fazer novo prompt"
        return@setOnClickListener
    }
    lastRequestTime = currentTime
    // ... fazer requisição
}
```

---

## 🔍 DEBUG - Verificar Logs

### No Logcat do Android Studio
```
Filter: "AiLogicFragment"
```

### Adicionar logs customizados
```kotlin
Log.d("AiLogicFragment", "Bitmap size: ${bitmap.width}x${bitmap.height}")
Log.d("AiLogicFragment", "Prompt: $prompt")
Log.d("AiLogicFragment", "Response: ${response.text}")
```

---

## 📞 Quando Tudo Falha

### Verificar:
1. ✅ API Gemini ativada no Firebase Console
2. ✅ google-services.json configurado
3. ✅ Firebase Auth funcionando (testa login)
4. ✅ Internet conectada
5. ✅ Permissões concedidas
6. ✅ Versões de dependências corretas
7. ✅ Sync Gradle atualizado

### Reset Completo
```bash
# No Android Studio Terminal:
./gradlew clean
./gradlew build

# Ou pelo Menu:
Build → Clean Project
Build → Rebuild Project
```

---

## 📝 Logs Úteis para Debug

### Adicionar ao AiLogicFragment
```kotlin
private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
    lifecycleScope.launch {
        try {
            Log.d("AI_DEBUG", "=== INICIANDO GEMINI ===")
            Log.d("AI_DEBUG", "Prompt: $prompt")
            Log.d("AI_DEBUG", "Bitmap: ${bitmap.width}x${bitmap.height}")
            Log.d("AI_DEBUG", "Modelo: gemini-2.5-flash")
            
            val promptImage = content {
                image(bitmap)
                text(prompt)
            }
            Log.d("AI_DEBUG", "Enviando para Gemini...")
            
            val response = model.generateContent(promptImage)
            
            Log.d("AI_DEBUG", "Resposta recebida")
            Log.d("AI_DEBUG", "Texto: ${response.text}")
            Log.d("AI_DEBUG", "=== FIM ===")
            
            resultText.text = response.text ?: "Sem resposta"
        } catch (e: Exception) {
            Log.e("AI_DEBUG", "ERRO: ${e.message}", e)
            resultText.text = "Erro: ${e.message}"
        }
    }
}
```

---

## 🆘 Suporte

Se o problema persistir:
1. Verifique: https://firebase.google.com/docs/ai/troubleshooting
2. Consulte: https://issuetracker.google.com/issues/new (Firebase AI)
3. Stack Overflow: tag `firebase-ai`

---

**Última atualização**: 13 de Novembro de 2025
