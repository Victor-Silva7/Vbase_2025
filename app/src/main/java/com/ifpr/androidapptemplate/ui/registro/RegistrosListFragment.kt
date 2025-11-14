package com.ifpr.androidapptemplate.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentRegistrosListBinding
import com.ifpr.androidapptemplate.data.model.Planta
import com.ifpr.androidapptemplate.data.model.Inseto
import com.ifpr.androidapptemplate.data.repository.RegistrationStats
import android.text.TextWatcher
import android.text.Editable
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import android.util.Log

/**
 * Fragment para exibir lista completa de registros (plantas e insetos)
 * Consolidado com filtros, busca e FAB do MeusRegistrosFragment
 */
class RegistrosListFragment : Fragment() {

    companion object {
        const val TYPE_PLANTS = "plants"
        const val TYPE_INSECTS = "insects"
        private const val ARG_TYPE = "type"

        fun newInstance(type: String): RegistrosListFragment {
            return RegistrosListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }

    private var _binding: FragmentRegistrosListBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MeusRegistrosViewModel by activityViewModels()
    
    private lateinit var registrosAdapter: RegistrosAdapter
    private var listType: String = TYPE_PLANTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString(ARG_TYPE, TYPE_PLANTS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrosListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            Log.d("RegistrosListFragment", "onViewCreated iniciado")
            setupRecyclerView()
            Log.d("RegistrosListFragment", "setupRecyclerView OK")
            setupSwipeRefresh()
            Log.d("RegistrosListFragment", "setupSwipeRefresh OK")
            setupFilters()
            Log.d("RegistrosListFragment", "setupFilters OK")
            observeViewModel()
            Log.d("RegistrosListFragment", "observeViewModel OK")
            
            // Load initial data
            try {
                sharedViewModel.loadRegistrations()
                Log.d("RegistrosListFragment", "loadRegistrations OK")
            } catch (e: Exception) {
                Log.e("RegistrosListFragment", "Erro ao carregar registros: ${e.message}", e)
                e.printStackTrace()
                showError("Erro ao carregar registros: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("RegistrosListFragment", "Erro em onViewCreated", e)
            e.printStackTrace()
            showError("Erro ao inicializar tela: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega dados toda vez que o fragment é exibido (ao voltar do Registro Activity)
        sharedViewModel.loadRegistrations()
    }

    /**
     * Configura o RecyclerView
     */
    private fun setupRecyclerView() {
        try {
            registrosAdapter = RegistrosAdapter(
                onItemClick = { registration ->
                    // Handle item click (open details)
                    openRegistrationDetails(registration)
                },
                onEditClick = { registration ->
                    // Handle edit click
                    editRegistration(registration)
                },
                onShareClick = { registration ->
                    // Handle share click
                    shareRegistration(registration)
                }
            )

            val recyclerView = binding?.recyclerView ?: run {
                Log.w("RegistrosListFragment", "RecyclerView is null")
                return
            }

            // Use StaggeredGrid for better visual presentation
            recyclerView.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = registrosAdapter
                setHasFixedSize(true)
            }
            
            Log.d("RegistrosListFragment", "setupRecyclerView - RecyclerView configured")
        } catch (e: Exception) {
            Log.e("RegistrosListFragment", "setupRecyclerView error", e)
            e.printStackTrace()
        }
    }

    /**
     * Configura o SwipeRefreshLayout
     */
    private fun setupSwipeRefresh() {
        try {
            val swipeLayout = binding?.swipeRefreshLayout ?: run {
                Log.w("RegistrosListFragment", "SwipeRefreshLayout is null")
                return
            }
            
            swipeLayout.setOnRefreshListener {
                sharedViewModel.refreshData()
            }
            
            // Set color scheme for refresh indicator
            swipeLayout.setColorSchemeResources(
                R.color.primary_green,
                R.color.secondary_green
            )
            
            Log.d("RegistrosListFragment", "setupSwipeRefresh - SwipeRefresh configured")
        } catch (e: Exception) {
            Log.e("RegistrosListFragment", "setupSwipeRefresh error", e)
            e.printStackTrace()
        }
    }

    /**
     * Configura os filtros de categoria
     */
    private fun setupFilters() {
        try {
            val chipGroup = binding?.chipGroupFilters ?: run {
                Log.w("RegistrosListFragment", "chipGroupFilters is null")
                return
            }
            
            Log.d("RegistrosListFragment", "setupFilters - chipGroup found, setting listener")
            
            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                Log.d("RegistrosListFragment", "setupFilters - chip checked, ids: $checkedIds")
                try {
                    if (checkedIds.isNotEmpty()) {
                        val filter = when (checkedIds.first()) {
                            R.id.chipAll -> FiltroCategoria.TODOS
                            R.id.chipPlants -> FiltroCategoria.PLANTAS
                            R.id.chipInsects -> FiltroCategoria.INSETOS
                            else -> FiltroCategoria.TODOS
                        }
                        sharedViewModel.applyFilter(filter)
                        updateFilterCounts()
                    }
                } catch (e: Exception) {
                    Log.e("RegistrosListFragment", "Error in chip listener", e)
                }
            }
            
            Log.d("RegistrosListFragment", "setupFilters - listener set, attempting to check chipAll")
            
            // Set initial selection - use post to ensure view is fully ready
            view?.post {
                try {
                    binding?.chipAll?.isChecked = true
                    Log.d("RegistrosListFragment", "setupFilters - chipAll checked successfully")
                } catch (e: Exception) {
                    Log.e("RegistrosListFragment", "Error checking chipAll", e)
                }
            }
            
            Log.d("RegistrosListFragment", "setupFilters - filters configured")
        } catch (e: Exception) {
            Log.e("RegistrosListFragment", "setupFilters error", e)
            e.printStackTrace()
        }
    }

    /**
     * Atualiza contadores nos chips
     */
    private fun updateFilterCounts() {
        try {
            val (total, plants, insects) = sharedViewModel.getFilterCounts()
            
            Log.d("RegistrosListFragment", "updateFilterCounts - total:$total, plants:$plants, insects:$insects")
            
            binding?.chipAll?.apply {
                text = "Todos ($total)"
            }
            binding?.chipPlants?.apply {
                text = "Plantas ($plants)"
            }
            binding?.chipInsects?.apply {
                text = "Insetos ($insects)"
            }
            
            Log.d("RegistrosListFragment", "updateFilterCounts - chips updated")
        } catch (e: Exception) {
            Log.e("RegistrosListFragment", "updateFilterCounts error", e)
            e.printStackTrace()
        }
    }

    /**
     * Configura a funcionalidade de busca
     */
    private fun setupSearch() {
        // Busca removida do novo layout - foi simplificado
        Log.d("RegistrosListFragment", "setupSearch - não está no novo layout")
    }

    /**
     * Observa mudanças no ViewModel
     */
    private fun observeViewModel() {
        // Observe combined registrations with filters
        sharedViewModel.filteredCombinedRegistrations.observe(viewLifecycleOwner) { registrations ->
            updateRegistrationsList(registrations)
        }
        
        // Observe loading state
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
        
        // Observe error messages
        sharedViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showError(errorMessage)
                sharedViewModel.clearError()
            }
        }
        
        // Observe current filter
        sharedViewModel.currentFilter.observe(viewLifecycleOwner) { filter ->
            updateEmptyStateForFilter(filter)
        }
    }

    /**
     * Atualiza a lista de plantas
     */
    private fun updatePlantsList(plantas: List<Planta>) {
        val registrations = plantas.map { RegistrationItem.PlantItem(it) }
        updateRegistrationsList(registrations)
    }

    /**
     * Atualiza a lista de insetos
     */
    private fun updateInsectsList(insetos: List<Inseto>) {
        val registrations = insetos.map { RegistrationItem.InsectItem(it) }
        updateRegistrationsList(registrations)
    }

    /**
     * Atualiza a lista de registros
     */
    private fun updateRegistrationsList(registrations: List<RegistrationItem>) {
        registrosAdapter.submitList(registrations)
        
        // Show/hide empty state
        if (registrations.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }
    
    /**
     * Atualiza o estado vazio baseado no filtro atual
     */
    private fun updateEmptyStateForFilter(filter: FiltroCategoria) {
        // Empty state agora é simples - só mudar visibilidade
        val isEmpty = sharedViewModel.filteredCombinedRegistrations.value?.isEmpty() ?: true
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Atualiza o estado vazio baseado na busca
     */
    private fun updateEmptyStateForSearch(query: String) {
        val isEmpty = sharedViewModel.filteredCombinedRegistrations.value?.isEmpty() ?: true
        binding.layoutEmptyState.visibility = if (isEmpty && query.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Mostra o estado vazio
     */
    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    /**
     * Esconde o estado vazio
     */
    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    /**
     * Atualiza as estatísticas exibidas
     */
    private fun updateStatistics(stats: RegistrationStats) {
        // Stats não está no novo layout simplificado
        Log.d("RegistrosListFragment", "Stats não exibidas no novo layout")
    }

    /**
     * Atualiza estatísticas baseadas nos resultados da busca
     */
    private fun updateSearchResultsStats(searchResults: SearchResults) {
        // Search stats não está no novo layout
        Log.d("RegistrosListFragment", "Search stats não exibidas no novo layout")
    }

    /**
     * Executa a busca com o texto atual (não usado no novo layout)
     */
    private fun performSearch() {
        Log.d("RegistrosListFragment", "performSearch - não está no novo layout simplificado")
    }

    /**
     * Abre detalhes do registro
     */
    private fun openRegistrationDetails(registration: RegistrationItem) {
        // TODO: Implement navigation to registration details
        when (registration) {
            is RegistrationItem.PlantItem -> {
                // Navigate to plant details
            }
            is RegistrationItem.InsectItem -> {
                // Navigate to insect details
            }
        }
    }

    /**
     * Edita o registro
     */
    private fun editRegistration(registration: RegistrationItem) {
        when (registration) {
            is RegistrationItem.PlantItem -> {
                // Navigate to plant edit screen
                navigateToPlantEdit(registration.planta)
            }
            is RegistrationItem.InsectItem -> {
                // Navigate to insect edit screen
                navigateToInsectEdit(registration.inseto)
            }
        }
    }

    /**
     * Compartilha o registro
     */
    private fun shareRegistration(registration: RegistrationItem) {
        val shareText = when (registration) {
            is RegistrationItem.PlantItem -> {
                val planta = registration.planta
                "Confira esta planta que registrei: ${planta.nome} em ${planta.local}"
            }
            is RegistrationItem.InsectItem -> {
                val inseto = registration.inseto
                "Confira este inseto que registrei: ${inseto.nome} em ${inseto.local}"
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartilhar registro"))
    }

    /**
     * Mostra diálogo para escolher tipo de registro
     */
    private fun showRegistrationTypeDialog() {
        val items = arrayOf(
            getString(R.string.filter_plants),
            getString(R.string.filter_insects)
        )
        
        AlertDialog.Builder(requireContext())
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
        val intent = Intent(requireContext(), RegistroPlantaActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navega para o registro de insetos
     */
    private fun navigateToInsectRegistration() {
        val intent = Intent(requireContext(), RegistroInsetoActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navega para edição de planta
     */
    private fun navigateToPlantEdit(planta: Planta) {
        // TODO: Implement navigation
        val intent = Intent(requireContext(), RegistroPlantaActivity::class.java).apply {
            putExtra("PLANT_ID", planta.id)
            putExtra("EDIT_MODE", true)
        }
        startActivity(intent)
    }

    /**
     * Navega para edição de inseto
     */
    private fun navigateToInsectEdit(inseto: Inseto) {
        // TODO: Implement navigation
        val intent = Intent(requireContext(), RegistroInsetoActivity::class.java).apply {
            putExtra("INSECT_ID", inseto.id)
            putExtra("EDIT_MODE", true)
        }
        startActivity(intent)
    }

    /**
     * Exibe mensagem de erro
     */
    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}