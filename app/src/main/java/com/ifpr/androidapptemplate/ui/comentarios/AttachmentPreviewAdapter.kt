package com.ifpr.androidapptemplate.ui.comentarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemAttachmentPreviewBinding

/**
 * Adapter para exibir previews de anexos em comentários
 */
class AttachmentPreviewAdapter(
    private val attachments: List<String>,
    private val onAttachmentClick: (String) -> Unit
) : RecyclerView.Adapter<AttachmentPreviewAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding = ItemAttachmentPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttachmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    inner class AttachmentViewHolder(
        private val binding: ItemAttachmentPreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            // Carrega a imagem do anexo
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_planta_24dp)
                .error(R.drawable.ic_error_24dp)
                .transform(RoundedCorners(16))
                .into(binding.imageViewAttachment)
            
            // Configura clique no anexo
            binding.imageViewAttachment.setOnClickListener {
                onAttachmentClick(imageUrl)
            }
        }
    }
}