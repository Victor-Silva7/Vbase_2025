package com.ifpr.androidapptemplate.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentMeusRegistrosBinding
import com.ifpr.androidapptemplate.data.repository.RegistrationStats

/**
 * Fragment para exibir a lista de registros pessoais do usuário
 * Usa tabs para separar plantas e insetos
 */
class MeusRegistrosFragment : Fragment() {

    private var _binding: FragmentMeusRegistrosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MeusRegistrosViewModel by viewModels()
    
    private lateinit var viewPagerAdapter: RegistrosTabsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeusRegistrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupSearch()
        setupFab()
        observeViewModel()
        
        // Load initial data
        viewModel.loadRegistrations()
    }

    /**
     * Configura o ViewPager com tabs para plantas e insetos
     */
    private fun setupViewPager() {
        viewPagerAdapter = RegistrosTabsAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter
        
        // Conecta o TabLayout com o ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_plants)
                1 -> getString(R.string.tab_insects)
                else -> ""
            }
        }.attach()
    }

    /**
     * Configura a funcionalidade de busca
     */
    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            performSearch()
            true
        }
        
        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            binding.ivClearSearch.visibility = View.GONE
            viewModel.clearSearch()
        }
        
        // Show/hide clear button based on text input
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.ivClearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    /**
     * Configura o FAB para adicionar novos registros
     */
    private fun setupFab() {
        binding.fabAddRegistration.setOnClickListener {
            // TODO: Show dialog to choose between plant or insect registration
            showRegistrationTypeDialog()
        }
    }

    /**
     * Observa mudanças no ViewModel
     */
    private fun observeViewModel() {
        // Observe registration statistics
        viewModel.registrationStats.observe(viewLifecycleOwner) { stats ->
            updateStatistics(stats)
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state if needed
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                // Show error message
                showError(errorMessage)
            }
        }
    }

    /**
     * Atualiza as estatísticas exibidas no header
     */
    private fun updateStatistics(stats: RegistrationStats) {
        binding.tvTotalPlantas.text = stats.totalPlantas.toString()
        binding.tvTotalInsetos.text = stats.totalInsetos.toString()
        binding.tvTotalRegistros.text = stats.getTotalRegistros().toString()
    }

    /**
     * Executa a busca com o texto atual
     */
    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        viewModel.searchRegistrations(query)
    }

    /**
     * Mostra diálogo para escolher tipo de registro
     */
    private fun showRegistrationTypeDialog() {
        val items = arrayOf(
            getString(R.string.filter_plants),
            getString(R.string.filter_insects)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Escolher tipo de registro")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        // Navigate to plant registration
                        navigateToPlantRegistration()
                    }
                    1 -> {
                        // Navigate to insect registration
                        navigateToInsectRegistration()
                    }
                }
            }
            .show()
    }

    /**
     * Navega para o registro de plantas
     */
    private fun navigateToPlantRegistration() {
        // TODO: Implement navigation to plant registration activity
        // val intent = android.content.Intent(requireContext(), RegistroPlantaActivity::class.java)
        // startActivity(intent)
    }

    /**
     * Navega para o registro de insetos
     */
    private fun navigateToInsectRegistration() {
        // TODO: Implement navigation to insect registration activity
        // val intent = android.content.Intent(requireContext(), RegistroInsetoActivity::class.java)
        // startActivity(intent)
    }

    /**
     * Exibe mensagem de erro
     */
    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Adapter para os tabs do ViewPager
     */
    private class RegistrosTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RegistrosListFragment.newInstance(RegistrosListFragment.TYPE_PLANTS)
                1 -> RegistrosListFragment.newInstance(RegistrosListFragment.TYPE_INSECTS)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}