package com.ifpr.androidapptemplate.ui.registro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemRegistroCardBinding
import com.ifpr.androidapptemplate.data.model.PlantHealthCategory
import com.ifpr.androidapptemplate.data.model.InsectCategory

/**
 * Adapter simplificado e robusto para exibir registros (plantas e insetos)
 * Foca apenas nas 4 informações essenciais:
 * 1. Tipo (PLANTA/INSETO)
 * 2. Imagem
 * 3. Descrição
 * 4. Data
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
     * ViewHolder para itens de registro
     */
    inner class RegistroViewHolder(
        private val binding: ItemRegistroCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RegistrationItem) {
            try {
                // Exibe nome/título do registro
                binding.tvRegistrationName.text = item.commonName

                // Exibe observação/descrição
                binding.tvObservation.text = item.commonObservation

                // Exibe data
                binding.tvRegistrationDate.text = item.commonDate

                // Carrega imagem
                loadImage(item)

                // Configura tipo (PLANTA ou INSETO)
                setupTypeLabel(item)

                // Configura cliques
                setupClickListeners(item)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Carrega a imagem do registro
         */
        private fun loadImage(item: RegistrationItem) {
            try {
                val firstImage = item.commonImages.firstOrNull()
                val context = binding.root.context

                if (!firstImage.isNullOrBlank()) {
                    Glide.with(context)
                        .load(firstImage)
                        .apply(
                            RequestOptions()
                                .transform(CenterCrop(), RoundedCorners(8))
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(binding.ivRegistrationImage)
                } else {
                    // Se não houver imagem, exibe placeholder genérico
                    Glide.with(context)
                        .load(R.drawable.ic_image_placeholder)
                        .into(binding.ivRegistrationImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Configura rótulo de tipo (PLANTA/INSETO)
         */
        private fun setupTypeLabel(item: RegistrationItem) {
            try {
                val context = binding.root.context

                when (item) {
                    is RegistrationItem.PlantItem -> {
                        binding.tvTypeLabel.text = "PLANTA"
                        binding.ivTypeIcon.setImageResource(R.drawable.ic_planta_24dp)
                        binding.layoutTypeBadge.setBackgroundColor(
                            context.getColor(R.color.primary_green)
                        )
                    }
                    is RegistrationItem.InsectItem -> {
                        binding.tvTypeLabel.text = "INSETO"
                        binding.ivTypeIcon.setImageResource(R.drawable.ic_inseto_24dp)
                        binding.layoutTypeBadge.setBackgroundColor(
                            context.getColor(R.color.beneficial_color)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Configura listeners de clique
         */
        private fun setupClickListeners(item: RegistrationItem) {
            binding.root.setOnClickListener {
                try {
                    onItemClick(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * DiffUtil callback para otimizar atualizações
     */
    private class DiffCallback : DiffUtil.ItemCallback<RegistrationItem>() {
        override fun areItemsTheSame(oldItem: RegistrationItem, newItem: RegistrationItem): Boolean {
            return oldItem.commonId == newItem.commonId
        }

        override fun areContentsTheSame(oldItem: RegistrationItem, newItem: RegistrationItem): Boolean {
            return oldItem.commonName == newItem.commonName &&
                    oldItem.commonDate == newItem.commonDate &&
                    oldItem.commonImages == newItem.commonImages &&
                    oldItem.commonObservation == newItem.commonObservation
        }
    }
}