package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.databinding.ItemSelectedImageBinding
import com.ifpr.androidapptemplate.utils.ImageUploadManager

class SelectedImageAdapter(
    private val context: Context,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder>() {

    private var images = mutableListOf<SelectedImage>()
    private val uploadManager = ImageUploadManager.getInstance()

    fun updateImages(newImages: List<Uri>) {
        val newSelectedImages = newImages.map { uri ->
            SelectedImage(
                uri = uri,
                isUploading = false,
                uploadProgress = 0,
                needsCompression = uploadManager.needsCompression(context, uri)
            )
        }
        
        val diffCallback = ImageDiffCallback(images, newSelectedImages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        images.clear()
        images.addAll(newSelectedImages)
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun updateUploadProgress(uri: Uri, progress: Int, isUploading: Boolean = true) {
        val index = images.indexOfFirst { it.uri == uri }
        if (index != -1) {
            images[index] = images[index].copy(
                isUploading = isUploading,
                uploadProgress = progress
            )
            notifyItemChanged(index)
        }
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

        fun bind(selectedImage: SelectedImage) {
            val imageUri = selectedImage.uri
            
            // Load image with Glide for better performance
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .centerCrop()
            
            Glide.with(context)
                .load(imageUri)
                .apply(requestOptions)
                .into(binding.imageView)
            
            // Show compression indicator
            if (selectedImage.needsCompression) {
                binding.compressionIndicator.visibility = android.view.View.VISIBLE
            } else {
                binding.compressionIndicator.visibility = android.view.View.GONE
            }
            
            // Show upload progress
            if (selectedImage.isUploading) {
                binding.uploadProgressBar.visibility = android.view.View.VISIBLE
                binding.uploadProgressBar.progress = selectedImage.uploadProgress
                binding.buttonRemove.isEnabled = false
                binding.buttonRemove.alpha = 0.5f
            } else {
                binding.uploadProgressBar.visibility = android.view.View.GONE
                binding.buttonRemove.isEnabled = true
                binding.buttonRemove.alpha = 1.0f
            }
            
            binding.buttonRemove.setOnClickListener {
                if (!selectedImage.isUploading) {
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
}

/**
 * Data class for selected image with upload state
 */
data class SelectedImage(
    val uri: Uri,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val needsCompression: Boolean = false
)

/**
 * DiffUtil callback for efficient RecyclerView updates
 */
class ImageDiffCallback(
    private val oldList: List<SelectedImage>,
    private val newList: List<SelectedImage>
) : DiffUtil.Callback() {
    
    override fun getOldListSize(): Int = oldList.size
    
    override fun getNewListSize(): Int = newList.size
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uri == newList[newItemPosition].uri
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}