package com.ifpr.androidapptemplate.ui.registro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para o Fragment de Registro
 * Gerencia estado e lógica de navegação entre tipos de registro
 */
class RegistroViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Escolha o tipo de registro"
    }
    val text: LiveData<String> = _text
}
