# 🧪 GUIA DE TESTE - Fluxo Registros → Postagens

## ✅ Antes de Testar

- [ ] Build do projeto compila sem erros
- [ ] Firebase Realtime Database está configurado
- [ ] Usuário está autenticado (logado no app)
- [ ] Conexão de internet ativa

---

## 🧪 Teste 1: Criação Básica de Planta

### Passos:
1. Abra o app
2. Clique em **"Registro"** (aba inferior)
3. Clique em **"Registrar Planta"**
4. Preencha os campos:
   - Nome: `Rosa Vermelha`
   - Data: `14/11/2025`
   - Local: `Jardim de Casa`
   - Observação: `Planta saudável com flores bonitas`
   - Categoria: Selecione `SAUDÁVEL`
5. Clique em **"Selecionar Imagens"** e escolha 1-2 fotos
6. Clique em **"Salvar"**

### Resultado Esperado:
- ✅ Toast mostrando "Registro salvo com sucesso"
- ✅ Volta automaticamente para a tela anterior
- ✅ Registro aparece em **"Seus Registros"** em segundos

---

## 🧪 Teste 2: Verificar em "Seus Registros"

### Passos:
1. Na aba inferior, clique em **"Seus Registros"**
2. Verifique se o registro criado apareça

### Resultado Esperado:
- ✅ Card com a planta/inseto registrado
- ✅ Mostra: nome, data, local, imagem
- ✅ Pode clicar para ver detalhes

---

## 🧪 Teste 3: Verificar em "Postagens" (O PRINCIPAL!)

### Passos:
1. Na aba inferior, clique em **"Postagens"**
2. Veja se a postagem apareça na lista

### Resultado Esperado:
- ✅ **A postagem criada aparece no topo**
- ✅ Mostra: avatar, nome do usuário, título, descrição
- ✅ Exibe a imagem selecionada
- ✅ Mostra data/hora (ex: "Agora", "1h", etc)
- ✅ Conta de likes, comentários, compartilhamentos
- ✅ Botões funcionais (Like, Comentar, Compartilhar)

---

## 🧪 Teste 4: Criar Inseto

### Passos:
1. Clique em **"Registro"**
2. Clique em **"Registrar Inseto"**
3. Preencha os campos similarmente:
   - Nome: `Borboleta Azul`
   - Data: `14/11/2025`
   - Local: `Jardim Público`
   - Observação: `Inseto benéfico observado`
   - Categoria: Selecione uma categoria
4. Selecione imagens
5. Clique em **"Salvar"**

### Resultado Esperado:
- ✅ Inseto aparece em "Seus Registros"
- ✅ Inseto aparece automaticamente em "Postagens"

---

## 🧪 Teste 5: Tempo Real (Importante!)

### Setup:
- 2 dispositivos/abas do browser com o app aberto
- Ambas nas "Postagens"

### Passos:
1. **Dispositivo A**: Registre uma nova planta
2. **Dispositivo B**: Observe se a postagem aparece **sem precisar atualizar**

### Resultado Esperado:
- ✅ Postagem aparece em tempo real em Dispositivo B
- ✅ Nenhum lag ou delay perceptível

---

## 🧪 Teste 6: Verificar Firebase

### No Console Firebase:
1. Abra: https://console.firebase.google.com
2. Selecione seu projeto
3. **Realtime Database** → **Dados**
4. Procure pela pasta **`Postagens/`**

### Resultado Esperado:
```json
{
  "postagens": {
    "plant_1700000001_abc123": {
      "id": "plant_1700000001_abc123",
      "tipo": "PLANTA",
      "titulo": "Rosa Vermelha",
      "usuario": {
        "id": "user_123",
        "nome": "João Silva",
        ...
      },
      "imageUrl": "data:image/jpeg;base64,..."
    }
  }
}
```

---

## 🧪 Teste 7: Testar Botões de Ação

### Na tela "Postagens":
1. Clique em **❤️ Like** → Deve passar para 1 curtida
2. Clique em **💬 Comentar** → Deve abrir toast (em desenvolvimento)
3. Clique em **↗️ Compartilhar** → Deve abrir toast (em desenvolvimento)

### Resultado Esperado:
- ✅ Botões respondem ao clique
- ✅ Like muda de cor (preenchido/vazio)
- ✅ Contador de curtidas atualiza

---

## 🧪 Teste 8: Múltiplos Registros

### Passos:
1. Crie 3 plantas diferentes
2. Crie 2 insetos diferentes
3. Abra "Postagens"

### Resultado Esperado:
- ✅ Todas as 5 postagens aparecem
- ✅ Ordenadas por data (mais recentes no topo)
- ✅ Funcionam normalmente

---

## ❌ Possíveis Problemas e Soluções

### ❌ "Nenhuma postagem disponível"
**Causas possíveis:**
- Usuário não autenticado
- Sem conexão com Firebase
- Firestore rules bloqueando leitura

**Solução:**
```
1. Verifique se está logado
2. Verifique internet
3. Abra Console Firebase → Rules
4. Verifique permitir leitura em "postagens"
```

### ❌ Postagem criada mas não aparece
**Causas possíveis:**
- Listener não foi inicializado
- ID da postagem incorreto

**Solução:**
```
1. Abra Logcat do Android Studio
2. Procure por "PostagensVM" ou "RegistroPlantaVM"
3. Veja se tem erros
4. Reinicie o app
```

### ❌ Imagem não aparece
**Causas possíveis:**
- Imagem não foi uploadada
- Base64 muito grande
- Drawable não existe

**Solução:**
```
1. Verifique se selecionou imagem antes de salvar
2. Tente com imagem menor
3. Verifique em Firebase se imageUrl tem conteúdo
```

### ❌ App quebra ao abrir Postagens
**Causas possíveis:**
- Erro no adapter ao renderizar
- Falha ao desserializar dados

**Solução:**
```
1. Verifique logcat por exception
2. Verifique se PostagensAdapter.kt foi criado
3. Limpe build: Build → Clean Project
4. Rebuild: Build → Rebuild Project
```

---

## 📊 Checklist Final

- [ ] Planta criada aparece em "Seus Registros"
- [ ] Planta criada aparece em "Postagens"
- [ ] Inseto criado aparece em "Seus Registros"
- [ ] Inseto criado aparece em "Postagens"
- [ ] Postagens carregam em tempo real
- [ ] Múltiplos registros funcionam
- [ ] Firebase contém dados corretos
- [ ] Botões de ação funcionam
- [ ] Nenhum crash ao abrir telas
- [ ] Layout renderiza corretamente

---

## 📝 Logs Úteis para Debug

No Android Studio → Logcat, procure por:

```
# Sucesso ao criar postagem
D RegistroPlantaVM: Postagem criada com sucesso: plant_123...

# Sucesso ao carregar postagens
D FirebaseDB: Carregadas 5 postagens (0 erros)

# Erro ao salvar
E RegistroPlantaVM: Erro ao criar postagem

# Erro ao desserializar
E FirebaseDB: Erro ao desserializar postagem
```

---

## 🎉 Tudo Funcionando?

Se todas as verificações passaram:

✅ **O fluxo está 100% funcional!**

Seus usuários podem agora:
1. Registrar plantas/insetos
2. Ver em "Seus Registros" (privado)
3. Ver em "Postagens" (público) automaticamente
4. Interagir com postagens
5. Ver em tempo real

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique a seção "Possíveis Problemas e Soluções"
2. Limpe cache: File → Invalidate Caches → Restart
3. Execute um clean build
4. Se persistir, compartilhe os logs do Logcat

