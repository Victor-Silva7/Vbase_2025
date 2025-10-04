package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar observadores do ViewModel
        setupObservers(homeViewModel)
        
        // Configurar cliques dos botões
        setupClickListeners()

        return root
    }

    private fun setupObservers(viewModel: HomeViewModel) {
        // Observar mudanças no ViewModel
        viewModel.title.observe(viewLifecycleOwner) { title ->
            binding.textHome.text = title
        }
        
        // Observar estatísticas
        viewModel.plantCount.observe(viewLifecycleOwner) { count ->
            binding.textStatsPlantas.text = count
        }
        
        viewModel.insectCount.observe(viewLifecycleOwner) { count ->
            binding.textStatsInsetos.text = count
        }
        
        // Carregar estatísticas ao iniciar
        viewModel.loadUserStats()
    }

    private fun setupClickListeners() {
        // Configurar cliques dos cards de registro
        binding.cardRegistroPlanta.setOnClickListener {
            // TODO: Navegar para registro de planta
        }
        
        binding.cardRegistroInseto.setOnClickListener {
            // TODO: Navegar para registro de inseto
        }
        
        binding.cardSeusRegistros.setOnClickListener {
            // TODO: Navegar para lista de registros do usuário
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}