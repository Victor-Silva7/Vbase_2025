package com.ifpr.androidapptemplate.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemSearchResultBinding
import com.ifpr.androidapptemplate.data.model.*

/**
 * Adapter para exibir resultados de busca pública
 * Suporta plantas, insetos e usuários
 */
class SearchResultsAdapter(
    private val onItemClick: (SearchableItem) -> Unit
) : ListAdapter<SearchableItem, SearchResultsAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchableItem) {
            when (item) {
                is SearchableItem.PlantResult -> bindPlant(item.plant)
                is SearchableItem.InsectResult -> bindInsect(item.insect)
                is SearchableItem.UserResult -> bindUser(item.user)
            }
            
            // Clique no item
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun bindPlant(plant: PublicPlanta) {
            binding.apply {
                // Ícone do tipo
                ivTypeBadge.setImageResource(R.drawable.ic_planta_24dp)
                
                // Informações principais
                tvResultTitle.text = plant.nome
                tvResultSubtitle.text = if (plant.nomeCientifico.isNotEmpty()) plant.nomeCientifico else plant.local
                
                // Data
                tvResultTime.text = plant.getFormattedDate()
                
                // Usuário que registrou
                tvResultAuthor.text = plant.userName
                
                // Imagem da planta
                val photoUrl = plant.imagens.firstOrNull().orEmpty()
                if (photoUrl.isNotEmpty()) {
                    ivResultImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_planta_24dp)
                        .error(R.drawable.ic_error_24dp)
                        .into(ivResultImage)
                } else {
                    ivResultImage.visibility = View.GONE
                }
            }
        }

        private fun bindInsect(insect: PublicInseto) {
            binding.apply {
                // Ícone do tipo
                ivTypeBadge.setImageResource(R.drawable.ic_inseto_24dp)
                
                // Informações principais
                tvResultTitle.text = insect.nome
                tvResultSubtitle.text = if (insect.nomeCientifico.isNotEmpty()) insect.nomeCientifico else insect.local
                
                // Data
                tvResultTime.text = insect.getFormattedDate()
                
                // Usuário que registrou
                tvResultAuthor.text = insect.userName
                
                // Imagem do inseto
                val photoUrl = insect.imagens.firstOrNull().orEmpty()
                if (photoUrl.isNotEmpty()) {
                    ivResultImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_inseto_24dp)
                        .error(R.drawable.ic_error_24dp)
                        .into(ivResultImage)
                } else {
                    ivResultImage.visibility = View.GONE
                }
            }
        }

        private fun bindUser(user: PublicUser) {
            binding.apply {
                // Ícone do tipo
                ivTypeBadge.setImageResource(R.drawable.ic_person_24dp)
                
                // Informações principais
                tvResultTitle.text = user.nome
                tvResultSubtitle.text = "@${user.username}"
                tvResultAuthor.text = "${user.totalRegistros} registros"
                
                // Data de registro
                tvResultTime.text = formatDate(user.lastActivityTimestamp)
                
                // Avatar do usuário
                if (user.avatarUrl.isNotEmpty()) {
                    ivResultImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.ic_person_24dp)
                        .error(R.drawable.ic_person_24dp)
                        .transform(CircleCrop())
                        .into(ivResultImage)
                } else {
                    ivResultImage.visibility = View.GONE
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            val minutes = diff / (1000 * 60)
            val hours = diff / (1000 * 60 * 60)
            val days = diff / (1000 * 60 * 60 * 24)

            return when {
                minutes < 1 -> "Agora"
                minutes < 60 -> "${minutes}min"
                hours < 24 -> "${hours}h"
                days < 7 -> "${days}d"
                else -> {
                    val weeks = days / 7
                    "${weeks}sem"
                }
            }
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchableItem>() {
        override fun areItemsTheSame(oldItem: SearchableItem, newItem: SearchableItem): Boolean {
            return when {
                oldItem is SearchableItem.PlantResult && newItem is SearchableItem.PlantResult ->
                    oldItem.plant.id == newItem.plant.id
                oldItem is SearchableItem.InsectResult && newItem is SearchableItem.InsectResult ->
                    oldItem.insect.id == newItem.insect.id
                oldItem is SearchableItem.UserResult && newItem is SearchableItem.UserResult ->
                    oldItem.user.id == newItem.user.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SearchableItem, newItem: SearchableItem): Boolean {
            return oldItem == newItem
        }
    }
}