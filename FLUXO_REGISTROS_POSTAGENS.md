# 📱 Fluxo de Registros e Postagens - VBase 2025

## 🎯 Objetivo
Quando um usuário registra uma planta ou inseto:
1. ✅ O registro é **armazenado em "Seus Registros"** (privado)
2. ✅ O registro aparece também em **"Postagens"** (feed público/compartilhado)

---

## 📊 Arquitetura Atual

### Estrutura de Dados no Firebase

```
Vbase_2025
├── Plantas/
│   └── {plantId} → Planta Object
├── Insetos/
│   └── {insetoId} → Inseto Object
├── Postagens/
│   └── {postagemId} → PostagemFeed Object
└── Usuarios/
    └── {userId}
        ├── Registros/
        └── Postagens/
```

### Modelos de Dados

#### 1. **Planta.kt** - Registro Individual
```kotlin
data class Planta(
    val id: String,                    // ID único
    val nome: String,                  // Nome da planta
    val data: String,                  // Data do registro
    val local: String,                 // Localização
    val categoria: PlantHealthCategory,// HEALTHY, DISEASED, etc
    val observacao: String,
    val imagens: List<String>,        // Base64 encodadas
    val userId: String,               // Dono do registro
    val userName: String,             // Nome do usuário
    val timestamp: Long,              // Data de criação
    val tipo: String = "PLANTA",      // PLANTA ou INSETO
    val visibilidade: VisibilidadeRegistro // PRIVADO ou PUBLICO
)
```

#### 2. **PostagemFeed.kt** - Postagem no Feed
```kotlin
data class PostagemFeed(
    val id: String,                   // ID único (pode ser igual ao registro)
    val tipo: TipoPostagem,          // PLANTA ou INSETO
    val usuario: UsuarioPostagem,    // Dados do usuário
    val titulo: String,              // Título da postagem
    val descricao: String,           // Descrição
    val imagem: String,              // Imagem principal (Base64)
    val timestamp: Long,             // Data de criação
    val curtidas: Int = 0,           // Número de likes
    val comentarios: Int = 0,        // Número de comentários
    val usuarioId: String            // ID do criador
)
```

---

## 🔄 Fluxo Completo

### 1️⃣ **Usuário Registra Planta**
```
RegistroPlantaActivity
    ↓
RegistroPlantaViewModel.saveRegistration()
    ↓
ImageUploadManager.uploadPlantImages()  → Upload imagens em Base64
    ↓
FirebaseDatabaseService.savePlant()  → Salva em Plantas/ (PRIVADO)
    ↓
✅ Registro aparece em "Seus Registros"
```

### 2️⃣ **Publicar em Postagens** (❌ PRECISA SER IMPLEMENTADO)
```
FirebaseDatabaseService.savePlant()
    ↓
Auto-criar PostagemFeed a partir do Planta
    ↓
Salvar em Postagens/ (PÚBLICO)
    ↓
✅ Registro aparece em "Postagens"
```

---

## 📁 Arquivos Relacionados

### **Layouts**
| Arquivo | Uso |
|---------|-----|
| `fragment_registros_list.xml` | Tela "Seus Registros" com RecyclerView |
| `fragment_postagens.xml` | Tela "Postagens" com RecyclerView |
| `item_registro_card.xml` | Card individual de registro |
| `item_postagem_card.xml` | Card individual de postagem |

### **ViewModels**
| Arquivo | Responsabilidade |
|---------|-----------------|
| `RegistroPlantaViewModel.kt` | Gerenciar formulário e upload |
| `RegistroInsetoViewModel.kt` | Gerenciar formulário inseto |
| `MeusRegistrosViewModel.kt` | Carregar registros do usuário |
| `PostagensViewModel.kt` | Carregar postagens do feed |

### **Firebase Services**
| Arquivo | Responsabilidade |
|---------|-----------------|
| `FirebaseDatabaseService` | Operações CRUD no Realtime Database |
| `FirebaseStorageManager` | Gerenciar imagens |
| `ImageUploadManager` | Converter e upload em Base64 |

---

## 🔧 O Que Precisa Ser Implementado

### **Passo 1: Modificar RegistroPlantaViewModel.kt**

Na função `saveRegistrationToDatabase()`, após salvar em "Plantas/", criar postagem:

```kotlin
private fun saveRegistrationToDatabase(registration: Planta) {
    viewModelScope.launch {
        try {
            // Salvar em Plantas/ (privado - seus registros)
            val result = databaseService.savePlant(registration)
            
            result.onSuccess { plantId ->
                // 🆕 Criar PostagemFeed a partir do Planta
                criarPostagemDoRegistro(registration)
                
                // Refresh dos dados
                repository.getUserPlants(forceRefresh = true)
                _isLoading.value = false
                _saveSuccess.value = true
                clearFormData()
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Erro inesperado: ${e.message}"
        }
    }
}

// 🆕 Nova função para criar postagem
private fun criarPostagemDoRegistro(registration: Planta) {
    try {
        val postagem = PostagemFeed(
            id = registration.id,  // Usar mesmo ID
            tipo = if(registration.tipo == "PLANTA") 
                TipoPostagem.PLANTA else TipoPostagem.INSETO,
            usuario = UsuarioPostagem(
                id = registration.userId,
                nome = registration.userName,
                avatar = "" // Buscar do perfil do usuário se disponível
            ),
            titulo = registration.nome,
            descricao = registration.observacao,
            imagem = registration.imagens.firstOrNull() ?: "",
            timestamp = registration.timestamp,
            usuarioId = registration.userId
        )
        
        // Salvar em Postagens/
        databaseService.savePostagem(postagem)
        
    } catch (e: Exception) {
        // Log error but don't fail the registration
        android.util.Log.e("RegistroViewModel", "Erro ao criar postagem", e)
    }
}
```

### **Passo 2: Implementar FirebaseDatabaseService.savePostagem()**

Adicione este método ao seu `FirebaseDatabaseService`:

```kotlin
fun savePostagem(postagem: PostagemFeed): Result<String> {
    return try {
        val ref = database.reference.child("Postagens").child(postagem.id)
        ref.setValue(postagem.toMap())
        Result.success(postagem.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Passo 3: Implementar PostagensViewModel.loadPostagens()**

```kotlin
class PostagensViewModel : ViewModel() {
    private val _postagens = MutableLiveData<List<PostagemFeed>>()
    val postagens: LiveData<List<PostagemFeed>> = _postagens
    
    private val database = FirebaseConfig.getDatabase()
    
    fun loadPostagens() {
        _isLoading.value = true
        
        // Listener para mudanças em tempo real
        database.reference.child("Postagens")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postagensList = mutableListOf<PostagemFeed>()
                    
                    for (child in snapshot.children.reversed()) {
                        try {
                            val postagem = PostagemFeed.fromMap(
                                child.value as? Map<String, Any?> ?: continue
                            )
                            postagensList.add(postagem)
                        } catch (e: Exception) {
                            Log.e("PostagensViewModel", "Erro ao parsear postagem", e)
                        }
                    }
                    
                    _postagens.value = postagensList
                    _isLoading.value = false
                }
                
                override fun onCancelled(error: DatabaseError) {
                    _errorMessage.value = error.message
                    _isLoading.value = false
                }
            })
    }
}
```

### **Passo 4: Fazer o Mesmo para Insetos**

Repita os mesmos passos em `RegistroInsetoViewModel.kt`:

```kotlin
// Na função saveRegistrationToDatabase()
criarPostagemDoRegistro(registration)
```

---

## 📊 Regras de Visibilidade

### **Seus Registros** (Privado)
```
└── usuarios/{userId}/Registros/
    └── plantas/
    └── insetos/
    
✅ Apenas o dono pode ver
```

### **Postagens** (Público)
```
└── Postagens/
    └── {id} → PostagemFeed
    
✅ Todos podem ver
✅ Todos podem comentar/curtir
```

---

## 🔐 Regras Firebase Sugeridas

```json
{
  "rules": {
    "Plantas": {
      "$plantId": {
        ".read": true,
        ".write": "root.child('Postagens').child($plantId).exists()"
      }
    },
    "Postagens": {
      "$postagemId": {
        ".read": true,
        ".write": "auth.uid != null"
      }
    },
    "usuarios": {
      "$userId": {
        "Registros": {
          ".read": "$userId === auth.uid",
          ".write": "$userId === auth.uid"
        }
      }
    }
  }
}
```

---

## ✅ Checklist de Implementação

- [ ] Adicionar `criarPostagemDoRegistro()` em `RegistroPlantaViewModel`
- [ ] Adicionar `savePostagem()` em `FirebaseDatabaseService`
- [ ] Implementar `PostagensViewModel.loadPostagens()`
- [ ] Implementar `PostagemFeed.fromMap()`
- [ ] Implementar `PostagemFeed.toMap()`
- [ ] Adicionar mesmo fluxo em `RegistroInsetoViewModel`
- [ ] Testar criação de planta → verificar se aparece em Postagens
- [ ] Testar criação de inseto → verificar se aparece em Postagens
- [ ] Atualizar regras Firebase se necessário

---

## 📝 Estrutura Esperada no Firebase após implementação

```
Postagens/
├── plant_1700000001_abc123de
│   ├── id: "plant_1700000001_abc123de"
│   ├── tipo: "PLANTA"
│   ├── titulo: "Rosa Vermelha"
│   ├── descricao: "Planta saudável no jardim"
│   ├── usuario: { id, nome, avatar }
│   ├── timestamp: 1700000001
│   └── imagem: "data:image/jpeg;base64,..."
└── inseto_1700000002_def456gh
    ├── id: "inseto_1700000002_def456gh"
    ├── tipo: "INSETO"
    ├── titulo: "Borboleta Azul"
    └── ...
```

---

## 🚀 Próximos Passos Opcionais

1. **Adicionar curtidas/comentários** - Implementar sistema de reações
2. **Filtrar por usuário** - Mostrar apenas postagens de um usuário específico
3. **Compartilhamento** - Permitir recompartilhar postagens
4. **Notificações** - Avisar quando alguém curtir/comentar
5. **Busca** - Buscar postagens por título/descrição

