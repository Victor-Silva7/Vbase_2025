package com.ifpr.androidapptemplate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar RecyclerView para o feed
        setupRecyclerView()
        
        // Configurar observadores do ViewModel
        setupObservers(dashboardViewModel)

        return root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDashboard.apply {
            layoutManager = LinearLayoutManager(context)
            // TODO: Configurar adapter quando criarmos a estrutura de dados
        }
    }

    private fun setupObservers(viewModel: DashboardViewModel) {
        // Observar mudanças no ViewModel
        viewModel.title.observe(viewLifecycleOwner) { title ->
            binding.textDashboard.text = title
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDashboard.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // TODO: Observar lista de postagens quando implementarmos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}