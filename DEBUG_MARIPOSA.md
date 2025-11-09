# 🔍 DEBUG: Registro da Mariposa Desaparecido

## ⚠️ Problema Relatado
- Registro de mariposa foi salvo
- Não aparece em "SEUS REGISTROS"

## 🧪 Passos para Debug

### 1️⃣ Verifique os Logs (PRIMEIRO!)

Abra Android Studio e procure por estes logs **Na ordem**:

**No Logcat, procure por:**

```
1. Firebase Salvo?
   D/FirebaseDB: Salvando inseto em: usuarios/

2. Listener Detectou?
   D/FirebaseDB: Listener: Carregados X insetos

3. Repository Atualizou?
   D/RegistroRepository: Insetos atualizados: X registros

4. ViewModel Processou?
   D/MeusRegistrosVM: Combinando registros: X plantas + X insetos
```

**Se TODOS aparecerem: ✅ Fluxo está funcionando**
**Se algum FALTA: ❌ Encontramos o problema**

---

### 2️⃣ Verifique Firebase Console

1. Abra [Firebase Console](https://console.firebase.google.com)
2. Vá em **Realtime Database**
3. Procure: `usuarios/{seu-id}/insetos`
4. **A mariposa está lá?**
   - [ ] Sim → Problema é no listener/repository
   - [ ] Não → Problema é no salvamento

---

### 3️⃣ Se A Mariposa Está No Firebase

**Verifique a estrutura:**

```json
{
  "id": "insect_123...",
  "nome": "Mariposa",
  "categoria": "NEUTRAL",
  "imagens": ["uuid-1", "uuid-2"],  ← Tem UUIDs?
  "timestamp": 1699574324000,
  "userId": "seu-uid",
  "userName": "Seu Nome"
}
```

**Se não tiver `imagens` ou tiver URLs: ❌ Problema antigo**
**Se estiver igual acima: ✅ Estrutura correta**

---

### 4️⃣ Se A Mariposa NÃO Está No Firebase

**O salvamento falhou. Procure por:**

```
D/FirebaseDB: ❌ Erro ao salvar inseto:
E/FirebasDB: Exception:
```

**Possíveis erros:**
- `Permission denied` → Regras Firebase
- `Offline` → Sem conexão internet
- `Invalid data` → Estrutura errada

---

## 🔧 Possíveis Soluções

### Solução 1: Force Refresh
1. Vá em "SEUS REGISTROS"
2. Puxe para baixo (SwipeRefresh)
3. Espere 2 segundos
4. ✅ Mariposa deve aparecer

### Solução 2: Feche e Abra o App
1. Feche o app completamente
2. Abra novamente
3. Vá em "SEUS REGISTROS"
4. ✅ Mariposa deve aparecer

### Solução 3: Verifique Autenticação
```
Em Android Studio Console:
- User ID é igual em AMBOS os lugares?
- Login está ativo?
```

### Solução 4: Verifique Conexão
1. Certifique-se de WiFi/dados ativos
2. Tente novamente
3. Veja os logs

---

## 📊 Checklist de Debug

- [ ] Mariposa está no Firebase Console?
- [ ] Estrutura de dados está correta?
- [ ] Logs aparecem no Logcat?
- [ ] Autenticação está ativa?
- [ ] Conexão internet está ok?
- [ ] Swipe refresh foi feito?
- [ ] App foi reiniciado?

---

## 💬 Relatório de Erro

Quando responder, inclua:

1. **Mariposa está no Firebase?** (Sim/Não)
2. **Que logs você vê?** (copie-cole os relevantes)
3. **Qual foi a última ação?** (salvou e nada aconteceu?)
4. **Há mensagem de erro?** (sim/não, qual?)

---

## 🚨 Se Nada Funcionar

Envie:
1. Screenshot do Firebase Console mostrando a mariposa
2. Screenshot do Logcat com os logs
3. Mensagem de erro completa (se houver)

Assim poderei diagnósticos exatamente o que está errado! 🔍
