package com.ifpr.androidapptemplate.ui.registro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemRegistroCardBinding
import com.ifpr.androidapptemplate.data.model.PlantHealthCategory
import com.ifpr.androidapptemplate.data.model.InsectCategory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para RecyclerView de registros (plantas e insetos)
 */
class RegistrosAdapter(
    private val onItemClick: (RegistrationItem) -> Unit,
    private val onEditClick: (RegistrationItem) -> Unit,
    private val onShareClick: (RegistrationItem) -> Unit
) : ListAdapter<RegistrationItem, RegistrosAdapter.RegistroViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val binding = ItemRegistroCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RegistroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder para items de registro
     */
    inner class RegistroViewHolder(
        private val binding: ItemRegistroCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RegistrationItem) {
            // Set common data
            binding.tvRegistrationName.text = item.commonName
            binding.tvLocation.text = item.commonLocation
            binding.tvRegistrationDate.text = item.commonDate

            // Set observation if not empty
            if (item.commonObservation.isNotEmpty()) {
                binding.tvObservation.text = item.commonObservation
                binding.tvObservation.visibility = View.VISIBLE
            } else {
                binding.tvObservation.visibility = View.GONE
            }

            // Set image
            loadRegistrationImage(item)

            // Set type-specific data
            when (item) {
                is RegistrationItem.PlantItem -> {
                    bindPlantData(item)
                }
                is RegistrationItem.InsectItem -> {
                    bindInsectData(item)
                }
            }

            // Set image count if multiple images
            if (item.commonImages.size > 1) {
                binding.layoutImageCount.visibility = View.VISIBLE
                binding.tvImageCount.text = item.commonImages.size.toString()
            } else {
                binding.layoutImageCount.visibility = View.GONE
            }

            // Set click listeners
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.btnEdit.setOnClickListener {
                onEditClick(item)
            }

            binding.btnShare.setOnClickListener {
                onShareClick(item)
            }
        }

        /**
         * Vincula dados específicos de plantas
         */
        private fun bindPlantData(plantItem: RegistrationItem.PlantItem) {
            val planta = plantItem.planta

            // Set type icon
            binding.ivTypeIcon.setImageResource(R.drawable.ic_planta_24dp)

            // Set scientific name if available
            if (planta.nomeCientifico.isNotEmpty()) {
                binding.tvScientificName.text = planta.nomeCientifico
                binding.tvScientificName.visibility = View.VISIBLE
            } else {
                binding.tvScientificName.visibility = View.GONE
            }

            // Set category badge
            when (planta.categoria) {
                PlantHealthCategory.HEALTHY -> {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_saudavel_24dp)
                    binding.tvCategoryText.text = binding.root.context.getString(R.string.healthy)
                    binding.layoutCategoryBadge.setBackgroundResource(R.color.healthy_color)
                }
                PlantHealthCategory.SICK -> {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_doente_24dp)
                    binding.tvCategoryText.text = binding.root.context.getString(R.string.sick)
                    binding.layoutCategoryBadge.setBackgroundResource(R.color.sick_color)
                }
            }
        }

        /**
         * Vincula dados específicos de insetos
         */
        private fun bindInsectData(insectItem: RegistrationItem.InsectItem) {
            val inseto = insectItem.inseto

            // Set type icon
            binding.ivTypeIcon.setImageResource(R.drawable.ic_inseto_24dp)

            // Set scientific name if available
            if (inseto.nomeCientifico.isNotEmpty()) {
                binding.tvScientificName.text = inseto.nomeCientifico
                binding.tvScientificName.visibility = View.VISIBLE
            } else {
                binding.tvScientificName.visibility = View.GONE
            }

            // Set category badge
            when (inseto.categoria) {
                InsectCategory.BENEFICIAL -> {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_benefico_24dp)
                    binding.tvCategoryText.text = binding.root.context.getString(R.string.beneficial)
                    binding.layoutCategoryBadge.setBackgroundResource(R.color.beneficial_color)
                }
                InsectCategory.NEUTRAL -> {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_neutro_24dp)
                    binding.tvCategoryText.text = binding.root.context.getString(R.string.neutral)
                    binding.layoutCategoryBadge.setBackgroundResource(R.color.neutral_color)
                }
                InsectCategory.PEST -> {
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_praga_24dp)
                    binding.tvCategoryText.text = binding.root.context.getString(R.string.pest)
                    binding.layoutCategoryBadge.setBackgroundResource(R.color.pest_color)
                }
            }
        }

        /**
         * Carrega a imagem do registro
         */
        private fun loadRegistrationImage(item: RegistrationItem) {
            val firstImage = item.commonImages.firstOrNull()

            if (firstImage != null) {
                // For now, just use placeholder until Glide is properly configured
                binding.ivRegistrationImage.setImageResource(R.drawable.ic_image_placeholder)
            } else {
                // Set placeholder based on type
                val placeholderRes = when (item.commonType) {
                    "PLANTA" -> R.drawable.ic_planta_24dp
                    "INSETO" -> R.drawable.ic_inseto_24dp
                    else -> R.drawable.ic_image_placeholder
                }
                binding.ivRegistrationImage.setImageResource(placeholderRes)
            }
        }
    }

    /**
     * DiffUtil callback para otimizar atualizações da lista
     */
    private class DiffCallback : DiffUtil.ItemCallback<RegistrationItem>() {
        override fun areItemsTheSame(oldItem: RegistrationItem, newItem: RegistrationItem): Boolean {
            return oldItem.commonId == newItem.commonId
        }

        override fun areContentsTheSame(oldItem: RegistrationItem, newItem: RegistrationItem): Boolean {
            return when {
                oldItem is RegistrationItem.PlantItem && newItem is RegistrationItem.PlantItem -> {
                    oldItem.planta == newItem.planta
                }
                oldItem is RegistrationItem.InsectItem && newItem is RegistrationItem.InsectItem -> {
                    oldItem.inseto == newItem.inseto
                }
                else -> false
            }
        }
    }
}