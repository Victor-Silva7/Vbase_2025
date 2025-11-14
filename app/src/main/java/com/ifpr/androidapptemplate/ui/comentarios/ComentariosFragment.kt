package com.ifpr.androidapptemplate.ui.comentarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ifpr.androidapptemplate.R
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
    
    // Lista de anexos para o novo comentário
    private val attachments = mutableListOf<String>()
    
    // ID do comentário pai quando respondendo a um comentário
    private var replyToCommentId: String? = null
    
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
            onAttachmentClick = { imageUrl ->
                // Abrir visualizador de imagem em tela cheia
                showAttachmentPreview(imageUrl)
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
                if (content.isNotEmpty() || attachments.isNotEmpty()) {
                    // Se estamos respondendo a um comentário, passar o parentId
                    viewModel.addComment(content, replyToCommentId, attachments.toList())
                    editTextComment.text?.clear()
                    attachments.clear()
                    replyToCommentId = null // Resetar o modo de resposta
                    updateAttachmentsUI()
                    updateReplyModeUI() // Atualizar UI para modo normal
                } else {
                    Toast.makeText(requireContext(), "Digite um comentário ou adicione um anexo", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Botão de adicionar anexo (para testes)
            buttonAddAttachment.setOnClickListener {
                // Em uma implementação completa, abriríamos um seletor de arquivos
                // Para este exemplo, vamos adicionar um anexo mock
                addAttachment("https://example.com/attachment_${System.currentTimeMillis()}.jpg")
            }
            
            // Botão para cancelar resposta
            buttonCancelReply.setOnClickListener {
                replyToCommentId = null
                editTextComment.setText("")
                updateReplyModeUI()
            }
            
            // Carregar avatar do usuário (mock)
            Glide.with(this@ComentariosFragment)
                .load("https://example.com/current_user.jpg")
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
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
        // Definir o ID do comentário pai
        replyToCommentId = comentario.id
        
        // Atualizar UI para modo de resposta
        updateReplyModeUI()
        
        // Preencher o campo de texto com menção ao usuário
        binding.editTextComment.setText("@${comentario.usuario.nomeExibicao} ")
        binding.editTextComment.setSelection(binding.editTextComment.text?.length ?: 0)
        binding.editTextComment.requestFocus()
        
        // Mostrar teclado
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(binding.editTextComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun updateReplyModeUI() {
        binding.apply {
            if (replyToCommentId != null) {
                // Modo de resposta
                layoutReplyMode.visibility = View.VISIBLE
                // Você pode atualizar o texto com o nome do usuário sendo respondido
                // textViewReplyingTo.text = "Respondendo a ${usuario.nomeExibicao}"
            } else {
                // Modo normal
                layoutReplyMode.visibility = View.GONE
            }
        }
    }
    
    private fun showCommentOptions(comentario: Comentario) {
        // Em uma implementação completa, mostraríamos um menu com opções como:
        // - Editar (se for do usuário atual)
        // - Deletar (se for do usuário atual)
        // - Denunciar (se for de outro usuário)
        Toast.makeText(requireContext(), "Opções para comentário: ${comentario.id}", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAttachmentPreview(imageUrl: String) {
        // Em uma implementação completa, mostraríamos a imagem em tela cheia
        // com opções de zoom, download, compartilhamento, etc.
        Toast.makeText(requireContext(), "Visualizar anexo: $imageUrl", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateAttachmentsUI() {
        binding.apply {
            if (attachments.isNotEmpty()) {
                layoutAttachments.visibility = View.VISIBLE
                containerAttachments.removeAllViews()
                
                attachments.forEach { imageUrl ->
                    // Criar thumbnail para cada anexo
                    val thumbnailView = layoutInflater.inflate(
                        R.layout.item_attachment_thumbnail, 
                        containerAttachments, 
                        false
                    )
                    
                    // Configurar clique para remover anexo
                    val removeButton = thumbnailView.findViewById<ImageButton>(R.id.buttonRemoveAttachment)
                    removeButton.setOnClickListener {
                        attachments.remove(imageUrl)
                        updateAttachmentsUI()
                    }
                    
                    containerAttachments.addView(thumbnailView)
                }
            } else {
                layoutAttachments.visibility = View.GONE
            }
        }
    }
    
    /**
     * Adiciona um anexo ao comentário atual
     * Em uma implementação completa, isso seria chamado após selecionar uma imagem
     */
    private fun addAttachment(imageUrl: String) {
        attachments.add(imageUrl)
        updateAttachmentsUI()
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