package com.victor.vgroup.ui.perfil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victor.vgroup.data.model.Notificacao
import com.victor.vgroup.data.model.getIconeResId
import com.victor.vgroup.data.model.getMensagem
import com.victor.vgroup.data.model.getTempoDecorrido
import com.victor.vgroup.databinding.ItemNotificacaoBinding

class NotificacoesAdapter(
    private val onNotificacaoClick: (Notificacao) -> Unit
) : RecyclerView.Adapter<NotificacoesAdapter.NotificacaoViewHolder>() {

    private val notificacoes = mutableListOf<Notificacao>()

    fun submitList(novaLista: List<Notificacao>) {
        notificacoes.clear()
        notificacoes.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val binding = ItemNotificacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificacaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        holder.bind(notificacoes[position])
    }

    override fun getItemCount(): Int = notificacoes.size

    inner class NotificacaoViewHolder(
        private val binding: ItemNotificacaoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notificacao: Notificacao) {
            binding.imageIcone.setImageResource(notificacao.getIconeResId())
            binding.textMensagem.text = notificacao.getMensagem()
            binding.textTempo.text = notificacao.getTempoDecorrido()

            binding.root.setOnClickListener {
                onNotificacaoClick(notificacao)
            }
        }
    }
}
