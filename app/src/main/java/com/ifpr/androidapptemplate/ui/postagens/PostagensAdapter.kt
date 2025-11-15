package com.ifpr.androidapptemplate.ui.postagens

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemPostagemCardBinding
import com.ifpr.androidapptemplate.data.model.PostagemFeed
import java.util.*

/**
 * Adapter para exibir postagens em RecyclerView
 * Usa ListAdapter com DiffUtil para performance otimizada
 */
class PostagensAdapter(
    private val onLikeClick: (PostagemFeed) -> Unit = {},
    private val onCommentClick: (PostagemFeed) -> Unit = {},
    private val onShareClick: (PostagemFeed) -> Unit = {},
    private val onItemClick: (PostagemFeed) -> Unit = {}
) : ListAdapter<PostagemFeed, PostagensAdapter.PostagemViewHolder>(PostagemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostagemViewHolder {
        val binding = ItemPostagemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostagemViewHolder(binding, onLikeClick, onCommentClick, onShareClick, onItemClick)
    }

    override fun onBindViewHolder(holder: PostagemViewHolder, position: Int) {
        val postagem = getItem(position)
        holder.bind(postagem)
    }

    /**
     * ViewHolder para uma postagem individual
     */
    class PostagemViewHolder(
        private val binding: ItemPostagemCardBinding,
        private val onLikeClick: (PostagemFeed) -> Unit,
        private val onCommentClick: (PostagemFeed) -> Unit,
        private val onShareClick: (PostagemFeed) -> Unit,
        private val onItemClick: (PostagemFeed) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(postagem: PostagemFeed) {
            // Header do usuário
            binding.textViewUserName.text = postagem.usuario.nome
            binding.textViewUserLocation.text = postagem.usuario.localizacao.ifEmpty { "Localização não informada" }
            
            // Badge de verificação
            binding.imageViewVerified.visibility = 
                if (postagem.usuario.isVerificado) View.VISIBLE else View.GONE

            // Avatar do usuário
            if (postagem.usuario.avatarUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(postagem.usuario.avatarUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(binding.imageViewUserAvatar)
            } else {
                binding.imageViewUserAvatar.setImageResource(R.drawable.ic_user_placeholder)
            }

            // Conteúdo da postagem
            binding.textViewPostTitle.text = postagem.titulo
            binding.textViewPostDescription.text = postagem.descricao
            binding.textViewPostTime.text = postagem.getTempoPostagem()

            // Imagem da postagem
            if (postagem.imageUrl.isNotEmpty()) {
                try {
                    // Se for base64, decodificar
                    if (postagem.imageUrl.startsWith("data:image")) {
                        val base64String = postagem.imageUrl.substringAfter("base64,")
                        val imageData = Base64.getDecoder().decode(base64String)
                        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                        binding.imageViewPostPhoto.setImageBitmap(bitmap)
                    } else {
                        // Senão, carregar como URL normal
                        Glide.with(binding.root.context)
                            .load(postagem.imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(binding.imageViewPostPhoto)
                    }
                    binding.imageViewPostPhoto.visibility = View.VISIBLE
                } catch (e: Exception) {
                    binding.imageViewPostPhoto.visibility = View.GONE
                }
            } else {
                binding.imageViewPostPhoto.visibility = View.GONE
            }

            // Interações - Atualizar estatísticas
            val stats = String.format(
                "%d curtidas • %d comentários • %d compartilhamentos",
                postagem.interacoes.curtidas,
                postagem.interacoes.comentarios,
                postagem.interacoes.compartilhamentos
            )
            binding.textViewInteractionStats.text = stats

            // Botões de ação
            binding.buttonLike.setOnClickListener { onLikeClick(postagem) }
            binding.buttonComment.setOnClickListener { onCommentClick(postagem) }
            binding.buttonShare.setOnClickListener { onShareClick(postagem) }

            // Click no card inteiro
            binding.root.setOnClickListener { onItemClick(postagem) }
        }
    }

    /**
     * DiffCallback para otimização com ListAdapter
     */
    private class PostagemDiffCallback : DiffUtil.ItemCallback<PostagemFeed>() {
        override fun areItemsTheSame(oldItem: PostagemFeed, newItem: PostagemFeed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostagemFeed, newItem: PostagemFeed): Boolean {
            return oldItem == newItem
        }
    }
}
