package com.ifpr.androidapptemplate.ui.registro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ifpr.androidapptemplate.databinding.FragmentRegistroBinding
import androidx.navigation.fragment.findNavController
import com.ifpr.androidapptemplate.R

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
        // Atualizações específicas do layout de registro podem ser adicionadas aqui
    }

    private fun setupClickListeners() {
        // Configurar cliques dos botões de registro
        binding.buttonRegistroPlanta.setOnClickListener {
            val intent = Intent(requireContext(), RegistroPlantaActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonRegistroInseto.setOnClickListener {
            val intent = Intent(requireContext(), RegistroInsetoActivity::class.java)
            startActivity(intent)
        }
        
        binding.buttonSeusRegistros.setOnClickListener {
            // Navegar para lista de registros do usuário (fragment interno)
            findNavController().navigate(R.id.navigation_registros_list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}