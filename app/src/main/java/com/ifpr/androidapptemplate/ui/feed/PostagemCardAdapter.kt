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
import com.ifpr.androidapptemplate.databinding.ItemLoadingPaginationBinding
import com.ifpr.androidapptemplate.data.model.*

/**
 * Adapter para cards de postagem com suporte a scroll infinito
 * Suporta plantas e insetos com diferentes layouts adaptativos e item de loading
 */
class PostagemCardAdapter(
    private val onCardClick: (PostagemFeed) -> Unit,
    private val onUserClick: (UsuarioPostagem) -> Unit,
    private val onLikeClick: (PostagemFeed) -> Unit,
    private val onCommentClick: (PostagemFeed) -> Unit,
    private val onShareClick: (PostagemFeed) -> Unit,
    private val onBookmarkClick: (PostagemFeed) -> Unit,
    private val onLoadMore: () -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(AdapterDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_POSTAGEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }
    
    private var isLoadingVisible = false

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostagemFeed -> VIEW_TYPE_POSTAGEM
            is LoadingItem -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_POSTAGEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POSTAGEM -> {
                val binding = ItemPostagemCardBinding.inflate(
                    LayoutInflater.from(parent.context), 
                    parent, 
                    false
                )
                PostagemViewHolder(binding)
            }
            VIEW_TYPE_LOADING -> {
                val binding = ItemLoadingPaginationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PostagemViewHolder -> {
                val postagem = getItem(position) as PostagemFeed
                holder.bind(postagem)
                
                // Trigger load more quando próximo do final
                if (position >= itemCount - 3 && !isLoadingVisible) {
                    onLoadMore()
                }
            }
            is LoadingViewHolder -> {
                // Loading item não precisa de binding específico
            }
        }
    }
    
    /**
     * Adiciona item de loading ao final da lista
     */
    fun showLoading() {
        if (!isLoadingVisible) {
            isLoadingVisible = true
            val currentList = currentList.toMutableList()
            currentList.add(LoadingItem())
            submitList(currentList)
        }
    }
    
    /**
     * Remove item de loading da lista
     */
    fun hideLoading() {
        if (isLoadingVisible) {
            isLoadingVisible = false
            val currentList = currentList.toMutableList()
            currentList.removeAll { it is LoadingItem }
            submitList(currentList)
        }
    }
    
    /**
     * Atualiza lista com novas postagens (preserva loading se necessário)
     */
    fun updatePostagens(postagens: List<PostagemFeed>) {
        val newList = postagens.toMutableList<Any>()
        if (isLoadingVisible) {
            newList.add(LoadingItem())
        }
        submitList(newList)
    }
    
    /**
     * ViewHolder para postagens
     */
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
                
                // Contador de comentários
                // Note: Esta informação já está incluída no getTextoInteracoes()
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
    
    /**
     * ViewHolder para item de loading
     */
    inner class LoadingViewHolder(
        private val binding: ItemLoadingPaginationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // Loading item é estático, não precisa de binding
    }

    class AdapterDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is PostagemFeed && newItem is PostagemFeed -> oldItem.id == newItem.id
                oldItem is LoadingItem && newItem is LoadingItem -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is PostagemFeed && newItem is PostagemFeed -> oldItem == newItem
                oldItem is LoadingItem && newItem is LoadingItem -> oldItem == newItem
                else -> false
            }
        }
    }
}