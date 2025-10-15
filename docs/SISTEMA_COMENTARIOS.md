# Sistema de Comentários - V Group Manejo Verde

## 🎯 Visão Geral

Implementação completa do sistema de comentários para o aplicativo V Group - Manejo Verde, permitindo interações sociais ricas nas postagens da comunidade.

## 🏗️ Arquitetura Implementada

### 1. **Modelos de Dados** (`ComentarioModels.kt`)

#### `Comentario`
- Estrutura completa para comentários e respostas
- Suporte a anexos (imagens)
- Informações de usuário simplificadas
- Timestamps e estado de edição
- Contadores de interação (likes, respostas)

#### `UsuarioComentario`
- Versão otimizada do usuário para comentários
- Avatar, nome, verificação e nível

#### `ComentariosResult`
- Resultados paginados para carregamento eficiente
- Controle de páginas e navegação

#### `ComentarioStats`
- Estatísticas agregadas para postagens
- Contadores de comentários e usuários ativos

### 2. **Repository** (`ComentariosRepository.kt`)

#### Funcionalidades Principais:
- **Carregamento paginado** de comentários
- **Cache inteligente** por postagem e ordenação
- **Operações CRUD** completas (criar, ler, atualizar, deletar)
- **Interações sociais** (curtir/descurtir)
- **Ordenação flexível** (recentes, curtidos, respondidos)

#### Métodos Disponíveis:
```kotlin
suspend fun loadComments(postId: String, page: Int = 1): Result<ComentariosResult>
suspend fun addComment(novoComentario: NovoComentario): Result<Comentario>
suspend fun updateComment(atualizacao: AtualizacaoComentario): Result<Comentario>
suspend fun deleteComment(comentarioId: String, postId: String): Result<Unit>
suspend fun toggleLike(comentarioId: String, postId: String): Result<Comentario>
```

### 3. **ViewModel** (`ComentariosViewModel.kt`)

#### Responsabilidades:
- Gerenciamento de estado da interface
- Coordenação de operações assíncronas
- Integração com Repository
- Controle de paginação e ordenação

#### Estados Observáveis:
- `loadingState`: Estado de carregamento (Idle, Loading, LoadingMore, Success, Error)
- `currentComments`: Lista atual de comentários
- `postId`: ID da postagem atual
- `errorMessage`: Mensagens de erro
- `sortBy`: Ordenação atual

### 4. **Interface do Usuário**

#### `ComentariosFragment.kt`
- Fragment principal com ViewBinding
- Integração completa com ViewModel
- Gerenciamento de estados visuais
- Input de novos comentários

#### `fragment_comentarios.xml`
- Layout responsivo com múltiplos estados
- Toolbar com navegação
- RecyclerView para comentários
- Input de comentários com avatar
- Estados vazios e de erro

#### `item_comentario.xml`
- Layout hierárquico para comentários
- Suporte a respostas aninhadas
- Informações do usuário com avatar
- Interações sociais (curtir, responder)
- Anexos visuais

#### `item_attachment.xml`
- Componente reutilizável para anexos
- Visualização de imagens com bordas arredondadas

### 5. **Adapter** (`ComentariosAdapter.kt`)

#### Características:
- **DiffUtil** para performance otimizada
- **Scroll infinito** automático
- **Binding hierárquico** para comentários/respostas
- **Interações sociais** responsivas
- **Gestão de estados** visuais

#### Callbacks:
- `onLikeClick`: Curtir/descurtir comentário
- `onReplyClick`: Responder comentário
- `onMoreOptionsClick`: Opções adicionais
- `onLoadMore`: Carregar mais comentários

## 🔧 Integração com Sistema Existente

### Feed de Postagens
- Botão "Comentar" adicionado aos cards
- Contador de comentários nas estatísticas
- Navegação para tela de comentários

### Sistema de Usuários
- Reutilização de modelos otimizados
- Níveis e verificação visuais
- Avatar consistente em toda a aplicação

## 🎨 Recursos Visuais Criados

### Ícones Drawable:
- `ic_send_24dp.xml`: Enviar comentário
- `ic_reply_24dp.xml`: Responder comentário
- `ic_expand_more_24dp.xml`: Expandir respostas

### Cores e Estilos:
- Reutilização de cores existentes do tema
- Estilos consistentes com Material Design 3
- Hierarquia visual clara para comentários

## 🚀 Funcionalidades Implementadas

### ✅ Sistema de Comentários Hierárquico
- Comentários principais e respostas aninhadas
- Limite configurável de profundidade (3 níveis)
- Indicadores visuais de resposta

### ✅ Interações Sociais
- Curtir/descurtir comentários
- Contador de likes em tempo real
- Feedback visual imediato

### ✅ Paginação e Performance
- Carregamento paginado (20 itens por página)
- Scroll infinito automático
- Cache inteligente por postagem
- Estados de loading otimizados

### ✅ Interface Responsiva
- Múltiplos estados visuais (loading, vazio, erro)
- Pull-to-refresh para atualização
- Animações sutis em interações
- Suporte a anexos visuais

### ✅ Ordenação Flexível
- Mais recentes primeiro
- Mais curtidos primeiro
- Mais respondidos primeiro

## 📁 Estrutura de Arquivos

```
app/src/main/java/com/ifpr/androidapptemplate/
├── data/
│   ├── model/
│   │   └── ComentarioModels.kt
│   └── repository/
│       └── ComentariosRepository.kt
├── ui/
│   └── comentarios/
│       ├── ComentariosFragment.kt
│       ├── ComentariosViewModel.kt
│       └── ComentariosAdapter.kt
└── res/
    ├── layout/
    │   ├── fragment_comentarios.xml
    │   ├── item_comentario.xml
    │   └── item_attachment.xml
    └── drawable/
        ├── ic_send_24dp.xml
        ├── ic_reply_24dp.xml
        └── ic_expand_more_24dp.xml
```

## 📱 Próximos Passos Sugeridos

### 1. Integração com Navegação
- Configurar Navigation Component para navegação
- Adicionar transições animadas
- Implementar passagem de dados entre telas

### 2. Sistema de Anexos Completo
- Upload de imagens para comentários
- Visualização em tela cheia
- Suporte a múltiplos anexos

### 3. Moderação e Segurança
- Sistema de denúncias
- Filtros de conteúdo ofensivo
- Bloqueio de usuários

### 4. Notificações
- Alertas de novas respostas
- Notificações de menções (@usuário)
- Resumo diário de interações

## 🧪 Dados Mock para Demonstração

### Comentários Gerados:
- 15+ comentários por postagem
- Usuários variados com diferentes níveis
- Mistura de comentários principais e respostas
- Conteúdo realista sobre plantas/insetos
- Timestamps distribuídos no tempo

A implementação está completa e pronta para uso, com dados mock para demonstração. A arquitetura foi projetada para facilitar a futura integração com Firebase e expansão de funcionalidades. 🌱💬