package com.ifpr.androidapptemplate.ui.registro

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.databinding.ItemSelectedImageBinding

class SelectedImageAdapter(
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder>() {

    private var images = mutableListOf<Uri>()

    fun updateImages(newImages: List<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSelectedImageBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size

    inner class ImageViewHolder(
        private val binding: ItemSelectedImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUri: Uri) {
            // Load image with better error handling
            try {
                binding.imageView.setImageURI(imageUri)
                binding.loadingOverlay.visibility = android.view.View.GONE
            } catch (e: Exception) {
                // Show placeholder or error image
                binding.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                binding.loadingOverlay.visibility = android.view.View.GONE
            }
            
            binding.buttonRemove.setOnClickListener {
                // Add animation feedback
                binding.root.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(150)
                    .withEndAction {
                        onRemoveClick(imageUri)
                    }
                    .start()
            }
        }
    }
}