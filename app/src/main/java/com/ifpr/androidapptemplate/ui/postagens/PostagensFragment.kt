package com.ifpr.androidapptemplate.ui.postagens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentPostagensBinding
import com.ifpr.androidapptemplate.data.model.PostagemFeed

class PostagensFragment : Fragment() {

    private var _binding: FragmentPostagensBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PostagensViewModel
    private lateinit var adapter: PostagensAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostagensBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this)[PostagensViewModel::class.java]

        // Configurar RecyclerView
        setupRecyclerView()
        
        // Configurar observadores do ViewModel
        setupObservers()

        return root
    }

    private fun setupRecyclerView() {
        adapter = PostagensAdapter(
            onLikeClick = { postagem ->
                handleLikeClick(postagem)
            },
            onCommentClick = { postagem ->
                handleCommentClick(postagem)
            },
            onShareClick = { postagem ->
                handleShareClick(postagem)
            },
            onItemClick = { postagem ->
                handleItemClick(postagem)
            }
        )

        binding.recyclerViewPostagens.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PostagensFragment.adapter
            setHasFixedSize(false)
        }
    }

    private fun setupObservers() {
        // Observar mudanças no título
        viewModel.title.observe(viewLifecycleOwner) { title ->
            // Título pode ser usado se necessário
        }

        // Observar carregamento
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBarPostagens.visibility = View.VISIBLE
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.textViewEmpty.text = "🔍 Procurando por postagens..."
                binding.recyclerViewPostagens.visibility = View.GONE
            } else {
                binding.progressBarPostagens.visibility = View.GONE
            }
        }

        // Observar lista de postagens
        viewModel.postagens.observe(viewLifecycleOwner) { postagens ->
            if (postagens != null) {
                adapter.submitList(postagens)
                
                // Mostrar mensagem se vazio
                if (postagens.isEmpty()) {
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.textViewEmpty.text = "📭 Nenhuma postagem ainda!\nSeja o primeiro a registrar."
                    binding.recyclerViewPostagens.visibility = View.GONE
                } else {
                    binding.textViewEmpty.visibility = View.GONE
                    binding.recyclerViewPostagens.visibility = View.VISIBLE
                }
            }
        }

        // Observar mensagens de erro
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.textViewEmpty.text = "❌ Erro: $message"
                binding.recyclerViewPostagens.visibility = View.GONE
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLikeClick(postagem: PostagemFeed) {
        viewModel.likePostagem(postagem.id)
        Toast.makeText(
            context,
            if (postagem.interacoes.curtidoPeloUsuario) "Descurtido" else "Curtido",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleCommentClick(postagem: PostagemFeed) {
        viewModel.commentOnPostagem(postagem.id, "")
        // TODO: Abrir dialog/tela de comentários
        Toast.makeText(context, "Comentários em desenvolvimento", Toast.LENGTH_SHORT).show()
    }

    private fun handleShareClick(postagem: PostagemFeed) {
        viewModel.sharePostagem(postagem.id)
        Toast.makeText(context, "Compartilhado", Toast.LENGTH_SHORT).show()
    }

    private fun handleItemClick(postagem: PostagemFeed) {
        // TODO: Navegar para detalhes da postagem ou perfil do usuário
        Toast.makeText(context, "Postagem: ${postagem.titulo}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}