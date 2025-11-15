# 🔧 PASSO A PASSO - Resolver Erro de Dependência Firebase AI

## ✅ JÁ FEITO

```
✓ Versão corrigida para 17.5.0
✓ Gradle daemon parado
✓ Cache limpo
```

---

## 🎯 PRÓXIMAS AÇÕES (Escolha Uma)

### OPÇÃO 1: Pelo Android Studio (Recomendado) ⭐

#### Passo 1: Invalidar Cache
```
File → Invalidate Caches... → Invalidate and Restart
```

**Aguarde**: Android Studio reinicia (~2 minutos)

#### Passo 2: Clean Project
```
Build → Clean Project
```

**Aguarde**: Até aparecer "Build completed"

#### Passo 3: Rebuild Project
```
Build → Rebuild Project
```

**Aguarde**: Até aparecer "Build completed successfully" ✅

---

### OPÇÃO 2: Pelo Terminal

#### Passo 1: Navegar até o projeto
```bash
cd c:\Users\Victor\Documents\GitHub\Vbase_2025
```

#### Passo 2: Clean
```bash
./gradlew clean
```

**Aguarde**: Até terminar (2-3 minutos)

#### Passo 3: Build
```bash
./gradlew build
```

**Aguarde**: Até aparecer "BUILD SUCCESSFUL" ✅

---

### OPÇÃO 3: Rápida (Nuclear)

Se as anteriores não funcionarem:

#### Passo 1
```bash
cd c:\Users\Victor\Documents\GitHub\Vbase_2025
```

#### Passo 2
```bash
./gradlew clean --stop
```

#### Passo 3
Delete manualmente:
```
C:\Users\Victor\.gradle
```

#### Passo 4
```bash
./gradlew build
```

---

## 📊 VERIFICAR APÓS BUILD

### Se aparecer no final:
```
BUILD SUCCESSFUL
```

**Status**: ✅ Tudo OK!

### Se aparecer:
```
BUILD FAILED
```

**Verificar**:
1. Está conectado à internet?
2. A versão em `libs.versions.toml` é `17.5.0`?
3. Salvou o arquivo?

---

## ✅ QUANDO FUNCIONAR

Você verá:
```
BUILD SUCCESSFUL in XXs
```

Agora você pode:
1. Rodar o app no emulador
2. Testar a IA
3. Usar normalmente

---

## 💡 DICA

Se receber erro de conexão com Maven:
```
Check your internet connection
```

**Solução**:
- Verificar WiFi/internet
- Aguardar 1 minuto
- Tentar novamente

---

## 🚀 APÓS RESOLVER

1. Open Android Studio
2. Abra `AiLogicFragment.kt`
3. Verifique que não tem erros
4. Se estiver OK, está pronto! ✅

---

**Estimado**: 5-10 minutos para resolver
