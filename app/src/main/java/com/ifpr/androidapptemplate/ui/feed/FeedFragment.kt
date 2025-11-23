package com.ifpr.androidapptemplate.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentFeedBinding
import com.ifpr.androidapptemplate.data.model.*
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
                // Abrir tela de comentários
                openCommentsScreen(postagem)
            },
            onShareClick = { postagem ->
                // Compartilhar postagem (funcionalidade removida)
                Toast.makeText(requireContext(), "Compartilhar em breve", Toast.LENGTH_SHORT).show()
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
        // Busca removida - elementos não existem no layout
        // Se precisar de busca/filtros, adicione os elementos no fragment_feed.xml primeiro
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
        
        // fabAi removido do layout
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
        
        // Mensagem padrão (busca removida)
        binding.tvEmptyTitle.text = "Nenhuma postagem ainda"
        binding.tvEmptyMessage.text = "Seja o primeiro a compartilhar suas descobertas!"
    }
    
    private fun showErrorState() {
        binding.recyclerViewFeed.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        // Error state - pode mostrar mensagem se necessário
    }
    
    private fun openCommentsScreen(postagem: PostagemFeed) {
        // Abrir ComentariosActivity
        val intent = android.content.Intent(requireContext(), com.ifpr.androidapptemplate.ui.comentarios.ComentariosActivity::class.java)
        intent.putExtra(com.ifpr.androidapptemplate.ui.comentarios.ComentariosActivity.EXTRA_POSTAGEM_ID, postagem.id)
        intent.putExtra(com.ifpr.androidapptemplate.ui.comentarios.ComentariosActivity.EXTRA_POSTAGEM_TITULO, postagem.titulo)
        startActivity(intent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}