# Data Models Documentation - V Group Manejo Verde

## Overview
This document describes the comprehensive data model architecture implemented for the V Group - Manejo Verde Android application. The models are designed to support plant and insect observations with rich metadata, social features, and Firebase integration.

## 📊 Data Model Architecture

### Core Models
1. **[Planta.kt](file://c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\java\com\ifpr\androidapptemplate\data\model\Planta.kt)** - Plant observation data
2. **[Inseto.kt](file://c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\java\com\ifpr\androidapptemplate\data\model\Inseto.kt)** - Insect observation data
3. **[Usuario.kt](file://c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\java\com\ifpr\androidapptemplate\data\model\Usuario.kt)** - User profile and preferences
4. **[CommonModels.kt](file://c:\Users\Victor\Documents\GitHub\Vbase_2025\app\src\main\java\com\ifpr\androidapptemplate\data\model\CommonModels.kt)** - Shared data structures

## 🌱 Plant Data Model (`Planta`)

### Core Properties
```kotlin
data class Planta(
    val id: String,                     // Unique identifier
    val nome: String,                   // Plant name
    val nomePopular: String,            // Common name
    val nomeCientifico: String,         // Scientific name
    val data: String,                   // Observation date
    val dataTimestamp: Long,            // Date as timestamp
    val local: String,                  // Location name
    val coordenadas: Coordenadas?,      // GPS coordinates
    val categoria: PlantHealthCategory, // Health status
    val observacao: String,             // User observations
    val imagens: List<String>,          // Image URLs
    val thumbnailUrl: String,           // Thumbnail image
    // ... additional properties
)
```

### Plant Health Categories
Following project specifications with tooltips:
- **HEALTHY** (Saudável): Folhas verdes, sem manchas, crescimento normal
- **SICK** (Doente): Folhas manchadas, presença de doenças, crescimento anormal

### Key Features
- **Firebase Integration**: `toFirebaseMap()` and `fromFirebaseMap()`
- **Validation Methods**: `isEditable()`, `hasImages()`, `isOwnedBy()`
- **Display Helpers**: `getFormattedDate()`, `getHealthStatusText()`
- **Analytics**: `getDaysSinceObservation()`, engagement tracking

## 🐛 Insect Data Model (`Inseto`)

### Core Properties
```kotlin
data class Inseto(
    val id: String,                     // Unique identifier
    val nome: String,                   // Insect name
    val nomePopular: String,            // Common name
    val nomeCientifico: String,         // Scientific name
    val data: String,                   // Observation date
    val dataTimestamp: Long,            // Date as timestamp
    val local: String,                  // Location name
    val coordenadas: Coordenadas?,      // GPS coordinates
    val categoria: InsectCategory,      // Impact category
    val observacao: String,             // User observations
    val imagens: List<String>,          // Image URLs
    val impactoObservado: ImpactoInseto?, // Pest impact data
    // ... additional properties
)
```

### Insect Categories
Following project specifications with detailed tooltips:
- **BENEFICIAL** (Benéfico): Polinizadores, predadores naturais, controladores biológicos
- **NEUTRAL** (Neutro): Não causam danos, parte do ecossistema natural
- **PEST** (Praga): Causam danos às plantas, requerem controle

### Special Features
- **Category Colors**: Visual distinction for UI (`getCategoryColor()`)
- **Impact Assessment**: `ImpactoInseto` for pest damage tracking
- **Alert System**: `requiresAttention()` for pest management
- **Tooltip Integration**: `getCategoryDescription()` for user guidance

## 👤 User Data Model (`Usuario`)

### Core Properties
```kotlin
data class Usuario(
    val id: String,                         // Unique user ID
    val email: String,                      // Email address
    val nome: String,                       // Full name
    val nomeUsuario: String,                // Username
    val bio: String,                        // Profile biography
    val fotoPerfil: String,                 // Profile image URL
    val localizacao: String,                // Location text
    val coordenadas: Coordenadas?,          // GPS coordinates
    val nivelExperiencia: NivelExperiencia, // Experience level
    val especializacoes: List<String>,      // Areas of expertise
    val configuracoes: ConfiguracoesUsuario, // User preferences
    val estatisticas: EstatisticasUsuario,  // Activity statistics
    val conquistas: List<Conquista>,        // Achievements/badges
    // ... social features
)
```

### Experience Levels
- **INICIANTE**: 0-10 registrations
- **INTERMEDIARIO**: 11-50 registrations
- **AVANCADO**: 51-200 registrations
- **ESPECIALISTA**: 200+ registrations
- **PESQUISADOR**: Verified expert

### User Features
- **Profile Analytics**: `getProfileCompleteness()`, activity tracking
- **Social Network**: Following/followers system
- **Achievements**: Gamification with badges and points
- **Preferences**: Privacy settings, notifications, app configuration

## 🔧 Common Data Structures

### Geographic Data (`Coordenadas`)
```kotlin
data class Coordenadas(
    val latitude: Double,
    val longitude: Double,
    val precisao: Float,
    val altitude: Double,
    val endereco: String,
    val timestamp: Long
)
```

### Weather Conditions (`ClimaObservacao`)
```kotlin
data class ClimaObservacao(
    val temperatura: Float,
    val umidade: Int,
    val condicao: CondicaoClimatica,
    val vento: String,
    val precipitacao: String
)
```

### Validation System (`ValidacaoRegistro`)
```kotlin
data class ValidacaoRegistro(
    val validado: Boolean,
    val validadoPor: String,
    val nivelConfianca: NivelConfianca,
    val comentariosValidacao: String
)
```

### Interaction Statistics (`EstatisticasInteracao`)
```kotlin
data class EstatisticasInteracao(
    val visualizacoes: Int,
    val curtidas: Int,
    val comentarios: Int,
    val compartilhamentos: Int,
    val engajamento: Float
)
```

## 🏗️ Architecture Patterns

### Parcelable Support
All models implement `Parcelable` for efficient data transfer between Android components:
```kotlin
@Parcelize
data class Planta(...) : Parcelable
```

### Firebase Integration
Seamless Firebase Realtime Database integration:
```kotlin
// Save to Firebase
val firebaseMap = planta.toFirebaseMap()
database.reference.child("plantas").setValue(firebaseMap)

// Load from Firebase
val planta = Planta.fromFirebaseMap(firebaseData)
```

### Type Safety
Extensive use of enums for type safety:
```kotlin
enum class PlantHealthCategory { HEALTHY, SICK }
enum class InsectCategory { BENEFICIAL, NEUTRAL, PEST }
enum class StatusRegistro { ATIVO, INATIVO, PENDENTE }
```

### Validation & Business Logic
Built-in validation and business logic methods:
```kotlin
// Check if plant is editable
if (planta.isEditable()) { /* show edit option */ }

// Get engagement metrics
val engagementRate = statistics.calcularEngajamento()

// Check if content needs moderation
if (statistics.needsReview()) { /* flag for review */ }
```

## 🔄 Data Flow Architecture

### Registration Flow
```
User Input → ViewModel → Data Model → Firebase Storage → Firebase Database
     ↓            ↓            ↓              ↓                ↓
Validation → Transformation → Serialization → Upload → Save Reference
```

### Retrieval Flow
```
Firebase Database → Data Model → ViewModel → UI Components
        ↓               ↓           ↓            ↓
   Deserialization → Validation → Caching → Display
```

## 🔐 Security & Privacy

### Data Access Control
- **User Ownership**: `isOwnedBy()` checks for data access
- **Visibility Settings**: `VisibilidadeRegistro` enum for privacy control
- **Status Management**: `StatusRegistro` for content moderation

### Validation Levels
```kotlin
enum class NivelConfianca {
    NAO_VALIDADO,
    BAIXA_CONFIANCA,
    MEDIA_CONFIANCA,
    ALTA_CONFIANCA,
    VALIDADO_ESPECIALISTA
}
```

## 📱 UI Integration

### Display Helpers
```kotlin
// Get formatted date
val displayDate = planta.getFormattedDate()

// Get category with color
val categoryColor = inseto.getCategoryColor()

// Check if images are available
if (registro.hasImages()) { /* show image gallery */ }
```

### State Management
```kotlin
// Check editing permissions
val canEdit = registro.isEditable() && registro.isOwnedBy(currentUserId)

// Get engagement metrics
val isPopular = statistics.isPopular()
```

## 🧪 Testing Support

### Mock Data Generation
```kotlin
// Generate test plant
val testPlanta = Planta(
    id = Planta.generateId(),
    nome = "Rosa Test",
    categoria = PlantHealthCategory.HEALTHY,
    // ... other test properties
)
```

### Validation Testing
```kotlin
// Test business logic
assertTrue(planta.hasImages())
assertEquals("Saudável", planta.getHealthStatusText())
assertTrue(usuario.isActiveRecently())
```

## 🚀 Performance Optimizations

### Lazy Loading
- Images loaded on demand
- Statistics calculated when accessed
- Coordinates parsed only when needed

### Memory Efficiency
- Immutable data classes
- Efficient serialization
- Proper null handling

### Caching Strategy
- Local data persistence
- Image caching with Glide
- Firebase offline support

## 📊 Analytics Integration

### User Engagement
```kotlin
// Track user activity
estatisticas.calcularEngajamento()
usuario.getDaysSinceLastLogin()
planta.getDaysSinceObservation()
```

### Content Quality
```kotlin
// Assess content quality
validacao.nivelConfianca
estatisticas.pontuacaoQualidade
estatisticas.needsReview()
```

## 🔮 Future Extensions

### Planned Enhancements
- **AI Integration**: Automatic species identification
- **Weather API**: Real-time weather data integration
- **Advanced Analytics**: Machine learning insights
- **Collaboration**: Team projects and shared collections
- **Export Features**: PDF reports and data export

### Extensibility
- **Plugin Architecture**: Support for custom data fields
- **API Integration**: Third-party scientific databases
- **Offline Support**: Enhanced offline capabilities
- **Sync Optimization**: Delta sync for large datasets

## 📝 Migration Guide

### From Legacy Models
1. Update import statements to use new model package
2. Replace direct property access with helper methods
3. Use `toFirebaseMap()` for database operations
4. Implement `Parcelable` for data transfer

### Version Compatibility
- Backward compatible with existing Firebase data
- Graceful handling of missing fields
- Progressive enhancement of features

This comprehensive data model architecture provides a solid foundation for the V Group - Manejo Verde application, supporting current features while enabling future enhancements and scalability.