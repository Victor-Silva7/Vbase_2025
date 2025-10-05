package com.ifpr.androidapptemplate.ui.comentarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentComentariosBinding
import com.ifpr.androidapptemplate.data.model.Comentario

/**
 * Fragment para exibir e gerenciar comentários de uma postagem
 * Suporta scroll infinito, interações sociais e resposta a comentários
 */
class ComentariosFragment : Fragment() {
    
    private var _binding: FragmentComentariosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ComentariosViewModel by viewModels()
    
    private lateinit var comentariosAdapter: ComentariosAdapter
    
    private var postId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtém o ID da postagem dos argumentos
        postId = arguments?.getString("postId") ?: ""
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComentariosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupCommentInput()
        setupToolbar()
        setupSwipeRefresh()
        observeViewModel()
        
        // Inicializa o ViewModel com a postagem
        if (postId.isNotEmpty()) {
            viewModel.initForPost(postId)
        }
    }
    
    private fun setupRecyclerView() {
        comentariosAdapter = ComentariosAdapter(
            onLikeClick = { comentario ->
                viewModel.toggleLike(comentario.id)
            },
            onReplyClick = { comentario ->
                // Abrir input de resposta
                openReplyInput(comentario)
            },
            onMoreOptionsClick = { comentario ->
                // Mostrar opções (editar, deletar, etc)
                showCommentOptions(comentario)
            },
            onLoadMore = {
                viewModel.loadMoreComments()
            }
        )
        
        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = comentariosAdapter
        }
    }
    
    private fun setupCommentInput() {
        binding.apply {
            // Botão de enviar comentário
            buttonSend.setOnClickListener {
                val content = editTextComment.text.toString().trim()
                if (content.isNotEmpty()) {
                    viewModel.addComment(content)
                    editTextComment.text?.clear()
                }
            }
            
            // Carregar avatar do usuário (mock)
            Glide.with(this@ComentariosFragment)
                .load("https://example.com/current_user.jpg")
                .placeholder(R.drawable.ic_person_24dp)
                .error(R.drawable.ic_person_24dp)
                .transform(CircleCrop())
                .into(imageViewUserAvatar)
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Navegar de volta
            requireActivity().onBackPressed()
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.initForPost(postId)
        }
        
        // Cores do refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
            com.ifpr.androidapptemplate.R.color.primary_green,
            com.ifpr.androidapptemplate.R.color.primary_green_dark
        )
    }
    
    private fun observeViewModel() {
        // Estado de carregamento
        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            handleLoadingState(state)
        }
        
        // Comentários atuais
        viewModel.currentComments.observe(viewLifecycleOwner) { comentarios ->
            comentariosAdapter.submitList(comentarios)
            updateUIState(comentarios)
        }
        
        // Mensagens de erro
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun handleLoadingState(state: com.ifpr.androidapptemplate.data.model.LoadingState) {
        when (state) {
            is com.ifpr.androidapptemplate.data.model.LoadingState.Idle -> {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            is com.ifpr.androidapptemplate.data.model.LoadingState.Loading -> {
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    binding.layoutLoading.visibility = View.VISIBLE
                }
            }
            is com.ifpr.androidapptemplate.data.model.LoadingState.LoadingMore -> {
                // Loading more é tratado pelo adapter
            }
            is com.ifpr.androidapptemplate.data.model.LoadingState.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.layoutLoading.visibility = View.GONE
            }
            is com.ifpr.androidapptemplate.data.model.LoadingState.Error -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.layoutLoading.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.textErrorMessage.text = state.message
            }
        }
    }
    
    private fun updateUIState(comentarios: List<Comentario>) {
        binding.layoutLoading.visibility = View.GONE
        
        if (comentarios.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerViewComments.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerViewComments.visibility = View.VISIBLE
        }
    }
    
    private fun openReplyInput(comentario: Comentario) {
        // Em uma implementação completa, abriríamos um input especializado para respostas
        binding.editTextComment.setText("@${comentario.usuario.nomeExibicao} ")
        binding.editTextComment.setSelection(binding.editTextComment.text?.length ?: 0)
        binding.editTextComment.requestFocus()
        
        // Mostrar teclado
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(binding.editTextComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun showCommentOptions(comentario: Comentario) {
        // Em uma implementação completa, mostraríamos um menu com opções como:
        // - Editar (se for do usuário atual)
        // - Deletar (se for do usuário atual)
        // - Denunciar (se for de outro usuário)
        Toast.makeText(requireContext(), "Opções para comentário: ${comentario.id}", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        /**
         * Cria uma nova instância do fragment com argumentos
         */
        fun newInstance(postId: String): ComentariosFragment {
            val fragment = ComentariosFragment()
            val args = Bundle().apply {
                putString("postId", postId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}