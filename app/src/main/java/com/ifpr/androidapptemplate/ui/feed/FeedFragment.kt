package com.ifpr.androidapptemplate.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentFeedBinding
import com.ifpr.androidapptemplate.data.model.*
import com.ifpr.androidapptemplate.data.repository.TipoFiltro
import kotlinx.coroutines.*

/**
 * Fragment do feed de postagens com suporte a scroll infinito
 * Exibe postagens de plantas e insetos da comunidade com paginação
 */
class FeedFragment : Fragment() {
    
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var postagemAdapter: PostagemCardAdapter
    
    // Debounce para busca
    private var searchJob: Job? = null
    private val searchDebounceDelay = 500L
    
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
        observeViewModel()
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
                // Usar ViewModel para gerenciar curtidas
                viewModel.toggleLike(postagem)
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
                // Usar ViewModel para gerenciar salvamentos
                viewModel.toggleBookmark(postagem)
            },
            onLoadMore = {
                // Trigger para carregar mais páginas
                viewModel.loadNextPage()
            }
        )
        
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postagemAdapter
            // Remove DividerItemDecoration para melhor performance com muitos itens
        }
    }
    
    private fun setupSearchAndFilters() {
        // Busca com debounce
        binding.etSearchPosts.addTextChangedListener { text ->
            val query = text.toString().trim()
            
            // Cancela busca anterior
            searchJob?.cancel()
            
            // Nova busca com debounce
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(searchDebounceDelay)
                viewModel.applySearch(query)
            }
            
            // Mostrar/esconder botão limpar
            binding.ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        // Botão limpar busca
        binding.ivClearSearch.setOnClickListener {
            binding.etSearchPosts.text?.clear()
        }
        
        // Filtros por categoria
        binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
            val filtro = when (checkedId) {
                binding.chipPlantPosts.id -> TipoFiltro.PLANTAS
                binding.chipInsectPosts.id -> TipoFiltro.INSETOS
                else -> TipoFiltro.TODAS
            }
            viewModel.applyFilter(filtro)
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshFeed()
        }
        
        // Cores do refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
            com.ifpr.androidapptemplate.R.color.primary_green,
            com.ifpr.androidapptemplate.R.color.primary_green_dark
        )
        
        // Botões de refresh nos estados vazios
        binding.btnRefreshFeed.setOnClickListener { viewModel.refreshFeed() }
        binding.btnRetry.setOnClickListener { viewModel.refreshFeed() }
    }
    
    private fun observeViewModel() {
        // Estado de carregamento
        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            handleLoadingState(state)
        }
        
        // Postagens atuais
        viewModel.currentPosts.observe(viewLifecycleOwner) { postagens ->
            postagemAdapter.updatePostagens(postagens)
            updateUIState(postagens)
        }
        
        // Estado de refresh
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }
        
        // Mensagens de erro
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun handleLoadingState(state: LoadingState) {
        when (state) {
            is LoadingState.Idle -> {
                binding.progressBar.visibility = View.GONE
                postagemAdapter.hideLoading()
            }
            is LoadingState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                postagemAdapter.hideLoading()
            }
            is LoadingState.LoadingMore -> {
                binding.progressBar.visibility = View.GONE
                postagemAdapter.showLoading()
            }
            is LoadingState.Success -> {
                binding.progressBar.visibility = View.GONE
                postagemAdapter.hideLoading()
            }
            is LoadingState.Error -> {
                binding.progressBar.visibility = View.GONE
                postagemAdapter.hideLoading()
                showErrorState()
            }
        }
    }
    
    private fun updateUIState(postagens: List<PostagemFeed>) {
        if (postagens.isEmpty()) {
            showEmptyState()
        } else {
            showFeedContent()
        }
    }
    
    private fun showFeedContent() {
        binding.recyclerViewFeed.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }
    
    private fun showEmptyState() {
        binding.recyclerViewFeed.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
        
        // Personaliza mensagem baseada na busca
        val query = binding.etSearchPosts.text.toString().trim()
        if (query.isNotEmpty()) {
            binding.tvEmptyTitle.text = "Nenhum resultado encontrado"
            binding.tvEmptyMessage.text = "Tente ajustar os termos de busca ou filtros"
        } else {
            binding.tvEmptyTitle.text = "Nenhuma postagem ainda"
            binding.tvEmptyMessage.text = "Seja o primeiro a compartilhar suas descobertas!"
        }
    }
    
    private fun showErrorState() {
        binding.recyclerViewFeed.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.VISIBLE
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
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