package com.ifpr.androidapptemplate.ui.comentarios

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemComentarioBinding
import com.ifpr.androidapptemplate.data.model.Comentario

/**
 * Adapter para exibir comentários com suporte a respostas aninhadas
 * Implementa scroll infinito e interações sociais
 */
class ComentariosAdapter(
    private val onLikeClick: (Comentario) -> Unit,
    private val onReplyClick: (Comentario) -> Unit,
    private val onMoreOptionsClick: (Comentario) -> Unit,
    private val onAttachmentClick: (String) -> Unit,
    private val onLoadMore: () -> Unit
) : ListAdapter<Comentario, ComentariosAdapter.ComentarioViewHolder>(ComentarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val binding = ItemComentarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComentarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        holder.bind(getItem(position))
        
        // Trigger load more quando próximo do final
        if (position >= itemCount - 5) {
            onLoadMore()
        }
    }

    inner class ComentarioViewHolder(
        private val binding: ItemComentarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comentario: Comentario) {
            bindUserInfo(comentario)
            bindContent(comentario)
            bindAttachments(comentario)
            bindActions(comentario)
            bindReplies(comentario)
            setupClickListeners(comentario)
        }

        private fun bindUserInfo(comentario: Comentario) {
            binding.apply {
                // Avatar do usuário
                if (comentario.usuario.avatarUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(comentario.usuario.avatarUrl)
                        .placeholder(R.drawable.ic_person_24dp)
                        .error(R.drawable.ic_person_24dp)
                        .transform(CircleCrop())
                        .into(imageViewUserAvatar)
                } else {
                    imageViewUserAvatar.setImageResource(R.drawable.ic_person_24dp)
                }
                
                // Nome do usuário
                textViewUserName.text = comentario.usuario.nomeExibicao
                
                // Verificação do usuário
                imageViewVerified.visibility = if (comentario.usuario.isVerificado) View.VISIBLE else View.GONE
                
                // Nível do usuário
                textViewUserLevel.apply {
                    text = comentario.usuario.nivel.name.lowercase().replaceFirstChar { it.uppercase() }
                    setTextColor(Color.parseColor(getUserLevelColor(comentario.usuario.nivel)))
                }
                
                // Tempo do comentário
                textViewTime.text = comentario.getRelativeTime()
            }
        }

        private fun bindContent(comentario: Comentario) {
            binding.apply {
                // Conteúdo do comentário
                textViewContent.text = comentario.conteudo
                
                // Indicador de edição
                textViewEdited.visibility = if (comentario.isEdited) View.VISIBLE else View.GONE
            }
        }

        private fun bindAttachments(comentario: Comentario) {
            binding.apply {
                // Anexos
                if (comentario.attachments.isNotEmpty()) {
                    recyclerViewAttachments.visibility = View.VISIBLE
                    recyclerViewAttachments.layoutManager = LinearLayoutManager(
                        itemView.context, 
                        LinearLayoutManager.HORIZONTAL, 
                        false
                    )
                    
                    val adapter = AttachmentPreviewAdapter(comentario.attachments) { imageUrl ->
                        onAttachmentClick(imageUrl)
                    }
                    recyclerViewAttachments.adapter = adapter
                } else {
                    recyclerViewAttachments.visibility = View.GONE
                }
            }
        }

        private fun bindActions(comentario: Comentario) {
            binding.apply {
                // Estado do botão curtir
                if (comentario.likedByUser) {
                    imageViewLike.setImageResource(R.drawable.ic_favorite_24dp)
                    imageViewLike.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.like_color)
                    )
                    textViewLikeCount.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.like_color)
                    )
                } else {
                    imageViewLike.setImageResource(R.drawable.ic_favorite_border_24dp)
                    imageViewLike.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                    textViewLikeCount.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                }
                
                // Contador de curtidas
                textViewLikeCount.text = if (comentario.likes > 0) comentario.likes.toString() else ""
            }
        }

        private fun bindReplies(comentario: Comentario) {
            binding.apply {
                if (comentario.hasReplies()) {
                    layoutReplies.visibility = View.VISIBLE
                    textViewRepliesCount.text = "${comentario.totalReplies} ${if (comentario.totalReplies == 1) "resposta" else "respostas"}"
                } else {
                    layoutReplies.visibility = View.GONE
                }
            }
        }

        private fun setupClickListeners(comentario: Comentario) {
            binding.apply {
                // Curtir comentário
                buttonLike.setOnClickListener {
                    onLikeClick(comentario)
                }
                
                // Responder comentário
                buttonReply.setOnClickListener {
                    onReplyClick(comentario)
                }
                
                // Mais opções
                buttonMoreOptions.setOnClickListener {
                    onMoreOptionsClick(comentario)
                }
                
                // Expandir/colapsar respostas
                layoutHiddenReplies.setOnClickListener {
                    // Em uma implementação completa, carregaríamos as respostas
                }
            }
        }

        private fun getUserLevelColor(nivel: com.ifpr.androidapptemplate.data.model.NivelUsuario): String {
            return when (nivel) {
                com.ifpr.androidapptemplate.data.model.NivelUsuario.INICIANTE -> "#757575"
                com.ifpr.androidapptemplate.data.model.NivelUsuario.INTERMEDIARIO -> "#2196F3"
                com.ifpr.androidapptemplate.data.model.NivelUsuario.AVANCADO -> "#4CAF50"
                com.ifpr.androidapptemplate.data.model.NivelUsuario.ESPECIALISTA -> "#FF9800"
            }
        }
    }

    class ComentarioDiffCallback : DiffUtil.ItemCallback<Comentario>() {
        override fun areItemsTheSame(oldItem: Comentario, newItem: Comentario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comentario, newItem: Comentario): Boolean {
            return oldItem == newItem
        }
    }
}