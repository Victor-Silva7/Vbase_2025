package com.ifpr.androidapptemplate.data.model

import java.util.*

/**
 * Classes para gerenciamento de paginação
 */
data class PaginatedResult<T>(
    val data: List<T> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val pageSize: Int = 10,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isFirstPage(): Boolean = currentPage == 1
    fun isLastPage(): Boolean = currentPage >= totalPages
    
    companion object {
        fun <T> loading(currentPage: Int = 1): PaginatedResult<T> {
            return PaginatedResult(
                currentPage = currentPage,
                isLoading = true
            )
        }
        
        fun <T> error(error: String, currentPage: Int = 1): PaginatedResult<T> {
            return PaginatedResult(
                currentPage = currentPage,
                error = error,
                isLoading = false
            )
        }
        
        fun <T> success(
            data: List<T>,
            currentPage: Int,
            totalItems: Int,
            pageSize: Int = 10
        ): PaginatedResult<T> {
            val totalPages = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
            return PaginatedResult(
                data = data,
                currentPage = currentPage,
                totalPages = totalPages,
                totalItems = totalItems,
                hasNextPage = currentPage < totalPages,
                hasPreviousPage = currentPage > 1,
                pageSize = pageSize,
                isLoading = false
            )
        }
    }
}

/**
 * Estado de carregamento para scroll infinito
 */
sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object LoadingMore : LoadingState()
    data class Error(val message: String) : LoadingState()
    data class Success(val hasMore: Boolean) : LoadingState()
}

/**
 * Configuração de paginação
 */
data class PaginationConfig(
    val pageSize: Int = 10,
    val initialLoadSize: Int = 20,
    val prefetchDistance: Int = 5,
    val enablePlaceholders: Boolean = false
)

/**
 * Item de loading para o adapter
 */
data class LoadingItem(
    val id: String = "loading_${System.currentTimeMillis()}",
    val isVisible: Boolean = true
)

/**
 * Modelo de dados para postagens no feed
 * Inclui informações completas do usuário e do registro
 */
data class PostagemFeed(
    val id: String = "",
    val tipo: TipoPostagem = TipoPostagem.PLANTA,
    
    // Informações do usuário
    val usuario: UsuarioPostagem = UsuarioPostagem(),
    
    // Conteúdo da postagem
    val titulo: String = "",
    val descricao: String = "",
    val imageUrl: String = "",
    val localizacao: String = "",
    val dataPostagem: Long = System.currentTimeMillis(),
    
    // Informações específicas baseadas no tipo
    val detalhesPlanta: DetalhesPlanta? = null,
    val detalhesInseto: DetalhesInseto? = null,
    
    // Interações sociais
    val interacoes: InteracoesPostagem = InteracoesPostagem(),
    
    // Status e configurações
    val isPublico: Boolean = true,
    val isVerificado: Boolean = false,
    val tags: List<String> = emptyList()
) {
    fun getTempoPostagem(): String {
        val agora = System.currentTimeMillis()
        val diferenca = agora - dataPostagem
        
        val segundos = diferenca / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24
        
        return when {
            segundos < 60 -> "Agora"
            minutos < 60 -> "${minutos}min"
            horas < 24 -> "${horas}h"
            dias < 7 -> "${dias}d"
            dias < 30 -> "${dias / 7}sem"
            else -> "${dias / 30}m"
        }
    }
    
    fun getIconeRecurso(): String {
        return when (tipo) {
            TipoPostagem.PLANTA -> "ic_planta_24dp"
            TipoPostagem.INSETO -> "ic_inseto_24dp"
        }
    }
}

/**
 * Informações do usuário que fez a postagem
 */
data class UsuarioPostagem(
    val id: String = "",
    val nome: String = "",
    val nomeExibicao: String = "",
    val avatarUrl: String = "",
    val isVerificado: Boolean = false,
    val nivel: NivelUsuario = NivelUsuario.INICIANTE,
    val localizacao: String = "",
    val biografia: String = "",
    val dataRegistro: Long = 0L,
    val totalRegistros: Int = 0,
    val totalCurtidas: Int = 0
) {
    fun getTextoNivel(): String {
        return when (nivel) {
            NivelUsuario.INICIANTE -> "Iniciante"
            NivelUsuario.INTERMEDIARIO -> "Intermediário"
            NivelUsuario.AVANCADO -> "Avançado"
            NivelUsuario.ESPECIALISTA -> "Especialista"
        }
    }
    
    fun getCorNivel(): String {
        return when (nivel) {
            NivelUsuario.INICIANTE -> "#757575"
            NivelUsuario.INTERMEDIARIO -> "#2196F3"
            NivelUsuario.AVANCADO -> "#4CAF50"
            NivelUsuario.ESPECIALISTA -> "#FF9800"
        }
    }
}

/**
 * Detalhes específicos para postagens de plantas
 */
data class DetalhesPlanta(
    val nomeComum: String = "",
    val nomeCientifico: String = "",
    val familia: String = "",
    val altura: String = "",
    val status: StatusPlanta = StatusPlanta.SAUDAVEL,
    val estagio: EstagioPlanta = EstagioPlanta.ADULTO,
    val cuidadosEspeciais: List<String> = emptyList()
) {
    fun getTextoStatus(): String {
        return when (status) {
            StatusPlanta.SAUDAVEL -> "Saudável"
            StatusPlanta.DOENTE -> "Doente"
            StatusPlanta.CRESCIMENTO -> "Em crescimento"
            StatusPlanta.FLORACAO -> "Floração"
            StatusPlanta.FRUTIFICACAO -> "Frutificação"
        }
    }
    
    fun getCorStatus(): String {
        return when (status) {
            StatusPlanta.SAUDAVEL -> "#4CAF50"
            StatusPlanta.DOENTE -> "#F44336"
            StatusPlanta.CRESCIMENTO -> "#2196F3"
            StatusPlanta.FLORACAO -> "#E91E63"
            StatusPlanta.FRUTIFICACAO -> "#FF9800"
        }
    }
}

/**
 * Detalhes específicos para postagens de insetos
 */
data class DetalhesInseto(
    val nomeComum: String = "",
    val nomeCientifico: String = "",
    val familia: String = "",
    val tamanho: String = "",
    val tipo: TipoInseto = TipoInseto.NEUTRO,
    val habitat: String = "",
    val comportamento: String = ""
) {
    fun getTextoTipo(): String {
        return when (tipo) {
            TipoInseto.BENEFICO -> "Benéfico"
            TipoInseto.PRAGA -> "Praga"
            TipoInseto.NEUTRO -> "Neutro"
            TipoInseto.POLINIZADOR -> "Polinizador"
        }
    }
    
    fun getCorTipo(): String {
        return when (tipo) {
            TipoInseto.BENEFICO -> "#4CAF50"
            TipoInseto.PRAGA -> "#F44336"
            TipoInseto.NEUTRO -> "#757575"
            TipoInseto.POLINIZADOR -> "#FF9800"
        }
    }
}

/**
 * Interações sociais da postagem
 */
data class InteracoesPostagem(
    val curtidas: Int = 0,
    val comentarios: Int = 0,
    val compartilhamentos: Int = 0,
    val visualizacoes: Int = 0,
    val curtidoPeloUsuario: Boolean = false,
    val salvosPeloUsuario: Boolean = false,
    val ultimaInteracao: Long = 0L
) {
    fun getTextoInteracoes(): String {
        val partes = mutableListOf<String>()
        
        if (curtidas > 0) {
            partes.add("$curtidas curtida${if (curtidas > 1) "s" else ""}")
        }
        
        if (comentarios > 0) {
            partes.add("$comentarios comentário${if (comentarios > 1) "s" else ""}")
        }
        
        if (compartilhamentos > 0) {
            partes.add("$compartilhamentos compartilhamento${if (compartilhamentos > 1) "s" else ""}")
        }
        
        return when (partes.size) {
            0 -> "Seja o primeiro a curtir"
            1 -> partes[0]
            2 -> "${partes[0]} • ${partes[1]}"
            else -> "${partes[0]} • ${partes[1]} • ${partes[2]}"
        }
    }
}

/**
 * Enums para categorização
 */
enum class TipoPostagem {
    PLANTA, INSETO
}

enum class NivelUsuario {
    INICIANTE, INTERMEDIARIO, AVANCADO, ESPECIALISTA
}

enum class StatusPlanta {
    SAUDAVEL, DOENTE, CRESCIMENTO, FLORACAO, FRUTIFICACAO
}

enum class EstagioPlanta {
    SEMENTE, MUDA, JOVEM, ADULTO, MADURO
}

enum class TipoInseto {
    BENEFICO, PRAGA, NEUTRO, POLINIZADOR
}

/**
 * Dados mock para demonstração com suporte a paginação
 */
object PostagemMockData {
    
    private val usuarios = listOf(
        UsuarioPostagem(
            id = "user_001",
            nome = "Maria Silva",
            nomeExibicao = "Maria Silva",
            avatarUrl = "https://example.com/avatar1.jpg",
            isVerificado = true,
            nivel = NivelUsuario.ESPECIALISTA,
            localizacao = "São Paulo, SP",
            totalRegistros = 45,
            totalCurtidas = 1200
        ),
        UsuarioPostagem(
            id = "user_002",
            nome = "João Santos",
            nomeExibicao = "João Santos",
            avatarUrl = "https://example.com/avatar2.jpg",
            isVerificado = false,
            nivel = NivelUsuario.INTERMEDIARIO,
            localizacao = "Rio de Janeiro, RJ",
            totalRegistros = 12,
            totalCurtidas = 340
        ),
        UsuarioPostagem(
            id = "user_003",
            nome = "Ana Costa",
            nomeExibicao = "Ana Costa",
            avatarUrl = "https://example.com/avatar3.jpg",
            isVerificado = true,
            nivel = NivelUsuario.AVANCADO,
            localizacao = "Belo Horizonte, MG",
            totalRegistros = 28,
            totalCurtidas = 756
        ),
        UsuarioPostagem(
            id = "user_004",
            nome = "Carlos Oliveira",
            nomeExibicao = "Carlos Oliveira",
            avatarUrl = "https://example.com/avatar4.jpg",
            isVerificado = false,
            nivel = NivelUsuario.INICIANTE,
            localizacao = "Curitiba, PR",
            totalRegistros = 8,
            totalCurtidas = 125
        ),
        UsuarioPostagem(
            id = "user_005",
            nome = "Lúcia Fernandes",
            nomeExibicao = "Lúcia Fernandes",
            avatarUrl = "https://example.com/avatar5.jpg",
            isVerificado = true,
            nivel = NivelUsuario.ESPECIALISTA,
            localizacao = "Porto Alegre, RS",
            totalRegistros = 67,
            totalCurtidas = 2340
        )
    )
    
    private val plantasExemplo = listOf(
        "Rosa", "Manjericão", "Tomate", "Alface", "Cenoura", "Orquídea", "Girassol", 
        "Hortelã", "Alecrim", "Lavanda", "Cacto", "Samambaia", "Violeta", "Petúnia",
        "Begônia", "Azaleia", "Lírio", "Cravo", "Tulipa", "Margarida"
    )
    
    private val insetosExemplo = listOf(
        "Joaninha", "Borboleta", "Abelha", "Formiga", "Besouro", "Libelula", "Gafanhoto",
        "Cigarra", "Grilo", "Libélula", "Vespa", "Louva-deus", "Pulgão", "Lagarta",
        "Mosca", "Mosquito", "Barata", "Cupim", "Aranha", "Escorpião"
    )
    
    fun gerarPostagensPaginadas(page: Int, pageSize: Int = 10): PaginatedResult<PostagemFeed> {
        val todasPostagens = gerarTodasPostagens()
        val totalItems = todasPostagens.size
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalItems)
        
        if (startIndex >= totalItems) {
            return PaginatedResult.success(
                data = emptyList(),
                currentPage = page,
                totalItems = totalItems,
                pageSize = pageSize
            )
        }
        
        val pageData = todasPostagens.subList(startIndex, endIndex)
        
        return PaginatedResult.success(
            data = pageData,
            currentPage = page,
            totalItems = totalItems,
            pageSize = pageSize
        )
    }
    
    fun gerarPostagensMock(): List<PostagemFeed> {
        return gerarTodasPostagens().take(10) // Retorna apenas primeiras 10 para compatibilidade
    }
    
    private fun gerarTodasPostagens(): List<PostagemFeed> {
        val postagens = mutableListOf<PostagemFeed>()
        val random = Random(42) // Seed fixo para resultados consistentes
        
        // Gera 50 postagens mock para demonstração de paginação
        for (i in 1..50) {
            val isPlanta = random.nextBoolean()
            val usuario = usuarios[random.nextInt(usuarios.size)]
            val tempoAleatorio = System.currentTimeMillis() - random.nextLong(0, 7 * 24 * 60 * 60 * 1000) // Últimos 7 dias
            
            if (isPlanta) {
                val planta = plantasExemplo[random.nextInt(plantasExemplo.size)]
                postagens.add(
                    PostagemFeed(
                        id = "post_plant_${i.toString().padStart(3, '0')}",
                        tipo = TipoPostagem.PLANTA,
                        usuario = usuario,
                        titulo = "${planta} do meu jardim!",
                        descricao = gerarDescricaoPlanta(planta, random),
                        imageUrl = "https://example.com/${planta.lowercase()}_${random.nextInt(5) + 1}.jpg",
                        localizacao = gerarLocalizacao(random),
                        dataPostagem = tempoAleatorio,
                        detalhesPlanta = DetalhesPlanta(
                            nomeComum = planta,
                            nomeCientifico = gerarNomeCientifico(planta),
                            familia = gerarFamiliaPlanta(random),
                            altura = "${random.nextInt(200) + 10}cm",
                            status = StatusPlanta.values()[random.nextInt(StatusPlanta.values().size)],
                            estagio = EstagioPlanta.values()[random.nextInt(EstagioPlanta.values().size)]
                        ),
                        interacoes = InteracoesPostagem(
                            curtidas = random.nextInt(100),
                            comentarios = random.nextInt(20),
                            compartilhamentos = random.nextInt(10),
                            visualizacoes = random.nextInt(500) + 50,
                            curtidoPeloUsuario = random.nextBoolean(),
                            salvosPeloUsuario = random.nextBoolean()
                        ),
                        isVerificado = random.nextBoolean(),
                        tags = gerarTagsPlanta(planta, random)
                    )
                )
            } else {
                val inseto = insetosExemplo[random.nextInt(insetosExemplo.size)]
                postagens.add(
                    PostagemFeed(
                        id = "post_insect_${i.toString().padStart(3, '0')}",
                        tipo = TipoPostagem.INSETO,
                        usuario = usuario,
                        titulo = "${inseto} encontrado!",
                        descricao = gerarDescricaoInseto(inseto, random),
                        imageUrl = "https://example.com/${inseto.lowercase()}_${random.nextInt(5) + 1}.jpg",
                        localizacao = gerarLocalizacao(random),
                        dataPostagem = tempoAleatorio,
                        detalhesInseto = DetalhesInseto(
                            nomeComum = inseto,
                            nomeCientifico = gerarNomeCientificoInseto(inseto),
                            familia = gerarFamiliaInseto(random),
                            tamanho = "${random.nextInt(50) + 2}mm",
                            tipo = TipoInseto.values()[random.nextInt(TipoInseto.values().size)],
                            habitat = gerarHabitat(random),
                            comportamento = gerarComportamento(random)
                        ),
                        interacoes = InteracoesPostagem(
                            curtidas = random.nextInt(80),
                            comentarios = random.nextInt(15),
                            compartilhamentos = random.nextInt(8),
                            visualizacoes = random.nextInt(300) + 30,
                            curtidoPeloUsuario = random.nextBoolean(),
                            salvosPeloUsuario = random.nextBoolean()
                        ),
                        isVerificado = random.nextBoolean(),
                        tags = gerarTagsInseto(inseto, random)
                    )
                )
            }
        }
        
        // Ordena por data (mais recentes primeiro)
        return postagens.sortedByDescending { it.dataPostagem }
    }
    
    private fun gerarDescricaoPlanta(planta: String, random: Random): String {
        val descricoes = listOf(
            "Esta ${planta.lowercase()} está crescendo muito bem em meu jardim!",
            "Minha ${planta.lowercase()} floresceu hoje, que alegria!",
            "Cuidando com muito carinho desta ${planta.lowercase()}.",
            "Primeira vez cultivando ${planta.lowercase()}, estou aprendendo muito!",
            "${planta} sempre foi minha favorita, olhem como está bonita!"
        )
        return descricoes[random.nextInt(descricoes.size)]
    }
    
    private fun gerarDescricaoInseto(inseto: String, random: Random): String {
        val descricoes = listOf(
            "Encontrei este ${inseto.lowercase()} no meu jardim hoje.",
            "Que espécie interessante! Este ${inseto.lowercase()} chamou minha atenção.",
            "${inseto} visitando as flores da horta.",
            "Registro deste ${inseto.lowercase()} para nosso catálogo.",
            "Observando o comportamento deste ${inseto.lowercase()}."
        )
        return descricoes[random.nextInt(descricoes.size)]
    }
    
    private fun gerarLocalizacao(random: Random): String {
        val locais = listOf(
            "Jardim Botânico - SP", "Horta Urbana - RJ", "Parque Ibirapuera - SP",
            "Jardim de Casa - MG", "Varanda do Apartamento", "Quintal - PR",
            "Fazenda Orgânica - RS", "Estufa - SC", "Praça Central - BA"
        )
        return locais[random.nextInt(locais.size)]
    }
    
    private fun gerarNomeCientifico(planta: String): String {
        val cientificos = mapOf(
            "Rosa" to "Rosa gallica",
            "Manjericão" to "Ocimum basilicum",
            "Tomate" to "Solanum lycopersicum",
            "Orquídea" to "Orchidaceae sp.",
            "Girassol" to "Helianthus annuus"
        )
        return cientificos[planta] ?: "${planta.lowercase()} sp."
    }
    
    private fun gerarNomeCientificoInseto(inseto: String): String {
        val cientificos = mapOf(
            "Joaninha" to "Coccinella septempunctata",
            "Borboleta" to "Lepidoptera sp.",
            "Abelha" to "Apis mellifera",
            "Libélula" to "Libellula sp."
        )
        return cientificos[inseto] ?: "${inseto.lowercase()} sp."
    }
    
    private fun gerarFamiliaPlanta(random: Random): String {
        val familias = listOf(
            "Rosaceae", "Lamiaceae", "Solanaceae", "Asteraceae", "Orchidaceae",
            "Fabaceae", "Brassicaceae", "Apiaceae", "Malvaceae", "Rubiaceae"
        )
        return familias[random.nextInt(familias.size)]
    }
    
    private fun gerarFamiliaInseto(random: Random): String {
        val familias = listOf(
            "Coccinellidae", "Lepidoptera", "Apidae", "Formicidae", "Libellulidae",
            "Scarabaeidae", "Acrididae", "Cicadidae", "Mantidae", "Aphididae"
        )
        return familias[random.nextInt(familias.size)]
    }
    
    private fun gerarHabitat(random: Random): String {
        val habitats = listOf(
            "Folhas de plantas", "Flores", "Solo húmido", "Troncos de árvores",
            "Água parada", "Vegetação densa", "Cascas de árvores"
        )
        return habitats[random.nextInt(habitats.size)]
    }
    
    private fun gerarComportamento(random: Random): String {
        val comportamentos = listOf(
            "Predador de pulgões", "Polinizador", "Decompositor", "Herbívoro",
            "Parasita", "Construtor de ninhos", "Coletor de néctar"
        )
        return comportamentos[random.nextInt(comportamentos.size)]
    }
    
    private fun gerarTagsPlanta(planta: String, random: Random): List<String> {
        val tagsComuns = listOf("jardim", "cultivo", "orgânico", "sustentável")
        val tagsEspecificas = when {
            planta.lowercase().contains("rosa") -> listOf("flores", "romance", "perfume")
            planta.lowercase().contains("manjericão") -> listOf("ervas", "culinária", "aromática")
            planta.lowercase().contains("tomate") -> listOf("horta", "fruto", "vermelho")
            else -> listOf("planta", "verde", "natureza")
        }
        return (tagsComuns + tagsEspecificas).shuffled(random).take(3)
    }
    
    private fun gerarTagsInseto(inseto: String, random: Random): List<String> {
        val tagsComuns = listOf("inseto", "natureza", "biodiversidade")
        val tagsEspecificas = when {
            inseto.lowercase().contains("joaninha") -> listOf("controle-biológico", "benéfico")
            inseto.lowercase().contains("borboleta") -> listOf("polinizador", "colorido")
            inseto.lowercase().contains("abelha") -> listOf("mel", "polinização")
            else -> listOf("pequeno", "observação")
        }
        return (tagsComuns + tagsEspecificas).shuffled(random).take(3)
    }
}