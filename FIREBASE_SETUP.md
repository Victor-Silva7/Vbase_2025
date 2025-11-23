# 🔥 GUIA DE CONFIGURAÇÃO DO FIREBASE
## Rede Social Simplificada - Vbase 2025

### ✅ PASSO 1: Copiar as Regras

Abra o arquivo `firebase-rules-simple.json` e copie todo o conteúdo.

### ✅ PASSO 2: Aplicar no Firebase Console

1. Acesse seu projeto Firebase:
   https://console.firebase.google.com/u/0/project/teste20251-ab84a/database/teste20251-ab84a-default-rtdb/rules

2. No menu lateral, clique em **Realtime Database** → **Regras**

3. Cole o conteúdo copiado, substituindo as regras atuais

4. Clique em **Publicar**

### 📊 ESTRUTURA DO BANCO DE DADOS

Após aplicar as regras, seu banco terá esta estrutura:

```
teste20251-ab84a (root)
│
├── postagens/                    # Feed público de postagens
│   └── {postagemId}/
│       ├── id: string
│       ├── tipo: "PLANTA" | "INSETO"
│       ├── titulo: string
│       ├── descricao: string
│       ├── imageUrl: string
│       ├── usuario: object
│       ├── dataPostagem: number
│       └── interacoes/
│           ├── curtidas: number
│           └── comentarios: number
│
├── curtidas/                     # Registro de curtidas
│   └── {postagemId}/
│       └── {userId}: timestamp
│
├── comentarios/                  # Comentários das postagens
│   └── {postagemId}/
│       └── {comentarioId}/
│           ├── id: string
│           ├── userId: string
│           ├── userName: string
│           ├── userAvatar: string
│           ├── conteudo: string
│           └── timestamp: number
│
└── usuarios/                     # Dados privados dos usuários
    └── {userId}/
        ├── plantas/              # Registros de plantas
        │   └── {plantaId}/
        └── insetos/              # Registros de insetos
            └── {insetoId}/
```

### 🔐 REGRAS DE SEGURANÇA

#### Postagens
- **Leitura**: ✅ Pública (qualquer um pode ver)
- **Escrita**: 🔒 Requer autenticação
- **Indexação**: Por `dataPostagem` e `tipo` (otimização de queries)

#### Curtidas
- **Leitura**: ✅ Pública
- **Escrita**: 🔒 Somente o próprio usuário pode curtir/descurtir
- **Validação**: Impede manipulação de curtidas de outros usuários

#### Comentários
- **Leitura**: ✅ Pública
- **Escrita**: 🔒 Requer autenticação
- **Indexação**: Por `timestamp` (ordenação cronológica)

#### Usuários
- **Leitura**: 🔒 Somente o próprio usuário
- **Escrita**: 🔒 Somente o próprio usuário
- **Conteúdo**: Registros privados de plantas e insetos

### ⚡ OTIMIZAÇÕES APLICADAS

1. **Índices**: Queries mais rápidas em `dataPostagem` e `timestamp`
2. **Validações**: Garantem integridade dos dados
3. **Permissões**: Segurança contra manipulações não autorizadas

### ✨ PRONTO PARA USAR!

Após aplicar as regras, o app já está configurado para:
- ✅ Criar registros de plantas/insetos
- ✅ Gerar postagens automáticas no feed
- ✅ Curtir postagens
- ✅ Adicionar comentários
- ✅ Paginação otimizada
