package com.ifpr.androidapptemplate.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifpr.androidapptemplate.databinding.FragmentPublicSearchBinding
import com.ifpr.androidapptemplate.data.model.*

/**
 * Fragment para busca pública de registros de plantas e insetos
 * Permite buscar todos os registros públicos compartilhados pelos usuários
 */
class PublicSearchFragment : Fragment() {
    
    private var _binding: FragmentPublicSearchBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PublicSearchViewModel by viewModels()
    
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var searchSuggestionsAdapter: SearchSuggestionsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublicSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupSearchInterface()
        setupObservers()
        setupClickListeners()
        
        // Carrega sugestões iniciais e buscas recentes
        viewModel.loadInitialSuggestions()
    }
    
    private fun setupRecyclerViews() {
        // Adapter para resultados de busca
        searchResultsAdapter = SearchResultsAdapter { item ->
            handleSearchResultClick(item)
        }
        
        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultsAdapter
        }
        
        // Adapter para sugestões
        searchSuggestionsAdapter = SearchSuggestionsAdapter(
            onSuggestionClick = { suggestion ->
                binding.etSearchPublic.setText(suggestion)
                viewModel.search(suggestion)
            },
            onInsertClick = { suggestion ->
                val currentText = binding.etSearchPublic.text.toString()
                val newText = if (currentText.isEmpty()) {
                    suggestion
                } else {
                    "$currentText $suggestion"
                }
                binding.etSearchPublic.setText(newText)
                binding.etSearchPublic.setSelection(newText.length)
            }
        )
        
        binding.recyclerSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchSuggestionsAdapter
        }
    }
    
    private fun setupSearchInterface() {
        // Configuração da busca em tempo real
        binding.etSearchPublic.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                viewModel.clearSearch()
            } else {
                viewModel.search(query)
                viewModel.updateSuggestions(query)
            }
        }
        
        // Botão de limpar busca
        binding.ivClearSearch.setOnClickListener {
            binding.etSearchPublic.text?.clear()
            viewModel.clearSearch()
        }
        
        // Botão de filtros
        binding.ivAdvancedFilters.setOnClickListener {
            showFiltersDialog()
        }
        
        // Botão de ordenação
        binding.ivSortOptions.setOnClickListener {
            showSortDialog()
        }
    }
    
    private fun setupObservers() {
        // Observa modo de busca
        viewModel.searchMode.observe(viewLifecycleOwner) { mode ->
            updateUIForSearchMode(mode)
        }
        
        // Observa resultados de busca
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultsAdapter.submitList(results.getAllItems())
            updateResultsCount(results.totalResults)
        }
        
        // Observa sugestões
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            searchSuggestionsAdapter.submitList(suggestions)
        }
        
        // Observa estado de carregamento
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observa mensagens de erro
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        
        // Observa filtros atuais
        viewModel.currentFilters.observe(viewLifecycleOwner) { filters ->
            updateFiltersIndicator(filters)
        }
    }
    
    private fun setupClickListeners() {
        // Limpar histórico de buscas
        binding.tvClearHistory.setOnClickListener {
            viewModel.clearSearchHistory()
        }
        
        // Clique para tentar novamente em caso de erro
        binding.btnRetrySearch.setOnClickListener {
            val query = binding.etSearchPublic.text.toString()
            if (query.isNotEmpty()) {
                viewModel.search(query)
            }
        }
    }
    
    private fun updateUIForSearchMode(mode: SearchMode) {
        // Oculta todas as views primeiro
        binding.layoutInitialState.visibility = View.GONE
        binding.recyclerSuggestions.visibility = View.GONE
        binding.layoutSearchResults.visibility = View.GONE
        binding.layoutNoResults.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.layoutLoading.visibility = View.GONE
        
        // Mostra a view apropriada baseada no modo
        when (mode) {
            SearchMode.INITIAL -> {
                binding.layoutInitialState.visibility = View.VISIBLE
            }
            SearchMode.SUGGESTIONS -> {
                binding.recyclerSuggestions.visibility = View.VISIBLE
            }
            SearchMode.SEARCHING -> {
                binding.layoutSearchResults.visibility = View.VISIBLE
                binding.layoutLoading.visibility = View.VISIBLE
            }
            SearchMode.RESULTS -> {
                binding.layoutSearchResults.visibility = View.VISIBLE
            }
            SearchMode.NO_RESULTS -> {
                binding.layoutNoResults.visibility = View.VISIBLE
            }
            SearchMode.ERROR -> {
                binding.layoutError.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateResultsCount(count: Int) {
        binding.tvResultsCount.text = when (count) {
            0 -> "Nenhum resultado"
            1 -> "1 resultado encontrado"
            else -> "$count resultados encontrados"
        }
    }
    
    private fun updateFiltersIndicator(filters: SearchFilters) {
        val hasActiveFilters = filters.hasActiveFilters()
        binding.ivAdvancedFilters.isSelected = hasActiveFilters
    }
    
    private fun handleSearchResultClick(item: SearchableItem) {
        when (item) {
            is SearchableItem.PlantResult -> {
                // Navegar para detalhes da planta
                Toast.makeText(requireContext(), "Planta: ${item.plant.nome}", Toast.LENGTH_SHORT).show()
            }
            is SearchableItem.InsectResult -> {
                // Navegar para detalhes do inseto
                Toast.makeText(requireContext(), "Inseto: ${item.insect.nome}", Toast.LENGTH_SHORT).show()
            }
            is SearchableItem.UserResult -> {
                // Navegar para perfil do usuário
                Toast.makeText(requireContext(), "Usuário: ${item.user.nome}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showFiltersDialog() {
        // TODO: Implementar dialog de filtros
        Toast.makeText(requireContext(), "Filtros em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showSortDialog() {
        // TODO: Implementar dialog de ordenação
        Toast.makeText(requireContext(), "Ordenação em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}