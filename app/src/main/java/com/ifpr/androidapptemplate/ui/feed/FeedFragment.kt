package com.ifpr.androidapptemplate.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentFeedBinding
import com.ifpr.androidapptemplate.data.model.*

/**
 * Fragment do feed de postagens com cards de usuário
 * Exibe postagens de plantas e insetos da comunidade
 */
class FeedFragment : Fragment() {
    
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var postagemAdapter: PostagemCardAdapter
    private var todasPostagens = listOf<PostagemFeed>()
    private var filtroAtual = TipoFiltro.TODAS
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchAndFilters()
        setupSwipeRefresh()
        loadPostagens()
    }
    
    private fun setupRecyclerView() {
        postagemAdapter = PostagemCardAdapter(
            onCardClick = { postagem ->
                // Navegar para detalhes da postagem
                Toast.makeText(requireContext(), "Detalhes: ${postagem.titulo}", Toast.LENGTH_SHORT).show()
            },
            onUserClick = { usuario ->
                // Navegar para perfil do usuário
                Toast.makeText(requireContext(), "Perfil: ${usuario.nomeExibicao}", Toast.LENGTH_SHORT).show()
            },
            onLikeClick = { postagem ->
                // Implementar curtir/descurtir
                handleLikeClick(postagem)
            },
            onCommentClick = { postagem ->
                // Abrir comentários
                Toast.makeText(requireContext(), "Comentários: ${postagem.titulo}", Toast.LENGTH_SHORT).show()
            },
            onShareClick = { postagem ->
                // Compartilhar postagem
                handleShareClick(postagem)
            },
            onBookmarkClick = { postagem ->
                // Salvar/remover dos salvos
                handleBookmarkClick(postagem)
            }
        )
        
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postagemAdapter
            // Adiciona espaçamento entre itens
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(requireContext(), 
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
    }
    
    private fun setupSearchAndFilters() {
        // Busca em tempo real
        binding.etSearchPosts.addTextChangedListener { text ->
            val query = text.toString().trim()
            filtrarPostagens(query)
            
            // Mostrar/esconder botão limpar
            binding.ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        // Botão limpar busca
        binding.ivClearSearch.setOnClickListener {
            binding.etSearchPosts.text?.clear()
        }
        
        // Filtros por categoria
        binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
            filtroAtual = when (checkedId) {
                binding.chipPlantPosts.id -> TipoFiltro.PLANTAS
                binding.chipInsectPosts.id -> TipoFiltro.INSETOS
                else -> TipoFiltro.TODAS
            }
            filtrarPostagens(binding.etSearchPosts.text.toString().trim())
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshPostagens()
        }
        
        // Cores do refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
            com.ifpr.androidapptemplate.R.color.primary_green,
            com.ifpr.androidapptemplate.R.color.primary_green_dark
        )
        
        // Botões de refresh nos estados vazios
        binding.btnRefreshFeed.setOnClickListener { refreshPostagens() }
        binding.btnRetry.setOnClickListener { refreshPostagens() }
    }
    
    private fun loadPostagens() {
        showLoading(true)
        
        // Simula carregamento de dados (em produção seria do Firebase)
        binding.root.postDelayed({
            todasPostagens = PostagemMockData.gerarPostagensMock()
            filtrarPostagens("")
            showLoading(false)
        }, 1500)
    }
    
    private fun refreshPostagens() {
        binding.swipeRefreshLayout.isRefreshing = true
        
        // Simula refresh de dados
        binding.root.postDelayed({
            // Em produção, recarregaria do Firebase
            todasPostagens = PostagemMockData.gerarPostagensMock()
            filtrarPostagens(binding.etSearchPosts.text.toString().trim())
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireContext(), "Feed atualizado!", Toast.LENGTH_SHORT).show()
        }, 1000)
    }
    
    private fun filtrarPostagens(query: String) {
        var postagensFiltradas = todasPostagens
        
        // Filtro por categoria
        when (filtroAtual) {
            TipoFiltro.PLANTAS -> postagensFiltradas = postagensFiltradas.filter { it.tipo == TipoPostagem.PLANTA }
            TipoFiltro.INSETOS -> postagensFiltradas = postagensFiltradas.filter { it.tipo == TipoPostagem.INSETO }
            TipoFiltro.TODAS -> { /* Mostrar todas */ }
        }
        
        // Filtro por busca de texto
        if (query.isNotEmpty()) {
            postagensFiltradas = postagensFiltradas.filter { postagem ->
                postagem.titulo.contains(query, ignoreCase = true) ||
                postagem.descricao.contains(query, ignoreCase = true) ||
                postagem.usuario.nomeExibicao.contains(query, ignoreCase = true) ||
                postagem.tags.any { it.contains(query, ignoreCase = true) } ||
                (postagem.detalhesPlanta?.nomeComum?.contains(query, ignoreCase = true) == true) ||
                (postagem.detalhesPlanta?.nomeCientifico?.contains(query, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeComum?.contains(query, ignoreCase = true) == true) ||
                (postagem.detalhesInseto?.nomeCientifico?.contains(query, ignoreCase = true) == true)
            }
        }
        
        // Atualiza a lista
        postagemAdapter.submitList(postagensFiltradas)
        
        // Gerencia estados vazios
        if (postagensFiltradas.isEmpty()) {
            if (query.isNotEmpty()) {
                showEmptySearch()
            } else {
                showEmptyFeed()
            }
        } else {
            showFeedContent()
        }
    }
    
    private fun handleLikeClick(postagem: PostagemFeed) {
        // Atualiza o estado local (em produção seria no Firebase)
        val novasInteracoes = postagem.interacoes.copy(
            curtidoPeloUsuario = !postagem.interacoes.curtidoPeloUsuario,
            curtidas = if (postagem.interacoes.curtidoPeloUsuario) {
                postagem.interacoes.curtidas - 1
            } else {
                postagem.interacoes.curtidas + 1
            }
        )
        
        val postagemAtualizada = postagem.copy(interacoes = novasInteracoes)
        
        // Atualiza a lista local
        todasPostagens = todasPostagens.map { 
            if (it.id == postagem.id) postagemAtualizada else it 
        }
        
        // Reaplica filtros
        filtrarPostagens(binding.etSearchPosts.text.toString().trim())
        
        // Feedback visual
        val mensagem = if (novasInteracoes.curtidoPeloUsuario) "Curtido!" else "Descurtido"
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
    }
    
    private fun handleShareClick(postagem: PostagemFeed) {
        // Implementa compartilhamento
        val compartilharIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, 
                "Confira esta postagem: ${postagem.titulo}\n\nPor: ${postagem.usuario.nomeExibicao}\n\n${postagem.descricao}")
        }
        
        startActivity(android.content.Intent.createChooser(compartilharIntent, "Compartilhar postagem"))
    }
    
    private fun handleBookmarkClick(postagem: PostagemFeed) {
        // Atualiza estado de salvamento
        val novasInteracoes = postagem.interacoes.copy(
            salvosPeloUsuario = !postagem.interacoes.salvosPeloUsuario
        )
        
        val postagemAtualizada = postagem.copy(interacoes = novasInteracoes)
        
        // Atualiza a lista local
        todasPostagens = todasPostagens.map { 
            if (it.id == postagem.id) postagemAtualizada else it 
        }
        
        // Reaplica filtros
        filtrarPostagens(binding.etSearchPosts.text.toString().trim())
        
        // Feedback visual
        val mensagem = if (novasInteracoes.salvosPeloUsuario) "Salvo nos favoritos!" else "Removido dos favoritos"
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewFeed.visibility = if (show) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }
    
    private fun showFeedContent() {
        binding.recyclerViewFeed.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }
    
    private fun showEmptyFeed() {
        binding.recyclerViewFeed.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        
        // Personaliza mensagem para estado vazio
        binding.tvEmptyTitle.text = "Nenhuma postagem ainda"
        binding.tvEmptyMessage.text = "Seja o primeiro a compartilhar suas descobertas!"
    }
    
    private fun showEmptySearch() {
        binding.recyclerViewFeed.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        
        // Personaliza mensagem para busca vazia
        binding.tvEmptyTitle.text = "Nenhum resultado encontrado"
        binding.tvEmptyMessage.text = "Tente ajustar os termos de busca ou filtros"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private enum class TipoFiltro {
        TODAS, PLANTAS, INSETOS
    }
}