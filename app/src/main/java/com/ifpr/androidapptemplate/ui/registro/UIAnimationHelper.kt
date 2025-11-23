package com.ifpr.androidapptemplate.ui.registro

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.ifpr.androidapptemplate.R

/**
 * Helper class for UI animations and visual feedback
 * Centralizes animation logic to avoid duplication
 */
class UIAnimationHelper(private val context: Context) {
    
    /**
     * Animate card selection with stroke and elevation changes
     */
    fun animateCardSelection(card: MaterialCardView, isSelected: Boolean) {
        card.apply {
            strokeWidth = if (isSelected) 4 else 2
            strokeColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.vgroup_green)
            } else {
                ContextCompat.getColor(context, R.color.vgroup_gray_light)
            }
            elevation = if (isSelected) 8f else 2f
            
            // Scale animation
            ObjectAnimator.ofFloat(this, "scaleX", if (isSelected) 1.05f else 1f).apply {
                duration = 200
                start()
            }
            ObjectAnimator.ofFloat(this, "scaleY", if (isSelected) 1.05f else 1f).apply {
                duration = 200
                start()
            }
        }
    }
    
    /**
     * Animate icon click with scale effect
     */
    fun animateIconClick(icon: ImageView) {
        icon.apply {
            ObjectAnimator.ofFloat(this, "scaleX", 0.8f, 1f).apply {
                duration = 150
                start()
            }
            ObjectAnimator.ofFloat(this, "scaleY", 0.8f, 1f).apply {
                duration = 150
                start()
            }
        }
    }
    
    /**
     * Show feedback toast for category selection
     */
    fun showCategorySelectionFeedback(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show error state on cards
     */
    fun showCategoryError(cards: List<MaterialCardView>, errorMessage: String) {
        cards.forEach { card ->
            card.strokeColor = ContextCompat.getColor(context, android.R.color.holo_red_light)
            card.strokeWidth = 2
        }
        
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        
        // Clear error after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            clearCategoryError(cards)
        }, 3000)
    }
    
    /**
     * Clear error state from cards
     */
    fun clearCategoryError(cards: List<MaterialCardView>) {
        cards.forEach { card ->
            if (card.strokeWidth == 2) {
                card.strokeColor = ContextCompat.getColor(context, R.color.vgroup_gray_light)
            }
        }
    }
    
    /**
     * Pulse animation for attention
     */
    fun pulseView(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0.5f, 1f).apply {
            duration = 500
            repeatCount = 2
            start()
        }
    }
    
    /**
     * Shake animation for errors
     */
    fun shakeView(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f).apply {
            duration = 500
            start()
        }
    }
}
