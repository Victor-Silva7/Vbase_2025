# Implementação de Salvamento em Base64 no Realtime Database

## Resumo da Implementação

Este documento descreve a implementação completa do sistema de salvamento de imagens em Base64 no Realtime Database para o projeto Vbase_2025.

## Arquivos Modificados/Criados

### 1. **Base64ImageUtil.kt**
**Localização:** `app/src/main/java/com/ifpr/androidapptemplate/utils/Base64ImageUtil.kt`

**Responsabilidade:**
- Converter imagens (Uri) para strings Base64
- Redimensionar imagens automáticamente (máximo 1024x1024px)
- Comprimir imagens em JPEG com qualidade 70%
- Gerenciar recursos (fechar streams, reciclar bitmaps)

**Método Principal:**
```kotlin
fun toBase64(context: Context, imageUri: Uri): String?
```

Retorna: String em Base64 ou null se houver erro

### 2. **UploadProgress.kt**
**Localização:** `app/src/main/java/com/ifpr/androidapptemplate/utils/UploadProgress.kt`

**Responsabilidade:**
- Data class para rastreamento do progresso do upload

**Propriedades:**
- `currentStep`: Int - Etapa atual do upload
- `progress`: Double - Porcentagem de progresso (0-100)

### 3. **RealtimeDatabaseImageManager.kt**
**Localização:** `app/src/main/java/com/ifpr/androidapptemplate/data/firebase/RealtimeDatabaseImageManager.kt`

**Responsabilidade Principal:**
- Gerenciar o salvamento de imagens em Base64 no Realtime Database
- Organizar estrutura de dados no firebase
- Deletar imagens quando necessário
- Fornecer feedback de progresso

**Métodos Principais:**

1. `saveImage()` - Salva uma imagem
```kotlin
suspend fun saveImage(
    context: Context,
    imageUri: Uri,
    path: String
): Result<String>
```

2. `saveImages()` - Salva múltiplas imagens
```kotlin
suspend fun saveImages(
    context: Context,
    imageUris: List<Uri>,
    path: String,
    onProgress: (progress: Int) -> Unit
): Result<List<String>>
```

3. `savePlantImages()` - Atalho para salvar imagens de plantas
```kotlin
suspend fun savePlantImages(
    context: Context,
    plantId: String,
    imageUris: List<Uri>,
    onProgress: (progress: Int) -> Unit
): Result<List<String>>
```

4. `saveInsectImages()` - Atalho para salvar imagens de insetos
```kotlin
suspend fun saveInsectImages(
    context: Context,
    insectId: String,
    imageUris: List<Uri>,
    onProgress: (progress: Int) -> Unit
): Result<List<String>>
```

5. `deleteImage()` - Remove uma imagem
```kotlin
suspend fun deleteImage(path: String, imageId: String): Result<Unit>
```

6. `deleteImages()` - Remove múltiplas imagens
```kotlin
suspend fun deleteImages(path: String, imageIds: List<String>): Result<Unit>
```

### 4. **ImageUploadManager.kt** (Modificado)
**Localização:** `app/src/main/java/com/ifpr/androidapptemplate/utils/ImageUploadManager.kt`

**Responsabilidade:**
- Interface principal para upload de imagens
- Coordena o processo de conversão e salvamento
- Fornece feedback de progresso e status

**Métodos Principais:**

1. `uploadPlantImages()` - Upload de imagens de plantas
```kotlin
fun uploadPlantImages(
    context: Context,
    plantId: String,
    imageUris: List<Uri>,
    onSuccess: (List<String>) -> Unit,
    onFailure: (Exception) -> Unit
)
```

2. `uploadInsectImages()` - Upload de imagens de insetos
```kotlin
fun uploadInsectImages(
    context: Context,
    insectId: String,
    imageUris: List<Uri>,
    onSuccess: (List<String>) -> Unit,
    onFailure: (Exception) -> Unit
)
```

**LiveData para Observação:**
- `uploadProgress`: Progresso do upload
- `uploadStatus`: Status do upload (STARTING, UPLOADING, SUCCESS, ERROR)

## Estrutura no Realtime Database

```
usuarios/
  {userId}/
    plantas/
      {plantId}/
        imagens/
          {imageId}: "data:image/jpeg;base64,..."
    insetos/
      {insectId}/
        imagens/
          {imageId}: "data:image/jpeg;base64,..."
```

## Regras de Segurança Firebase

As regras foram atualizadas em `firebase-database-rules.json` para permitir:
- Leitura e escrita de imagens apenas para usuários autenticados
- Limite de tamanho máximo de ~5MB por imagem em Base64

## Como Usar

### Exemplo de Upload de Plantas:
```kotlin
val imageUploadManager = ImageUploadManager.getInstance()

imageUploadManager.uploadPlantImages(
    context = context,
    plantId = "planta_123",
    imageUris = listOf(uri1, uri2, uri3),
    onSuccess = { imageIds ->
        // Salvar os IDs no objeto de planta
        println("Imagens salvas com IDs: $imageIds")
    },
    onFailure = { exception ->
        // Tratar erro
        println("Erro no upload: ${exception.message}")
    }
)

// Observar progresso
imageUploadManager.uploadProgress.observe(this) { progress ->
    val percentage = progress.progress.toInt()
    progressBar.progress = percentage
}

// Observar status
imageUploadManager.uploadStatus.observe(this) { status ->
    when (status) {
        ImageUploadManager.UploadStatus.STARTING -> println("Iniciando...")
        ImageUploadManager.UploadStatus.UPLOADING -> println("Enviando...")
        ImageUploadManager.UploadStatus.SUCCESS -> println("Sucesso!")
        ImageUploadManager.UploadStatus.ERROR -> println("Erro!")
    }
}
```

### Exemplo de Upload de Insetos:
```kotlin
imageUploadManager.uploadInsectImages(
    context = context,
    insectId = "inseto_456",
    imageUris = listOf(uri1, uri2),
    onSuccess = { imageIds ->
        println("Inseto salvo com IDs de imagens: $imageIds")
    },
    onFailure = { exception ->
        println("Erro: ${exception.message}")
    }
)
```

## Fluxo de Processamento

1. **Recebimento da imagem:** URI da imagem é recebida
2. **Conversão para Base64:**
   - Abre a imagem da URI
   - Redimensiona se necessário (máximo 1024x1024)
   - Comprime como JPEG com qualidade 70%
   - Converte para Base64
3. **Salvamento no Realtime Database:**
   - Gera ID único para a imagem
   - Salva a string Base64 no database
4. **Retorno de Sucesso:**
   - Retorna lista de IDs das imagens salvas
   - Callback onSuccess é chamado com os IDs

## Vantagens desta Implementação

✅ **Independência do Firebase Storage**
- Não depende do serviço de Storage
- Tudo em um único banco de dados

✅ **Facilidade de Sincronização**
- Dados sempre sincronizados com o banco
- Fácil de fazer backup

✅ **Controle Total**
- Total controle sobre o tamanho e formato
- Compressão otimizada

✅ **Melhor para Conexões Instáveis**
- Sincronização automática quando retorna conexão
- Funcionamento offline do Realtime Database

## Considerações Importantes

⚠️ **Limite de Tamanho:**
- Cada imagem é limitada a ~5MB em Base64
- Imagens são redimensionadas automaticamente

⚠️ **Performance:**
- Imagens maiores podem aumentar latência
- Recomendado para até ~50 imagens por item

⚠️ **Custos Firebase:**
- Leitura/escrita do database são contabilizadas
- Considere quotas no seu plano Firebase

## Tratamento de Erros

Todos os métodos retornam `Result<T>`:
- `Result.success(dados)` - Operação bem-sucedida
- `Result.failure(exception)` - Operação falhou

Use `fold()` para tratar ambos os casos:
```kotlin
result.fold(
    onSuccess = { data ->
        // Processar sucesso
    },
    onFailure = { exception ->
        // Processar erro
    }
)
```

## Logs de Debug

Todos os eventos são registrados com tag `RealtimeDBImageManager` e `ImageUploadManager`:
```kotlin
// Ver logs
adb logcat RealtimeDBImageManager
adb logcat ImageUploadManager
```