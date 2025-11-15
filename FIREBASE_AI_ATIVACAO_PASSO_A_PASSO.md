# 🔐 PASSO A PASSO - Ativar Firebase AI Logic no Console

## ⏱️ Tempo estimado: 2 minutos

---

## PASSO 1: Acessar Firebase Console

**URL**: https://console.firebase.google.com

![1_firebase_console](https://via.placeholder.com/400x300?text=Abra+Firebase+Console)

---

## PASSO 2: Selecionar Seu Projeto

**Procure por**: `teste20251-ab84a`

Clique para abrir o projeto.

```
├─ Projetos Recentes
│  ├─ teste20251-ab84a  ← CLIQUE AQUI
│  └─ ...
```

---

## PASSO 3: Ir em Build > AI

No menu lateral esquerdo, procure por:

```
Build (seção no menu)
  ├─ Realtime Database
  ├─ Firestore Database
  ├─ Storage
  ├─ Hosting
  └─ AI  ← CLIQUE AQUI
```

![3_build_ai_menu](https://via.placeholder.com/400x300?text=Menu+Build+>+AI)

---

## PASSO 4: Ativar API Gemini

Na página de AI Logic, você verá:

```
┌─────────────────────────────────────┐
│ Firebase AI Logic                   │
│                                     │
│ Status: Não ativado                 │
│                                     │
│ [ ATIVAR API GEMINI ]  ← CLIQUE    │
└─────────────────────────────────────┘
```

Clique no botão "Ativar API Gemini" ou "Enable Gemini API".

![4_enable_gemini](https://via.placeholder.com/400x300?text=Enable+Gemini+API)

---

## PASSO 5: Aceitar Termos de Serviço

Um modal será exibido:

```
┌─────────────────────────────────────┐
│ Termos de Serviço do Gemini         │
│                                     │
│ ☑ Aceito os termos de serviço       │
│                                     │
│ [ CONFIRMAR ]  [ CANCELAR ]         │
└─────────────────────────────────────┘
```

- ☑ Marque a caixa de aceitação
- Clique em "Confirmar" ou "I Agree"

![5_terms_agreement](https://via.placeholder.com/400x300?text=Accept+Terms)

---

## PASSO 6: Ativar API

Confirme a ativação clicando em "Ativar" ou "Enable".

A página mostrará:

```
Status: ⏳ Ativando...
```

Aguarde 30-60 segundos.

---

## PASSO 7: Verificar Ativação

Quando concluído, você verá:

```
┌─────────────────────────────────────┐
│ Firebase AI Logic                   │
│                                     │
│ Status: ✅ ATIVADO                  │
│                                     │
│ API Gemini: Disponível              │
│ Modelo: gemini-2.5-flash            │
└─────────────────────────────────────┘
```

---

## ✅ PRONTO!

Agora você pode usar o app Android sem problemas!

### Para testar no app:
1. Abra o app
2. Vá para o Feed
3. Clique no botão 🟢 (AI)
4. Selecione uma imagem
5. Digite um prompt
6. Clique em "Gerar resposta"

---

## 🆘 PROBLEMAS COMUNS

### "API não encontrada"
→ Aguarde 1-2 minutos para a API propagar

### "Não tem permissão"
→ Verifique se está logado com a conta correta no Firebase

### "Projeto não encontrado"
→ Certifique-se de estar no projeto correto (teste20251-ab84a)

### "Enable button não aparece"
→ Atualize a página (F5 ou Ctrl+R)

---

## 📱 DEPOIS DE ATIVAR

Abra seu app Android e:

```
1. Vá para o Feed (primeira aba)
2. Procure pelo botão com ícone 🟢
3. Clique para abrir a IA
4. Selecione uma foto da galeria
5. Digite seu prompt
6. Aguarde a resposta do Gemini
```

**Exemplo de prompts:**
- "Qual é a doença desta planta?"
- "Identifique este inseto"
- "Recomende tratamento"

---

## 🎉 TUDO PRONTO!

Seu Firebase AI Logic está totalmente configurado e pronto para usar!

Se tiver dúvidas, consulte:
https://firebase.google.com/docs/ai/start
