package com.victor.vgroup.ui.comentarios

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.victor.vgroup.databinding.ActivityComentariosBinding

/**
 * Activity SIMPLIFICADA para comentários
 * Exibe comentários de uma postagem e permite adicionar novos
 */
class ComentariosActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityComentariosBinding
    private val viewModel: ComentariosViewModel by viewModels()
    private lateinit var adapter: SimpleComentariosAdapter
    
    companion object {
        const val EXTRA_POSTAGEM_ID = "postagem_id"
        const val EXTRA_POSTAGEM_TITULO = "postagem_titulo"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val postagemId = intent.getStringExtra(EXTRA_POSTAGEM_ID) ?: run {
            Toast.makeText(this, "Erro: ID da postagem não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val postagemTitulo = intent.getStringExtra(EXTRA_POSTAGEM_TITULO) ?: "Comentários"
        
        setupToolbar(postagemTitulo)
        setupRecyclerView()
        setupInputField()
        observeViewModel()
        
        viewModel.carregarComentarios(postagemId)
    }
    
    private fun setupToolbar(titulo: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = titulo
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = SimpleComentariosAdapter()
        binding.recyclerViewComentarios.apply {
            layoutManager = LinearLayoutManager(this@ComentariosActivity)
            adapter = this@ComentariosActivity.adapter
        }
    }
    
    private fun setupInputField() {
        binding.buttonEnviar.setOnClickListener {
            val texto = binding.editTextComentario.text.toString().trim()
            
            if (texto.isEmpty()) {
                Toast.makeText(this, "Digite um comentário", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val postagemId = intent.getStringExtra(EXTRA_POSTAGEM_ID) ?: return@setOnClickListener
            viewModel.adicionarComentario(postagemId, texto)
            binding.editTextComentario.text?.clear()
        }
    }
    
    private fun observeViewModel() {
        viewModel.comentarios.observe(this) { comentarios ->
            adapter.submitList(comentarios)
            updateEmptyState(comentarios.isEmpty())
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerViewComentarios.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerViewComentarios.visibility = View.VISIBLE
        }
    }
}
