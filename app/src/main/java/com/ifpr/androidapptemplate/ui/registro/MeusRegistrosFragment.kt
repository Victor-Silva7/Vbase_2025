package com.ifpr.androidapptemplate.ui.registro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.FragmentMeusRegistrosBinding
import com.ifpr.androidapptemplate.data.repository.RegistrationStats

/**
 * Fragment para exibir a lista de registros pessoais do usuário
 * Usa filtros para separar plantas, insetos ou exibir ambos
 */
class MeusRegistrosFragment : Fragment() {

    private var _binding: FragmentMeusRegistrosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MeusRegistrosViewModel by viewModels()
    
    private lateinit var registrosAdapter: RegistrosAdapter

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
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFilters()
        setupFab()
        observeViewModel()
        
        // Load initial data
        viewModel.loadRegistrations()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega dados toda vez que o fragment é exibido (ao voltar do Registro Activity)
        viewModel.loadRegistrations()
    }

    /**
     * Configura o RecyclerView
     */
    private fun setupRecyclerView() {
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

        // Use StaggeredGrid for better visual presentation
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = registrosAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * Configura o SwipeRefreshLayout
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
        
        // Set color scheme for refresh indicator
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_green,
            R.color.secondary_green
        )
    }

    /**
     * Configura os filtros de categoria
     */
    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val filter = when (checkedIds.first()) {
                    R.id.chipAll -> FiltroCategoria.TODOS
                    R.id.chipPlants -> FiltroCategoria.PLANTAS
                    R.id.chipInsects -> FiltroCategoria.INSETOS
                    else -> FiltroCategoria.TODOS
                }
                viewModel.applyFilter(filter)
                updateFilterCounts()
            }
        }
        
        // Set initial selection
        binding.chipAll.isChecked = true
        
        // Setup empty state button
        binding.btnAddFirst.setOnClickListener {
            showRegistrationTypeDialog()
        }
    }

    /**
     * Atualiza contadores nos chips
     */
    private fun updateFilterCounts() {
        val (total, plants, insects) = viewModel.getFilterCounts()
        
        binding.chipAll.text = "Todos ($total)"
        binding.chipPlants.text = "Plantas ($plants)"
        binding.chipInsects.text = "Insetos ($insects)"
    }

    /**
     * Configura a funcionalidade de busca
     */
    private fun setupSearch() {
        // Real-time search as user types
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                binding.ivClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                // Perform search with debouncing handled by ViewModel
                viewModel.searchRegistrations(query)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Search on keyboard enter
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                // Hide keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }
        
        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            binding.ivClearSearch.visibility = View.GONE
            viewModel.clearSearch()
        }
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
        // Observe combined registrations with filters
        viewModel.filteredCombinedRegistrations.observe(viewLifecycleOwner) { registrations ->
            updateRegistrationsList(registrations)
        }
        
        // Observe registration statistics
        viewModel.registrationStats.observe(viewLifecycleOwner) { stats ->
            updateStatistics(stats)
            updateFilterCounts()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showError(errorMessage)
                viewModel.clearError()
            }
        }
        
        // Observe current filter
        viewModel.currentFilter.observe(viewLifecycleOwner) { filter ->
            updateEmptyStateForFilter(filter)
        }
        
        // Observe search results
        viewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (searchResults.query.isNotEmpty()) {
                updateSearchResultsStats(searchResults)
            }
        }
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
        when (filter) {
            FiltroCategoria.TODOS -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_planta_24dp)
                binding.tvEmptyTitle.text = "Nenhum registro encontrado"
                binding.tvEmptyMessage.text = "Você ainda não tem registros.\nComece explorando e documentando a natureza!"
            }
            FiltroCategoria.PLANTAS -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_planta_24dp)
                binding.tvEmptyTitle.text = getString(R.string.no_plants_registered)
                binding.tvEmptyMessage.text = getString(R.string.no_plants_message)
            }
            FiltroCategoria.INSETOS -> {
                binding.ivEmptyIcon.setImageResource(R.drawable.ic_inseto_24dp)
                binding.tvEmptyTitle.text = getString(R.string.no_insects_registered)
                binding.tvEmptyMessage.text = getString(R.string.no_insects_message)
            }
        }
    }

    /**
     * Mostra o estado vazio
     */
    private fun showEmptyState() {
        // Atualiza o texto baseado no filtro atual antes de exibir
        val currentFilter = viewModel.currentFilter.value ?: FiltroCategoria.TODOS
        updateEmptyStateForFilter(currentFilter)
        
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
     * Ações de registro
     */
    private fun openRegistrationDetails(registration: RegistrationItem) {
        // TODO: Navigate to registration details
    }

    private fun editRegistration(registration: RegistrationItem) {
        when (registration) {
            is RegistrationItem.PlantItem -> {
                // Navigate to plant edit
                navigateToPlantRegistration()
            }
            is RegistrationItem.InsectItem -> {
                // Navigate to insect edit
                navigateToInsectRegistration()
            }
        }
    }

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

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }

        startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar registro"))
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
     * Atualiza estatísticas baseadas nos resultados da busca
     */
    private fun updateSearchResultsStats(searchResults: SearchResults) {
        if (searchResults.query.isNotEmpty()) {
            // Show search result counts
            binding.tvTotalPlantas.text = searchResults.plants.size.toString()
            binding.tvTotalInsetos.text = searchResults.insects.size.toString()
            binding.tvTotalRegistros.text = searchResults.totalResults.toString()
        }
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
        val intent = android.content.Intent(requireContext(), RegistroPlantaActivity::class.java)
        startActivity(intent)
    }

    /**
     * Navega para o registro de insetos
     */
    private fun navigateToInsectRegistration() {
        val intent = android.content.Intent(requireContext(), RegistroInsetoActivity::class.java)
        startActivity(intent)
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
}