package com.ifpr.androidapptemplate.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ifpr.androidapptemplate.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val perfilViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar observadores do ViewModel
        setupObservers(perfilViewModel)
        
        // Configurar cliques dos botões
        setupClickListeners()

        return root
    }

    private fun setupObservers(viewModel: NotificationsViewModel) {
        // Observar dados do usuário
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.textUserName.text = name
        }
        
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.textUserEmail.text = email
        }
        
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            binding.textUserStats.text = stats
        }
    }

    private fun setupClickListeners() {
        // TODO: Configurar cliques para editar perfil, configurações, etc.
        binding.buttonEditProfile.setOnClickListener {
            // TODO: Navegar para edição de perfil
        }
        
        binding.buttonSettings.setOnClickListener {
            // TODO: Abrir configurações
        }
        
        binding.buttonLogout.setOnClickListener {
            // TODO: Implementar logout
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}