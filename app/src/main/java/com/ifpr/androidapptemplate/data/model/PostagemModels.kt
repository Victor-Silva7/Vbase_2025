package com.ifpr.androidapptemplate.data.model

import java.util.*

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
 * Dados mock para demonstração
 */
object PostagemMockData {
    fun gerarPostagensMock(): List<PostagemFeed> {
        return listOf(
            PostagemFeed(
                id = "post_001",
                tipo = TipoPostagem.PLANTA,
                usuario = UsuarioPostagem(
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
                titulo = "Rosa do meu jardim floresceu!",
                descricao = "Depois de 3 meses cuidando com muito carinho, minha rosa finalmente deu sua primeira florada. As pétalas estão com uma cor incrível!",
                imageUrl = "https://example.com/rosa1.jpg",
                localizacao = "Jardim Botânico - SP",
                dataPostagem = System.currentTimeMillis() - 3600000, // 1 hora atrás
                detalhesPlanta = DetalhesPlanta(
                    nomeComum = "Rosa",
                    nomeCientifico = "Rosa gallica",
                    familia = "Rosaceae",
                    altura = "80cm",
                    status = StatusPlanta.FLORACAO,
                    estagio = EstagioPlanta.ADULTO
                ),
                interacoes = InteracoesPostagem(
                    curtidas = 23,
                    comentarios = 5,
                    compartilhamentos = 2,
                    visualizacoes = 156
                ),
                isVerificado = true,
                tags = listOf("rosa", "floração", "jardim")
            ),
            
            PostagemFeed(
                id = "post_002",
                tipo = TipoPostagem.INSETO,
                usuario = UsuarioPostagem(
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
                titulo = "Joaninha encontrada na horta",
                descricao = "Encontrei essa joaninha na minha horta hoje de manhã. Ela estava ajudando a controlar os pulgões das minhas plantas de tomate. Natureza trabalhando!",
                imageUrl = "https://example.com/joaninha1.jpg",
                localizacao = "Horta Urbana - RJ",
                dataPostagem = System.currentTimeMillis() - 7200000, // 2 horas atrás
                detalhesInseto = DetalhesInseto(
                    nomeComum = "Joaninha",
                    nomeCientifico = "Coccinella septempunctata",
                    familia = "Coccinellidae",
                    tamanho = "7mm",
                    tipo = TipoInseto.BENEFICO,
                    habitat = "Folhas de plantas",
                    comportamento = "Predador de pulgões"
                ),
                interacoes = InteracoesPostagem(
                    curtidas = 18,
                    comentarios = 3,
                    compartilhamentos = 1,
                    visualizacoes = 89
                ),
                isVerificado = false,
                tags = listOf("joaninha", "controle-biologico", "horta")
            ),
            
            PostagemFeed(
                id = "post_003",
                tipo = TipoPostagem.PLANTA,
                usuario = UsuarioPostagem(
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
                titulo = "Manjericão crescendo muito bem",
                descricao = "Meu manjericão está crescendo super bem na varanda. As folhas estão grandes e perfumadas. Vou fazer um pesto delicioso com elas!",
                imageUrl = "https://example.com/manjericao1.jpg",
                localizacao = "Varanda do apartamento",
                dataPostagem = System.currentTimeMillis() - 21600000, // 6 horas atrás
                detalhesPlanta = DetalhesPlanta(
                    nomeComum = "Manjericão",
                    nomeCientifico = "Ocimum basilicum",
                    familia = "Lamiaceae",
                    altura = "25cm",
                    status = StatusPlanta.SAUDAVEL,
                    estagio = EstagioPlanta.JOVEM
                ),
                interacoes = InteracoesPostagem(
                    curtidas = 31,
                    comentarios = 8,
                    compartilhamentos = 4,
                    visualizacoes = 203
                ),
                isVerificado = true,
                tags = listOf("manjericão", "ervas", "culinária")
            )
        )
    }
}