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
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .transform(CircleCrop())
                        .into(imageViewUserAvatar)
                } else {
                    imageViewUserAvatar.setImageResource(R.drawable.ic_user_placeholder)
                }
                
                // Elementos removidos do layout:
                // imageViewVerified, textViewUserLevel, textViewUserLocation
            }
        }

        private fun bindPostContent(postagem: PostagemFeed) {
            binding.apply {
                // Tempo da postagem
                textViewPostTime.text = postagem.getTempoPostagem()
                
                // imageViewPostType removido do layout
                
                // Título da postagem
                textViewPostTitle.text = postagem.titulo
                
                // Descrição da postagem
                textViewPostDescription.text = postagem.descricao
                
                // Imagem da postagem
                if (postagem.imageUrl.isNotEmpty()) {
                    imageViewPostPhoto.visibility = View.VISIBLE
                    
                    // Garantir que a imagem Base64 tem o prefixo correto
                    val imageUrl = if (!postagem.imageUrl.startsWith("data:image") && 
                                      !postagem.imageUrl.startsWith("http")) {
                        // Se não tem prefixo, adiciona
                        "data:image/jpeg;base64,${postagem.imageUrl}"
                    } else {
                        postagem.imageUrl
                    }
                    
                    android.util.Log.d("PostagemCardAdapter", "🖼️ Carregando imagem (${imageUrl.take(50)}...)")
                    try {
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_planta_24dp)
                            .error(R.drawable.ic_error_24dp)
                            .transform(RoundedCorners(24))
                            .into(imageViewPostPhoto)
                        android.util.Log.d("PostagemCardAdapter", "✅ Glide iniciou carregamento")
                    } catch (e: Exception) {
                        android.util.Log.e("PostagemCardAdapter", "❌ Erro ao carregar imagem: ${e.message}", e)
                        imageViewPostPhoto.visibility = View.GONE
                    }
                } else {
                    imageViewPostPhoto.visibility = View.GONE
                    android.util.Log.d("PostagemCardAdapter", "⚠️ Nenhuma imagem para exibir")
                }
                
                // layoutPostLocation e textViewPostLocation removidos do layout
            }
        }

        private fun bindSpecificInfo(postagem: PostagemFeed) {
            binding.chipGroupInfo.removeAllViews()
            
            when (postagem.tipo) {
                TipoPostagem.PLANTA -> {
                    postagem.detalhesPlanta?.let { detalhes ->
                        // Nome comum
                        if (detalhes.nomeComum.isNotEmpty()) {
                            addInfoChip(detalhes.nomeComum, R.color.text_secondary)
                        }
                        
                        // Status
                        addInfoChip(detalhes.getTextoStatus(), Color.parseColor(detalhes.getCorStatus()))
                    }
                }
                
                TipoPostagem.INSETO -> {
                    postagem.detalhesInseto?.let { detalhes ->
                        // Nome comum
                        if (detalhes.nomeComum.isNotEmpty()) {
                            addInfoChip(detalhes.nomeComum, R.color.text_secondary)
                        }
                        
                        // Tipo
                        addInfoChip(detalhes.getTextoTipo(), Color.parseColor(detalhes.getCorTipo()))
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
            
            // Se colorRes é maior que 0xFFFFFF, é uma cor hex; senão, é um resource ID
            if (colorRes > 0xFFFFFF) {
                chip.setTextColor(colorRes)
            } else {
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