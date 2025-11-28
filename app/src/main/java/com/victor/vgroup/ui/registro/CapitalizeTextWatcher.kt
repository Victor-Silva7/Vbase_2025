package com.victor.vgroup.ui.registro

import android.text.TextWatcher

/**
 * Text watcher for automatic title case capitalization
 * Capitalizes the first letter of each word while preserving spacing
 */
class CapitalizeTextWatcher : TextWatcher {
    private var isUpdating = false
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    
    override fun afterTextChanged(s: android.text.Editable?) {
        if (isUpdating || s == null) return
        
        isUpdating = true
        
        val text = s.toString()
        val capitalizedText = capitalizeWords(text)
        
        if (text != capitalizedText) {
            s.replace(0, s.length, capitalizedText)
        }
        
        isUpdating = false
    }
    
    private fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.lowercase().replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            } else {
                word
            }
        }
    }
}