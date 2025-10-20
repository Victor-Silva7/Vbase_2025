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
        // Carregar dados reais do usuário autenticado
        perfilViewModel.loadUserData()

        return root
    }

    private fun setupObservers(viewModel: NotificationsViewModel) {
        // Exibir um resumo simples usando o único TextView do layout
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.textNotifications.text = name
        }
        
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            // Anexar email como informação adicional
            val current = binding.textNotifications.text?.toString()?.takeIf { it.isNotEmpty() } ?: "Usuário"
            binding.textNotifications.text = "$current\n$email"
        }
        
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            val current = binding.textNotifications.text?.toString()?.takeIf { it.isNotEmpty() } ?: ""
            binding.textNotifications.text = listOf(current, stats).filter { it.isNotEmpty() }.joinToString("\n")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}