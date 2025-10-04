package com.ifpr.androidapptemplate.ui.registro

import com.ifpr.androidapptemplate.data.model.Planta
import com.ifpr.androidapptemplate.data.model.Inseto

/**
 * Sealed class para representar diferentes tipos de registros
 * Permite que o adapter trabalhe com plantas e insetos de forma unificada
 */
sealed class RegistrationItem {
    
    /**
     * Item de planta
     */
    data class PlantItem(val planta: Planta) : RegistrationItem() {
        val id: String get() = planta.id
        val name: String get() = planta.nome
        val date: String get() = planta.data
        val location: String get() = planta.local
        val images: List<String> get() = planta.imagens
        val observation: String get() = planta.observacao
        val timestamp: Long get() = planta.timestamp
        val type: String get() = "PLANTA"
    }
    
    /**
     * Item de inseto
     */
    data class InsectItem(val inseto: Inseto) : RegistrationItem() {
        val id: String get() = inseto.id
        val name: String get() = inseto.nome
        val date: String get() = inseto.data
        val location: String get() = inseto.local
        val images: List<String> get() = inseto.imagens
        val observation: String get() = inseto.observacao
        val timestamp: Long get() = inseto.timestamp
        val type: String get() = "INSETO"
    }
    
    /**
     * Propriedades comuns para facilitar o uso no adapter
     */
    val commonId: String
        get() = when (this) {
            is PlantItem -> id
            is InsectItem -> id
        }
    
    val commonName: String
        get() = when (this) {
            is PlantItem -> name
            is InsectItem -> name
        }
    
    val commonDate: String
        get() = when (this) {
            is PlantItem -> date
            is InsectItem -> date
        }
    
    val commonLocation: String
        get() = when (this) {
            is PlantItem -> location
            is InsectItem -> location
        }
    
    val commonImages: List<String>
        get() = when (this) {
            is PlantItem -> images
            is InsectItem -> images
        }
    
    val commonObservation: String
        get() = when (this) {
            is PlantItem -> observation
            is InsectItem -> observation
        }
    
    val commonTimestamp: Long
        get() = when (this) {
            is PlantItem -> timestamp
            is InsectItem -> timestamp
        }
    
    val commonType: String
        get() = when (this) {
            is PlantItem -> type
            is InsectItem -> type
        }
}