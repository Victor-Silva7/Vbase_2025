package com.ifpr.androidapptemplate.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentNotificacoesBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentNotificacoesBinding? = null
    private val binding get() = _binding!!
    private lateinit var perfilViewModel: PerfilViewModel
    private lateinit var notificacoesAdapter: NotificacoesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        perfilViewModel = ViewModelProvider(this)[PerfilViewModel::class.java]
        
        _binding = FragmentNotificacoesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar RecyclerView
        setupRecyclerView()
        
        // Configurar observadores do ViewModel
        setupObservers(perfilViewModel)

        return root
    }
    
    private fun setupRecyclerView() {
        notificacoesAdapter = NotificacoesAdapter { notificacao ->
            // TODO: Abrir postagem ao clicar na notificação
            android.widget.Toast.makeText(
                requireContext(),
                "Postagem: ${notificacao.postagemTitulo}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.recyclerViewNotificacoes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificacoesAdapter
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Recarregar dados sempre que a tela for exibida
        perfilViewModel.loadUserData()
        perfilViewModel.loadNotificacoes()
    }

    private fun setupObservers(viewModel: PerfilViewModel) {
        // Observar apenas estatísticas (quantidade de registros)
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            binding.textPerfil.text = stats
        }
        
        // Observar notificações
        viewModel.notificacoes.observe(viewLifecycleOwner) { notificacoes ->
            if (notificacoes.isEmpty()) {
                binding.recyclerViewNotificacoes.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerViewNotificacoes.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                notificacoesAdapter.submitList(notificacoes)
            }
        }
        
        // Observar loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}