package com.ifpr.androidapptemplate.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ifpr.androidapptemplate.databinding.FragmentRegistroBinding

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val registroViewModel = ViewModelProvider(this)[RegistroViewModel::class.java]
        
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar observadores do ViewModel
        setupObservers(registroViewModel)
        
        // Configurar cliques dos botões
        setupClickListeners()

        return root
    }

    private fun setupObservers(viewModel: RegistroViewModel) {
        // Observar mudanças no ViewModel
        viewModel.title.observe(viewLifecycleOwner) { title ->
            binding.textRegistroTitle.text = title
        }
    }

    private fun setupClickListeners() {
        // Configurar cliques dos botões de registro
        binding.buttonRegistroPlanta.setOnClickListener {
            // TODO: Navegar para registro de planta
        }
        
        binding.buttonRegistroInseto.setOnClickListener {
            // TODO: Navegar para registro de inseto
        }
        
        binding.buttonSeusRegistros.setOnClickListener {
            // TODO: Navegar para lista de registros do usuário
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}