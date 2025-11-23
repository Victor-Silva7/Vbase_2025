package com.ifpr.androidapptemplate.ui.registro

import android.content.Context
import com.ifpr.androidapptemplate.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reusable form validator for Plant and Insect registration forms
 * Centralizes all validation logic to avoid duplication
 */
class FormValidator(private val context: Context) {
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    /**
     * Validate name field
     * Returns null if valid, error message if invalid
     */
    fun validateNome(nome: String): String? {
        val trimmedNome = nome.trim()
        
        return when {
            trimmedNome.isEmpty() -> 
                context.getString(R.string.error_nome_required)
            trimmedNome.length < 2 -> 
                context.getString(R.string.error_nome_too_short)
            trimmedNome.length > 50 -> 
                context.getString(R.string.error_nome_too_long)
            !trimmedNome.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) -> 
                context.getString(R.string.error_nome_invalid_chars)
            else -> null
        }
    }
    
    /**
     * Validate date field
     * Returns null if valid, error message if invalid
     */
    fun validateData(data: String): String? {
        val trimmedData = data.trim()
        
        return when {
            trimmedData.isEmpty() -> 
                context.getString(R.string.error_data_required)
            !isValidDateFormat(trimmedData) -> 
                context.getString(R.string.error_data_invalid_format)
            isFutureDate(trimmedData) -> 
                context.getString(R.string.error_data_future)
            else -> null
        }
    }
    
    /**
     * Validate location field
     * Returns null if valid, error message if invalid
     */
    fun validateLocal(local: String): String? {
        val trimmedLocal = local.trim()
        
        return when {
            trimmedLocal.isEmpty() -> 
                context.getString(R.string.error_local_required)
            trimmedLocal.length < 2 -> 
                context.getString(R.string.error_local_too_short)
            trimmedLocal.length > 30 -> 
                context.getString(R.string.error_local_too_long)
            !trimmedLocal.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) -> 
                context.getString(R.string.error_local_invalid_chars)
            else -> null
        }
    }
    
    /**
     * Validate observation field for insect category requirements
     * Returns null if valid, error message if invalid
     */
    fun validateObservacao(observacao: String, minLength: Int = 10): String? {
        val trimmedObservacao = observacao.trim()
        
        return when {
            trimmedObservacao.isEmpty() -> "Observação é obrigatória"
            trimmedObservacao.length < minLength -> 
                "Observação muito curta (mínimo $minLength caracteres)"
            else -> null
        }
    }
    
    /**
     * Validate that a category is selected
     * Returns null if valid, error message if invalid
     */
    fun validateCategory(isSelected: Boolean): String? {
        return if (!isSelected) {
            context.getString(R.string.error_category_required)
        } else {
            null
        }
    }
    
    /**
     * Validate entire form at once
     * Returns true if all fields are valid
     */
    fun validateForm(
        nome: String,
        data: String,
        local: String,
        categorySelected: Boolean
    ): Boolean {
        var isValid = true
        
        if (validateNome(nome) != null) isValid = false
        if (validateData(data) != null) isValid = false
        if (validateLocal(local) != null) isValid = false
        if (validateCategory(categorySelected) != null) isValid = false
        
        return isValid
    }
    
    // Private helper methods
    
    private fun isValidDateFormat(date: String): Boolean {
        return try {
            dateFormatter.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isFutureDate(date: String): Boolean {
        return try {
            val inputDate = dateFormatter.parse(date)
            val today = Calendar.getInstance().time
            inputDate?.after(today) ?: false
        } catch (e: Exception) {
            false
        }
    }
}
