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
            
            // Score de relevância
            binding.textRelevanceScore.text = "%.1f".format(item.relevanceScore)
        }

        private fun bindPlant(plant: PublicPlanta) {
            binding.apply {
                // Ícone do tipo
                imageViewTypeIcon.setImageResource(R.drawable.ic_planta_24dp)
                
                // Informações principais
                textViewTitle.text = plant.nome
                textViewSubtitle.text = "Planta • ${plant.familia}"
                textViewDescription.text = plant.descricao
                
                // Status da planta
                textViewStatus.apply {
                    text = plant.status
                    setTextColor(getStatusColor(plant.status))
                }
                
                // Localização
                if (plant.localizacao.isNotEmpty()) {
                    textViewLocation.apply {
                        visibility = View.VISIBLE
                        text = plant.localizacao
                    }
                    imageViewLocation.visibility = View.VISIBLE
                } else {
                    textViewLocation.visibility = View.GONE
                    imageViewLocation.visibility = View.GONE
                }
                
                // Data
                textViewDate.text = formatDate(plant.dataRegistro)
                
                // Usuário que registrou
                textViewUser.text = "Por ${plant.nomeUsuario}"
                
                // Imagem da planta
                if (plant.imageUrl.isNotEmpty()) {
                    imageViewPhoto.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(plant.imageUrl)
                        .placeholder(R.drawable.ic_planta_24dp)
                        .error(R.drawable.ic_error_24dp)
                        .into(imageViewPhoto)
                } else {
                    imageViewPhoto.visibility = View.GONE
                }
                
                // Informações adicionais
                textViewAdditionalInfo.text = "Altura: ${plant.altura}cm"
                
                // Verificado
                imageViewVerified.visibility = if (plant.verificado) View.VISIBLE else View.GONE
            }
        }

        private fun bindInsect(insect: PublicInseto) {
            binding.apply {
                // Ícone do tipo
                imageViewTypeIcon.setImageResource(R.drawable.ic_inseto_24dp)
                
                // Informações principais
                textViewTitle.text = insect.nome
                textViewSubtitle.text = "Inseto • ${insect.familia}"
                textViewDescription.text = insect.descricao
                
                // Tipo do inseto
                textViewStatus.apply {
                    text = insect.tipo
                    setTextColor(getInsectTypeColor(insect.tipo))
                }
                
                // Localização
                if (insect.localizacao.isNotEmpty()) {
                    textViewLocation.apply {
                        visibility = View.VISIBLE
                        text = insect.localizacao
                    }
                    imageViewLocation.visibility = View.VISIBLE
                } else {
                    textViewLocation.visibility = View.GONE
                    imageViewLocation.visibility = View.GONE
                }
                
                // Data
                textViewDate.text = formatDate(insect.dataRegistro)
                
                // Usuário que registrou
                textViewUser.text = "Por ${insect.nomeUsuario}"
                
                // Imagem do inseto
                if (insect.imageUrl.isNotEmpty()) {
                    imageViewPhoto.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(insect.imageUrl)
                        .placeholder(R.drawable.ic_inseto_24dp)
                        .error(R.drawable.ic_error_24dp)
                        .into(imageViewPhoto)
                } else {
                    imageViewPhoto.visibility = View.GONE
                }
                
                // Informações adicionais
                textViewAdditionalInfo.text = "Tamanho: ${insect.tamanho}mm"
                
                // Verificado
                imageViewVerified.visibility = if (insect.verificado) View.VISIBLE else View.GONE
            }
        }

        private fun bindUser(user: PublicUser) {
            binding.apply {
                // Ícone do tipo
                imageViewTypeIcon.setImageResource(R.drawable.ic_person_24dp)
                
                // Informações principais
                textViewTitle.text = user.displayName
                textViewSubtitle.text = "Usuário"
                textViewDescription.text = user.bio
                
                // Nível/Tipo do usuário
                textViewStatus.apply {
                    text = user.userLevel
                    setTextColor(getUserLevelColor(user.userLevel))
                }
                
                // Localização
                if (user.location.isNotEmpty()) {
                    textViewLocation.apply {
                        visibility = View.VISIBLE
                        text = user.location
                    }
                    imageViewLocation.visibility = View.VISIBLE
                } else {
                    textViewLocation.visibility = View.GONE
                    imageViewLocation.visibility = View.GONE
                }
                
                // Data de registro
                textViewDate.text = "Membro desde ${formatDate(user.joinDate)}"
                
                // Oculta usuário (já é o próprio usuário)
                textViewUser.visibility = View.GONE
                
                // Avatar do usuário
                if (user.profileImageUrl.isNotEmpty()) {
                    imageViewPhoto.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_person_24dp)
                        .error(R.drawable.ic_person_24dp)
                        .transform(CircleCrop())
                        .into(imageViewPhoto)
                } else {
                    imageViewPhoto.visibility = View.GONE
                }
                
                // Informações adicionais
                textViewAdditionalInfo.text = "${user.totalRegistrations} registros"
                
                // Verificado
                imageViewVerified.visibility = if (user.isVerified) View.VISIBLE else View.GONE
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status.lowercase()) {
                "saudável", "healthy" -> itemView.context.getColor(R.color.healthy_color)
                "doente", "sick" -> itemView.context.getColor(R.color.error_color)
                "em crescimento", "growing" -> itemView.context.getColor(R.color.primary_green)
                else -> itemView.context.getColor(R.color.text_secondary)
            }
        }

        private fun getInsectTypeColor(tipo: String): Int {
            return when (tipo.lowercase()) {
                "benéfico", "beneficial" -> itemView.context.getColor(R.color.beneficial_color)
                "praga", "pest" -> itemView.context.getColor(R.color.pest_color)
                "neutro", "neutral" -> itemView.context.getColor(R.color.text_secondary)
                else -> itemView.context.getColor(R.color.text_secondary)
            }
        }

        private fun getUserLevelColor(level: String): Int {
            return when (level.lowercase()) {
                "expert", "especialista" -> itemView.context.getColor(R.color.primary_green)
                "intermediário", "intermediate" -> itemView.context.getColor(R.color.beneficial_color)
                "iniciante", "beginner" -> itemView.context.getColor(R.color.text_secondary)
                else -> itemView.context.getColor(R.color.text_secondary)
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
                    oldItem.user.userId == newItem.user.userId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SearchableItem, newItem: SearchableItem): Boolean {
            return oldItem == newItem
        }
    }
}