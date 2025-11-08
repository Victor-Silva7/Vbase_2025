package com.ifpr.androidapptemplate.ui.postagens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentPostagensBinding

class PostagensFragment : Fragment() {

    private var _binding: FragmentPostagensBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val postagensViewModel = ViewModelProvider(this)[PostagensViewModel::class.java]
        
        _binding = FragmentPostagensBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar RecyclerView para o feed
        setupRecyclerView()
        
        // Configurar observadores do ViewModel
        setupObservers(postagensViewModel)

        return root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPostagens.apply {
            layoutManager = LinearLayoutManager(context)
            // TODO: Configurar adapter quando criarmos a estrutura de dados
        }
    }

    private fun setupObservers(viewModel: PostagensViewModel) {
        // Observar mudanças no ViewModel
        viewModel.title.observe(viewLifecycleOwner) { title ->
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarPostagens.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // TODO: Observar lista de postagens quando implementarmos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}