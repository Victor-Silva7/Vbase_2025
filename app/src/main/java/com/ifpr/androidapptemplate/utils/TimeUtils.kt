package com.ifpr.androidapptemplate.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitário centralizado para formatação de tempo e datas
 * Elimina duplicação de código em PostagemModels, ComentarioModels, etc.
 */
object TimeUtils {
    
    /**
     * Formata timestamp em tempo relativo (ex: "5min", "2h", "3d")
     * 
     * @param timestamp Timestamp em milissegundos
     * @return String formatada com tempo relativo
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        
        return when {
            seconds < 60 -> "Agora"
            minutes < 60 -> "${minutes}min"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            days < 30 -> "${weeks}sem"
            else -> "${months}m"
        }
    }
    
    /**
     * Formata timestamp em data completa (ex: "15/11/2025 14:30")
     * 
     * @param timestamp Timestamp em milissegundos
     * @param pattern Padrão de formatação (padrão: "dd/MM/yyyy HH:mm")
     * @return String formatada com data completa
     */
    fun getFormattedDateTime(timestamp: Long, pattern: String = "dd/MM/yyyy HH:mm"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Formata timestamp apenas com data (ex: "15/11/2025")
     * 
     * @param timestamp Timestamp em milissegundos
     * @return String formatada apenas com data
     */
    fun getFormattedDate(timestamp: Long): String {
        return getFormattedDateTime(timestamp, "dd/MM/yyyy")
    }
    
    /**
     * Formata timestamp apenas com hora (ex: "14:30")
     * 
     * @param timestamp Timestamp em milissegundos
     * @return String formatada apenas com hora
     */
    fun getFormattedTime(timestamp: Long): String {
        return getFormattedDateTime(timestamp, "HH:mm")
    }
    
    /**
     * Verifica se um timestamp é recente (menos de 7 dias)
     * 
     * @param timestamp Timestamp em milissegundos
     * @return true se é recente, false caso contrário
     */
    fun isRecent(timestamp: Long): Boolean {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return timestamp > sevenDaysAgo
    }
    
    /**
     * Calcula o score de "frescor" baseado na idade do timestamp
     * Usado para cálculos de relevância
     * 
     * @param timestamp Timestamp em milissegundos
     * @return Score de frescor (1.2f se recente, 1.0f caso contrário)
     */
    fun getFreshnessScore(timestamp: Long): Float {
        return if (isRecent(timestamp)) 1.2f else 1.0f
    }
}
