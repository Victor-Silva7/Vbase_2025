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

/**
 * Fragment para exibir lista de registros (plantas ou insetos)
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
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupEmptyState()
        observeViewModel()
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
            sharedViewModel.refreshData()
        }
        
        // Set color scheme for refresh indicator
        binding.swipeRefreshLayout.setColorSchemeResources(
            com.ifpr.androidapptemplate.R.color.primary_green,
            com.ifpr.androidapptemplate.R.color.secondary_green
        )
    }

    /**
     * Configura o estado vazio baseado no tipo de lista
     */
    private fun setupEmptyState() {
        when (listType) {
            TYPE_PLANTS -> {
                binding.ivEmptyIcon.setImageResource(com.ifpr.androidapptemplate.R.drawable.ic_planta_24dp)
                binding.tvEmptyTitle.text = getString(com.ifpr.androidapptemplate.R.string.no_plants_registered)
                binding.tvEmptyMessage.text = getString(com.ifpr.androidapptemplate.R.string.no_plants_message)
            }
            TYPE_INSECTS -> {
                binding.ivEmptyIcon.setImageResource(com.ifpr.androidapptemplate.R.drawable.ic_inseto_24dp)
                binding.tvEmptyTitle.text = getString(com.ifpr.androidapptemplate.R.string.no_insects_registered)
                binding.tvEmptyMessage.text = getString(com.ifpr.androidapptemplate.R.string.no_insects_message)
            }
        }

        binding.btnAddFirst.setOnClickListener {
            when (listType) {
                TYPE_PLANTS -> navigateToPlantRegistration()
                TYPE_INSECTS -> navigateToInsectRegistration()
            }
        }

        binding.btnRetry.setOnClickListener {
            sharedViewModel.refreshData()
        }
    }

    /**
     * Observa mudanças no ViewModel
     */
    private fun observeViewModel() {
        // Observe the appropriate filtered data based on list type
        when (listType) {
            TYPE_PLANTS -> {
                sharedViewModel.filteredPlants.observe(viewLifecycleOwner) { plantas ->
                    updatePlantsList(plantas)
                }
                // Also observe original data as fallback
                sharedViewModel.userPlants.observe(viewLifecycleOwner) { plantas ->
                    // Only update if no search is active
                    if (sharedViewModel.searchQuery.value.isNullOrEmpty()) {
                        updatePlantsList(plantas)
                    }
                }
            }
            TYPE_INSECTS -> {
                sharedViewModel.filteredInsects.observe(viewLifecycleOwner) { insetos ->
                    updateInsectsList(insetos)
                }
                // Also observe original data as fallback
                sharedViewModel.userInsects.observe(viewLifecycleOwner) { insetos ->
                    // Only update if no search is active
                    if (sharedViewModel.searchQuery.value.isNullOrEmpty()) {
                        updateInsectsList(insetos)
                    }
                }
            }
        }

        // Observe loading state
        sharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
        
        // Observe searching state
        sharedViewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            if (isSearching) {
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        // Observe search query changes
        sharedViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            // Update empty state message based on search
            updateEmptyStateForSearch(query)
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
     * Atualiza o estado vazio baseado na busca
     */
    private fun updateEmptyStateForSearch(query: String) {
        if (query.isNotEmpty()) {
            // Update empty state for search
            binding.tvEmptyTitle.text = "Nenhum resultado encontrado"
            binding.tvEmptyMessage.text = "Não encontramos registros para \"$query\".\nTente uma busca diferente."
            binding.btnAddFirst.text = "Nova busca"
            binding.btnAddFirst.setOnClickListener {
                // Clear search
                sharedViewModel.clearSearch()
            }
        } else {
            // Reset to normal empty state
            setupEmptyState()
        }
    }



    /**
     * Mostra o estado vazio
     */
    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }

    /**
     * Esconde o estado vazio
     */
    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
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

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }

        startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar registro"))
    }

    /**
     * Navega para registro de plantas
     */
    private fun navigateToPlantRegistration() {
        // TODO: Implement navigation
        // val intent = android.content.Intent(requireContext(), RegistroPlantaActivity::class.java)
        // startActivity(intent)
    }

    /**
     * Navega para registro de insetos
     */
    private fun navigateToInsectRegistration() {
        // TODO: Implement navigation
        // val intent = android.content.Intent(requireContext(), RegistroInsetoActivity::class.java)
        // startActivity(intent)
    }

    /**
     * Navega para edição de planta
     */
    private fun navigateToPlantEdit(planta: Planta) {
        // TODO: Implement navigation
        // val intent = android.content.Intent(requireContext(), RegistroPlantaActivity::class.java).apply {
        //     putExtra("PLANT_ID", planta.id)
        //     putExtra("EDIT_MODE", true)
        // }
        // startActivity(intent)
    }

    /**
     * Navega para edição de inseto
     */
    private fun navigateToInsectEdit(inseto: Inseto) {
        // TODO: Implement navigation
        // val intent = android.content.Intent(requireContext(), RegistroInsetoActivity::class.java).apply {
        //     putExtra("INSECT_ID", inseto.id)
        //     putExtra("EDIT_MODE", true)
        // }
        // startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}