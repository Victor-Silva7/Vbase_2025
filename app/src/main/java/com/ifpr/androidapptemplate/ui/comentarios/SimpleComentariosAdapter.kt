package com.ifpr.androidapptemplate.ui.comentarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemComentarioSimpleBinding

/**
 * Adapter SIMPLIFICADO para lista de comentários
 */
class SimpleComentariosAdapter : ListAdapter<ComentarioSimples, SimpleComentariosAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComentarioSimpleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemComentarioSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(comentario: ComentarioSimples) {
            binding.apply {
                // Nome do usuário
                textViewUserName.text = comentario.userName
                
                // Tempo relativo
                textViewTime.text = comentario.getTempoRelativo()
                
                // Conteúdo do comentário
                textViewComentario.text = comentario.conteudo
                
                // Avatar do usuário
                if (comentario.userAvatar.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(comentario.userAvatar)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(imageViewAvatar)
                } else {
                    imageViewAvatar.setImageResource(R.drawable.ic_user_placeholder)
                }
            }
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<ComentarioSimples>() {
        override fun areItemsTheSame(oldItem: ComentarioSimples, newItem: ComentarioSimples): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ComentarioSimples, newItem: ComentarioSimples): Boolean {
            return oldItem == newItem
        }
    }
}
