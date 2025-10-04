package com.ifpr.androidapptemplate.ui.feed

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemPostagemCardBinding
import com.ifpr.androidapptemplate.data.model.*

/**
 * Adapter para cards de postagem com informações completas do usuário
 * Suporta plantas e insetos com diferentes layouts adaptativos
 */
class PostagemCardAdapter(
    private val onCardClick: (PostagemFeed) -> Unit,
    private val onUserClick: (UsuarioPostagem) -> Unit,
    private val onLikeClick: (PostagemFeed) -> Unit,
    private val onCommentClick: (PostagemFeed) -> Unit,
    private val onShareClick: (PostagemFeed) -> Unit,
    private val onBookmarkClick: (PostagemFeed) -> Unit
) : ListAdapter<PostagemFeed, PostagemCardAdapter.PostagemViewHolder>(PostagemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostagemViewHolder {
        val binding = ItemPostagemCardBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return PostagemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostagemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostagemViewHolder(
        private val binding: ItemPostagemCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(postagem: PostagemFeed) {
            bindUserInfo(postagem.usuario)
            bindPostContent(postagem)
            bindSpecificInfo(postagem)
            bindInteractions(postagem)
            setupClickListeners(postagem)
        }

        private fun bindUserInfo(usuario: UsuarioPostagem) {
            binding.apply {
                // Nome do usuário
                textViewUserName.text = usuario.nomeExibicao
                
                // Avatar do usuário
                if (usuario.avatarUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(usuario.avatarUrl)
                        .placeholder(R.drawable.ic_person_24dp)
                        .error(R.drawable.ic_person_24dp)
                        .transform(CircleCrop())
                        .into(imageViewUserAvatar)
                } else {
                    imageViewUserAvatar.setImageResource(R.drawable.ic_person_24dp)
                }
                
                // Verificação do usuário
                imageViewVerified.visibility = if (usuario.isVerificado) View.VISIBLE else View.GONE
                
                // Nível do usuário
                textViewUserLevel.apply {
                    text = usuario.getTextoNivel()
                    setTextColor(Color.parseColor(usuario.getCorNivel()))
                }
                
                // Localização do usuário
                textViewUserLocation.apply {
                    text = usuario.localizacao
                    visibility = if (usuario.localizacao.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        private fun bindPostContent(postagem: PostagemFeed) {
            binding.apply {
                // Tempo da postagem
                textViewPostTime.text = postagem.getTempoPostagem()
                
                // Ícone do tipo de postagem
                when (postagem.tipo) {
                    TipoPostagem.PLANTA -> {
                        imageViewPostType.setImageResource(R.drawable.ic_planta_24dp)
                        imageViewPostType.imageTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context, R.color.healthy_color)
                        )
                    }
                    TipoPostagem.INSETO -> {
                        imageViewPostType.setImageResource(R.drawable.ic_inseto_24dp)
                        imageViewPostType.imageTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context, R.color.warning_color)
                        )
                    }
                }
                
                // Título da postagem
                textViewPostTitle.text = postagem.titulo
                
                // Descrição da postagem
                textViewPostDescription.text = postagem.descricao
                
                // Imagem da postagem
                if (postagem.imageUrl.isNotEmpty()) {
                    imageViewPostPhoto.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(postagem.imageUrl)
                        .placeholder(R.drawable.ic_planta_24dp)
                        .error(R.drawable.ic_error_24dp)
                        .transform(RoundedCorners(24))
                        .into(imageViewPostPhoto)
                } else {
                    imageViewPostPhoto.visibility = View.GONE
                }
                
                // Localização da postagem
                if (postagem.localizacao.isNotEmpty()) {
                    layoutPostLocation.visibility = View.VISIBLE
                    textViewPostLocation.text = postagem.localizacao
                } else {
                    layoutPostLocation.visibility = View.GONE
                }
            }
        }

        private fun bindSpecificInfo(postagem: PostagemFeed) {
            binding.chipGroupInfo.removeAllViews()
            
            when (postagem.tipo) {
                TipoPostagem.PLANTA -> {
                    postagem.detalhesPlanta?.let { detalhes ->
                        // Nome científico
                        if (detalhes.nomeCientifico.isNotEmpty()) {
                            addInfoChip(detalhes.nomeCientifico, R.color.text_secondary)
                        }
                        
                        // Família
                        if (detalhes.familia.isNotEmpty()) {
                            addInfoChip(detalhes.familia, R.color.text_secondary)
                        }
                        
                        // Status
                        addInfoChip(detalhes.getTextoStatus(), Color.parseColor(detalhes.getCorStatus()))
                        
                        // Altura se disponível
                        if (detalhes.altura.isNotEmpty()) {
                            addInfoChip("${detalhes.altura}", R.color.text_hint)
                        }
                    }
                }
                
                TipoPostagem.INSETO -> {
                    postagem.detalhesInseto?.let { detalhes ->
                        // Nome científico
                        if (detalhes.nomeCientifico.isNotEmpty()) {
                            addInfoChip(detalhes.nomeCientifico, R.color.text_secondary)
                        }
                        
                        // Família
                        if (detalhes.familia.isNotEmpty()) {
                            addInfoChip(detalhes.familia, R.color.text_secondary)
                        }
                        
                        // Tipo
                        addInfoChip(detalhes.getTextoTipo(), Color.parseColor(detalhes.getCorTipo()))
                        
                        // Tamanho se disponível
                        if (detalhes.tamanho.isNotEmpty()) {
                            addInfoChip("${detalhes.tamanho}", R.color.text_hint)
                        }
                    }
                }
            }
        }

        private fun addInfoChip(text: String, colorRes: Int) {
            val chip = com.google.android.material.chip.Chip(itemView.context)
            chip.text = text
            chip.textSize = 12f
            chip.isClickable = false
            chip.isCheckable = false
            
            if (colorRes is Int && colorRes > 0xFFFFFF) {
                // É uma cor hex, usar diretamente
                chip.setTextColor(colorRes)
            } else {
                // É um resource ID
                chip.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            }
            
            binding.chipGroupInfo.addView(chip)
        }

        private fun bindInteractions(postagem: PostagemFeed) {
            binding.apply {
                // Estatísticas de interação
                textViewInteractionStats.text = postagem.interacoes.getTextoInteracoes()
                
                // Estado do botão curtir
                if (postagem.interacoes.curtidoPeloUsuario) {
                    imageViewLike.setImageResource(R.drawable.ic_favorite_24dp)
                    imageViewLike.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.like_color)
                    )
                    textViewLike.setTextColor(ContextCompat.getColor(itemView.context, R.color.like_color))
                    textViewLike.text = "Curtido"
                } else {
                    imageViewLike.setImageResource(R.drawable.ic_favorite_border_24dp)
                    imageViewLike.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                    textViewLike.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                    textViewLike.text = "Curtir"
                }
                
                // Estado do botão salvar
                if (postagem.interacoes.salvosPeloUsuario) {
                    imageViewBookmark.setImageResource(R.drawable.ic_bookmark_24dp)
                    imageViewBookmark.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.primary_green)
                    )
                    textViewBookmark.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_green))
                    textViewBookmark.text = "Salvo"
                } else {
                    imageViewBookmark.setImageResource(R.drawable.ic_bookmark_border_24dp)
                    imageViewBookmark.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                    textViewBookmark.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                    textViewBookmark.text = "Salvar"
                }
            }
        }

        private fun setupClickListeners(postagem: PostagemFeed) {
            binding.apply {
                // Clique no card (conteúdo principal)
                root.setOnClickListener { onCardClick(postagem) }
                
                // Clique nas informações do usuário
                layoutUserHeader.setOnClickListener { onUserClick(postagem.usuario) }
                imageViewUserAvatar.setOnClickListener { onUserClick(postagem.usuario) }
                
                // Botões de ação
                buttonLike.setOnClickListener { 
                    onLikeClick(postagem)
                    // Animação de clique
                    it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction { 
                            it.animate().scaleX(1f).scaleY(1f).setDuration(100)
                        }
                }
                
                buttonComment.setOnClickListener { onCommentClick(postagem) }
                buttonShare.setOnClickListener { onShareClick(postagem) }
                buttonBookmark.setOnClickListener { 
                    onBookmarkClick(postagem)
                    // Animação de clique
                    it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction { 
                            it.animate().scaleX(1f).scaleY(1f).setDuration(100)
                        }
                }
                
                // Clique na imagem para visualização expandida
                imageViewPostPhoto.setOnClickListener { 
                    // TODO: Implementar visualização em tela cheia
                    onCardClick(postagem)
                }
            }
        }
    }

    class PostagemDiffCallback : DiffUtil.ItemCallback<PostagemFeed>() {
        override fun areItemsTheSame(oldItem: PostagemFeed, newItem: PostagemFeed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostagemFeed, newItem: PostagemFeed): Boolean {
            return oldItem == newItem
        }
    }
}