# Sistema de Busca Pública - V Group Manejo Verde

## Visão Geral

O sistema de busca pública permite aos usuários do aplicativo V Group - Manejo Verde buscar e descobrir registros de plantas, insetos e usuários compartilhados publicamente pela comunidade.

## Componentes Implementados

### 1. Modelos de Dados (`PublicSearchModels.kt`)

#### `PublicSearchResult`
- Contém resultados agregados de plantas, insetos e usuários
- Inclui algoritmos de relevância e ordenação
- Suporte a cache com timestamp

#### `SearchableItem` (Sealed Class)
- `PlantResult`: Resultado de busca para plantas
- `InsectResult`: Resultado de busca para insetos  
- `UserResult`: Resultado de busca para usuários
- Cada item tem score de relevância calculado

#### `SearchFilters`
- Filtros por tipo (plantas, insetos, usuários)
- Filtros por localização e período
- Filtros por status de verificação
- Contadores de filtros ativos

#### `SearchSuggestion`
- Sugestões tipadas (busca recente, nome de planta, localização, etc.)
- Sistema de prioridade para ordenação
- Integração com histórico de buscas

### 2. Repository (`PublicSearchRepository.kt`)

#### Funcionalidades Principais:
- **Busca com debounce**: Evita muitas consultas simultâneas
- **Cache inteligente**: Resultados válidos por 5 minutos
- **Sugestões dinâmicas**: Baseadas em consulta atual e histórico
- **Dados mock**: Para demonstração antes da integração Firebase

#### Métodos Principais:
```kotlin
suspend fun searchPublicRecords(query: String, filters: SearchFilters): Result<PublicSearchResult>
suspend fun getSuggestions(query: String): List<SearchSuggestion>
fun getRecentSearches(): List<SearchSuggestion>
```

### 3. ViewModel (`PublicSearchViewModel.kt`)

#### Estados de Busca:
- `INITIAL`: Tela inicial com sugestões
- `SUGGESTIONS`: Mostrando sugestões em tempo real
- `SEARCHING`: Executando busca (loading)
- `RESULTS`: Exibindo resultados
- `NO_RESULTS`: Nenhum resultado encontrado
- `ERROR`: Erro na busca

#### Funcionalidades:
- Busca com debounce (300ms)
- Gerenciamento de filtros
- Cache de resultados
- Tratamento de erros
- LiveData para observação da UI

### 4. Interface do Usuário

#### `PublicSearchFragment.kt`
- Fragment principal com ViewBinding
- Gerenciamento de estados da UI
- Integração com ViewModels
- Navegação entre resultados

#### `fragment_public_search.xml`
- Layout responsivo com múltiplos estados
- Search bar com botões de ação
- RecyclerViews para resultados e sugestões
- Estados vazios e de erro

#### `item_search_result.xml`
- Card layout para cada resultado
- Informações adaptáveis por tipo (planta/inseto/usuário)
- Score de relevância visual
- Botões de ação (curtir, compartilhar)

#### `item_search_suggestion.xml`
- Layout simples para sugestões
- Ícones tipados por categoria
- Botão de inserção de texto

### 5. Adapters

#### `SearchResultsAdapter.kt`
- Adapter com DiffUtil para performance
- Binding diferenciado por tipo de item
- Formatação inteligente de datas
- Cores dinâmicas por status

#### `SearchSuggestionsAdapter.kt`
- Adapter para sugestões e buscas recentes
- Clique para substituir ou inserir texto
- Ícones contextuais

## Integração com Navegação

### Arquivo de Navegação
```xml
<fragment
    android:id="@+id/navigation_public_search"
    android:name="com.ifpr.androidapptemplate.ui.search.PublicSearchFragment"
    android:label="@string/title_public_search"
    tools:layout="@layout/fragment_public_search" />
```

### Menu Bottom Navigation
```xml
<item
    android:id="@+id/navigation_public_search"
    android:icon="@drawable/ic_search_24dp"
    android:title="@string/title_public_search" />
```

## Recursos Visuais Criados

### Ícones Drawable:
- `ic_close.xml`: Fechar busca
- `ic_planta_24dp.xml`: Ícone de plantas
- `ic_inseto_24dp.xml`: Ícone de insetos
- `ic_person_24dp.xml`: Ícone de usuários
- `ic_error_24dp.xml`: Estados de erro
- `ic_location_on_24dp.xml`: Localização
- `ic_date_range_24dp.xml`: Filtros de data
- `ic_share_24dp.xml`: Compartilhamento
- `ic_star_border_24dp.xml`: Classificação

### Cores:
- `primary_green`: Verde principal do app
- `healthy_color`: Verde para plantas saudáveis
- `beneficial_color`: Azul para insetos benéficos
- `pest_color`: Vermelho para pragas
- `search_background`: Fundo da busca
- `filter_selected`: Filtros ativos

## Como Usar

### 1. Navegação para Busca
```kotlin
findNavController().navigate(R.id.navigation_public_search)
```

### 2. Busca Programática
```kotlin
val viewModel: PublicSearchViewModel by viewModels()
viewModel.search("rosa")
```

### 3. Aplicar Filtros
```kotlin
val filters = SearchFilters(
    types = setOf(SearchType.PLANTS),
    verifiedOnly = true
)
viewModel.applyFilters(filters)
```

### 4. Observar Resultados
```kotlin
viewModel.searchResults.observe(viewLifecycleOwner) { results ->
    // Atualizar UI com resultados
}
```

## Próximos Passos Sugeridos

### 1. Integração Firebase (Pendente)
- Substituir dados mock por consultas reais
- Implementar busca full-text no Realtime Database
- Adicionar índices de busca para performance

### 2. Filtros Avançados (Pendente)
- Dialog de filtros com múltiplas opções
- Filtros por família, status, localização
- Salvar filtros preferidos do usuário

### 3. Funcionalidades Adicionais
- Busca por voz
- Busca por imagem
- Histórico de buscas persistente
- Compartilhamento de resultados
- Busca offline

### 4. Otimizações
- Pagination para muitos resultados
- Cache mais inteligente
- Busca geolocalizada
- Analytics de busca

## Estrutura de Arquivos

```
app/src/main/java/com/ifpr/androidapptemplate/
├── data/
│   ├── model/
│   │   └── PublicSearchModels.kt
│   └── repository/
│       └── PublicSearchRepository.kt
├── ui/
│   └── search/
│       ├── PublicSearchFragment.kt
│       ├── PublicSearchViewModel.kt
│       ├── SearchResultsAdapter.kt
│       └── SearchSuggestionsAdapter.kt
└── res/
    ├── layout/
    │   ├── fragment_public_search.xml
    │   ├── item_search_result.xml
    │   └── item_search_suggestion.xml
    ├── drawable/
    │   └── [ícones diversos]
    ├── values/
    │   ├── colors.xml
    │   └── strings.xml
    ├── navigation/
    │   └── mobile_navigation.xml
    └── menu/
        └── bottom_nav_menu.xml
```

A implementação está completa e pronta para uso, com dados mock para demonstração. A integração com Firebase pode ser feita posteriormente substituindo as funções mock no Repository.