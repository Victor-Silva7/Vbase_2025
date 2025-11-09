# 🧪 Guia de Testes - Registros em \"SEUS REGISTROS\"

## Teste 1: Verificar Estrutura de Dados no Firebase

### Objetivo
Confirmar que imagens estão salvas como IDs Base64 e não como URIs

### Passos
1. Abra [Firebase Console](https://console.firebase.google.com)
2. Selecione seu projeto
3. Navegue até **Realtime Database**
4. Localize: `usuarios/{seu-uid}/insetos/{novo-inseto-id}`
5. Verifique a estrutura:

### ✅ Estrutura Correta
```json
{
  \"insetos\": {
    \"insect_1234567890_abcd\": {
      \"id\": \"insect_1234567890_abcd\",
      \"nome\": \"Joaninha\",
      \"categoria\": \"BENEFICIAL\",
      \"imagens\": [
        \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",
        \"e4d3c2b1-9876-5432-1098-abcdef123456\"
      ],
      \"timestamp\": 1699574324000,
      \"userId\": \"seu-uid-aqui\",
      \"userName\": \"Seu Nome\"
    },
    \"imagens\": {
      \"f47ac10b-58cc-4372-a567-0e02b2c3d479\": \"iVBORw0KGgoAAAANSUhEUgAAAAEA...\",
      \"e4d3c2b1-9876-5432-1098-abcdef123456\": \"iVBORw0KGgoAAAANSUhEUgAAAAEA...\"
    }
  }
}
```

### ❌ Estrutura Incorreta (Anterior)
```json
{
  \"insetos\": {
    \"insect_1234567890_abcd\": {
      \"imagens\": [
        \"content://media/external/images/media/12345\",
        \"file:///storage/emulated/0/Pictures/IMG_20231110_120000.jpg\"
      ]
    }
  }
}
```

### O que Verificar
- [ ] Campo `imagens` é uma lista de strings
- [ ] Cada string é um UUID/GUID (formato: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
- [ ] Subpath `imagens/` contém os Base64 strings
- [ ] Não há URIs do tipo `content://` ou `file://`

---

## Teste 2: Verificar Listeners Ativados

### Objetivo
Confirmar que os listeners estão conectados ao Firebase

### Passos
1. Abra Android Studio
2. Vá em **Logcat** (inferior da tela)
3. Selecione seu emulador/device
4. Filtre por: `FirebaseDB` ou `RegistroRepository`

### Execute a Ação
1. Abra o app
2. Navegue até "SEUS REGISTROS"
3. Observe os logs

### ✅ Logs Esperados
```
D/FirebaseDB: Attaching listener para: usuarios/xyz123/plantas
D/FirebaseDB: Attaching listener para: usuarios/xyz123/insetos
D/RegistroRepository: Starting listener para plantas do usuário
D/RegistroRepository: Starting listener para insetos do usuário
```

### Isso Significa
- Listeners foram criados com sucesso
- Estão monitorando os caminhos corretos
- Pronto para detectar mudanças

---

## Teste 3: Verificar Fluxo Completo de Salvamento

### Objetivo
Testar do início ao fim o salvamento de um novo registro

### Setup
1. Faça login no app
2. Navegue até \"SEUS REGISTROS\" e deixe a tela aberta
3. Abra um novo terminal com Logcat filtrado por `Meus`

### Execute a Ação
1. Clique no botão FAB (**+**) em \"SEUS REGISTROS\"
2. Selecione \"Novo Inseto\"
3. Preencha os campos:
   - Nome: `Teste Joaninha 001`
   - Categoria: `Benéfico`
   - Local: `Meu Jardim`
   - Observação: `Teste automatizado`
4. **Selecione uma foto** (importante!)
5. Clique \"Salvar\"

### Observe os Logs (em Sequência)

#### 1️⃣ Upload de Imagens
```
D/ImageUploadManager: Iniciando upload de 1 imagem(ns)
D/Base64ImageUtil: Convertendo imagem para Base64
D/RealtimeDatabaseImageManager: Salvando imagem Base64 em: usuarios/xyz/insetos/insect_123/imagens/uuid-1
```

#### 2️⃣ Salvamento do Registro
```
D/FirebaseDB: Salvando inseto em: usuarios/xyz/insetos/insect_123
D/FirebaseDB: Inseto salvo com sucesso
```

#### 3️⃣ Recarregamento do Repository
```
D/RegistroRepository: Força refresh para insetos do usuário
D/FirebaseDB: Attaching listener para: usuarios/xyz/insetos
D/FirebaseDB: Listener: Carregados 2 insetos de xyz (0 erros)
D/RegistroRepository: Insetos atualizados: 2 registros
```

#### 4️⃣ Atualização do ViewModel
```
D/MeusRegistrosVM: Combinando registros: 0 plantas + 2 insetos, filtro: TODOS
D/MeusRegistrosVM: Lista final de registros: 2
```

#### 5️⃣ Sucesso
```
✅ Novo inseto aparece na tela em tempo real!
```

### O Que Fazer se Não Aparecer

| Log Faltando | Causa | Solução |
|---|---|---|
| Logs do Upload | Imagem não foi selecionada | Selecione uma foto |
| Logs do Firebase SaveInsect | Erro ao salvar | Verifique regras de segurança |
| Logs do Listener Update | Listener não está ativo | Verifique se está no \"SEUS REGISTROS\" |
| Logs do ViewModel | ViewModel foi destruído | Não minimize o app durante teste |
| Nada aparece | Múltiplas causas | Veja \"Teste 4\" abaixo |

---

## Teste 4: Debug Detalhado

### Ativar Logs Adicionais

Se o fluxo não funcionar, ative logs mais detalhados:

#### 1. Em FirebaseDatabaseService.kt:
```kotlin
fun listenToUserInsects(userId: String? = null, callback: (List<Inseto>) -> Unit): ValueEventListener? {
    val targetUserId = userId ?: getCurrentUserId() ?: run {
        Log.e(\"FirebaseDB\", \"❌ getCurrentUserId retornou null!\")
        return null
    }
    
    Log.d(\"FirebaseDB\", \"✅ UserId: $targetUserId\")
    val userInsectsRef = usuariosRef.child(targetUserId).child(\"insetos\")
    Log.d(\"FirebaseDB\", \"✅ Path: usuarios/$targetUserId/insetos\")
    
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d(\"FirebaseDB\", \"📊 onDataChange disparado\")
            Log.d(\"FirebaseDB\", \"📊 Total de documentos: ${snapshot.childrenCount}\")
            
            val insetos = mutableListOf<Inseto>()
            var errorCount = 0
            
            snapshot.children.forEach { childSnapshot ->
                Log.d(\"FirebaseDB\", \"📝 Processando: ${childSnapshot.key}\")
                
                val insectData = childSnapshot.value as? Map<String, Any?> ?: run {
                    Log.w(\"FirebaseDB\", \"⚠️ Dados não são Map: ${childSnapshot.value?.javaClass?.simpleName}\")
                    return@forEach
                }
                
                try {
                    val inseto = Inseto.fromFirebaseMap(insectData)
                    Log.d(\"FirebaseDB\", \"✅ Inseto desserializado: ${inseto.nome}\")
                    insetos.add(inseto)
                } catch (e: Exception) {
                    errorCount++
                    Log.e(\"FirebaseDB\", \"❌ Erro ao desserializar\", e)
                    e.printStackTrace()
                }
            }
            
            Log.d(\"FirebaseDB\", \"🎉 Total: ${insetos.size} insetos, $errorCount erros\")
            callback(insetos.sortedByDescending { it.timestamp })
        }
        
        override fun onCancelled(error: DatabaseError) {
            Log.e(\"FirebaseDB\", \"❌ Listener cancelado: ${error.message}\")
            error.toException().printStackTrace()
        }
    }
    
    userInsectsRef.addValueEventListener(listener)
    return listener
}
```

#### 2. Em RegistroRepository.kt:
```kotlin
fun getUserInsects(userId: String? = null, forceRefresh: Boolean = false) {
    Log.d(\"RegistroRepository\", \"🔄 getUserInsects chamado - forceRefresh: $forceRefresh\")
    
    if (forceRefresh || _userInsects.value.isNullOrEmpty()) {
        Log.d(\"RegistroRepository\", \"📥 Recarregando insetos do Firebase\")
        
        repositoryScope.launch {
            try {
                val result = databaseService.getUserInsects(userId)
                result.onSuccess { insetos ->
                    Log.d(\"RegistroRepository\", \"✅ Sucesso: ${insetos.size} insetos\")
                    _userInsects.postValue(insetos)
                }.onFailure { error ->
                    Log.e(\"RepositoryRepository\", \"❌ Erro: ${error.message}\", error)
                }
            } catch (e: Exception) {
                Log.e(\"RegistroRepository\", \"❌ Exceção: ${e.message}\", e)
            }
        }
    } else {
        Log.d(\"RegistroRepository\", \"⏭️ Usando cache: ${_userInsects.value?.size ?: 0} insetos\")
    }
}
```

### Procure nos Logs:
- ✅ `getCurrentUserId retornou null` → Usuário não autenticado
- ✅ `Listener cancelado` → Erro de conexão Firebase
- ✅ `Erro ao desserializar` → Estrutura de dados inválida
- ✅ `Total: 0 insetos` → Nenhum inseto no database

---

## Teste 5: Verificar Autenticação

### Objetivo
Confirmar que o usuário está autenticado e tem UID válido

### Execute no Android Studio Console:

```kotlin
val auth = FirebaseAuth.getInstance()
val user = auth.currentUser

if (user != null) {
    Log.d(\"Auth\", \"✅ Usuário: ${user.email}\")
    Log.d(\"Auth\", \"✅ UID: ${user.uid}\")
    Log.d(\"Auth\", \"✅ Display Name: ${user.displayName}\")
} else {
    Log.e(\"Auth\", \"❌ Nenhum usuário autenticado\")
}
```

### Paste em MeusRegistrosFragment.onViewCreated():
```kotlin
val auth = FirebaseAuth.getInstance()
val user = auth.currentUser
if (user != null) {
    Log.d(\"Auth-Test\", \"UID: ${user.uid}\")
} else {
    Log.e(\"Auth-Test\", \"Sem autenticação!\")
}
```

---

## Teste 6: Teste Manual no Firebase Console

### Objetivo
Simular salvamento manual para verificar se listeners funcionam

### Passos
1. Deixe o app aberto em \"SEUS REGISTROS\"
2. Abra Firebase Console em seu navegador
3. Clique em **Realtime Database**
4. Navegue até `usuarios/{seu-uid}/insetos`
5. Clique o botão **+** para adicionar novo registro
6. Preencha:
   ```json
   {
     \"id\": \"manual_test_123\",
     \"nome\": \"Teste Manual\",
     \"categoria\": \"NEUTRAL\",
     \"imagens\": [\"test-id-1\"],
     \"timestamp\": 1699574324000
   }
   ```
7. Clique **Add**

### ✅ Se Funcionar
- Novo item aparece na tela do app em tempo real (menos de 1 segundo)
- Listeners estão funcionando corretamente

### ❌ Se Não Funcionar
- Listeners podem estar inativos
- Verifique se está em \"SEUS REGISTROS\"
- Verifique regras de Firebase Security

---

## Teste 7: Performance e Limites

### Teste de Limite
1. Crie 100+ registros
2. Veja se a lista carrega
3. Observe se há lag ao scroll

### Esperado
- Primeira carga: até 2 segundos
- Scroll: sem travamentos
- Listeners: respondem em < 1 segundo

### Se Houver Problemas
- Implementar paginação
- Usar Room Database para cache
- Implementar índices no Firebase

---

## Teste 8: Teste de Sincronização Offline

### Setup
1. Deixe o app aberto
2. Desative WiFi/dados do device

### Teste
1. Salve um novo registro
2. Verifique se salvou localmente (UI não deve bloquear)
3. Reative WiFi
4. Verifique se sincronizou com Firebase

### Esperado
- App não deve travar quando offline
- Dados devem sincronizar quando voltar online

---

## Checklist Final

- [ ] Registros aparecem em \"SEUS REGISTROS\" após salvamento
- [ ] Imagens estão em Base64 no Firebase
- [ ] IDs de imagens são UUIDs, não URIs
- [ ] Listeners estão ativos (verificar logs)
- [ ] Autenticação funciona (usuário tem UID)
- [ ] Sem erros de compilação
- [ ] Sem crashes ao salvar
- [ ] Sem travamentos ao scrollar lista
- [ ] Sincronização em tempo real funciona
- [ ] Swipe refresh recarrega dados

---

## 🚨 Troubleshooting Rápido

| Sintoma | Causa Provável | Solução |
|---------|---|---|
| Registros não aparecem | Listeners não ativos | Abra \"SEUS REGISTROS\" |
| Firebase vazio | UserId incorreto | Verifique autenticação |
| Erros ao desserializar | Estrutura de dados errada | Verifique Inseto.fromFirebaseMap() |
| Imagens não carregam | URLs inválidas | Verifique se são IDs, não URIs |
| Listener não dispara | Regras de Firebase | Verifique firebase.json rules |
| App trava | Query muito grande | Implemente paginação |
| Sincronização lenta | Muitos listeners | Limpe listeners não usados |
| Offline não funciona | Sem cache local | Implemente Room Database |

---

## 📞 Se Nada Funcionar

1. **Verifique os logs** em cada camada (Firebase, Repository, ViewModel)
2. **Verifique a autenticação** - usuário está logado?
3. **Verifique as regras** - Firebase permite ler/escrever?
4. **Verifique a estrutura** - dados no Firebase estão corretos?
5. **Force refresh** - puxe para recarregar em \"SEUS REGISTROS\"
6. **Reinicie tudo** - feche o app e abra novamente
7. **Limpe cache** - em Configurações > Aplicativos > Limpar cache
8. **Check logs** - procure por **❌** em todos os logs mencionados
