package com.ifpr.androidapptemplate.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemSearchSuggestionBinding
import com.ifpr.androidapptemplate.data.model.SearchSuggestion

/**
 * Adapter para exibir sugestões de busca e buscas recentes
 */
class SearchSuggestionsAdapter(
    private val onSuggestionClick: (String) -> Unit,
    private val onInsertClick: (String) -> Unit
) : ListAdapter<SearchSuggestion, SearchSuggestionsAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSearchSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SuggestionViewHolder(
        private val binding: ItemSearchSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: SearchSuggestion) {
            binding.apply {
                // Texto da sugestão
                textViewSuggestion.text = suggestion.text
                
                // Ícone baseado no tipo
                imageViewIcon.setImageResource(getIconForType(suggestion.type))
                
                // Clique na sugestão (substitui texto atual)
                root.setOnClickListener {
                    onSuggestionClick(suggestion.text)
                }
                
                // Clique no botão de inserir (adiciona ao texto atual)
                buttonInsert.setOnClickListener {
                    onInsertClick(suggestion.text)
                }
                
                // Descrição adicional baseada no tipo
                textViewDescription.text = getDescriptionForType(suggestion.type)
            }
        }

        private fun getIconForType(type: SuggestionType): Int {
            return when (type) {
                SuggestionType.RECENT_SEARCH -> R.drawable.ic_search_24dp
                SuggestionType.PLANT_NAME -> R.drawable.ic_planta_24dp
                SuggestionType.INSECT_NAME -> R.drawable.ic_inseto_24dp
                SuggestionType.LOCATION -> R.drawable.ic_location_on_24dp
                SuggestionType.USER_NAME -> R.drawable.ic_person_24dp
                SuggestionType.FAMILY -> R.drawable.ic_filter_list_24dp
                SuggestionType.STATUS -> R.drawable.ic_verified_24dp
                SuggestionType.POPULAR -> R.drawable.ic_star_border_24dp
            }
        }

        private fun getDescriptionForType(type: SuggestionType): String {
            return when (type) {
                SuggestionType.RECENT_SEARCH -> "Busca recente"
                SuggestionType.PLANT_NAME -> "Nome de planta"
                SuggestionType.INSECT_NAME -> "Nome de inseto"
                SuggestionType.LOCATION -> "Localização"
                SuggestionType.USER_NAME -> "Usuário"
                SuggestionType.FAMILY -> "Família"
                SuggestionType.STATUS -> "Status"
                SuggestionType.POPULAR -> "Busca popular"
            }
        }
    }

    class SuggestionDiffCallback : DiffUtil.ItemCallback<SearchSuggestion>() {
        override fun areItemsTheSame(oldItem: SearchSuggestion, newItem: SearchSuggestion): Boolean {
            return oldItem.text == newItem.text && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: SearchSuggestion, newItem: SearchSuggestion): Boolean {
            return oldItem == newItem
        }
    }
}