package com.ifpr.androidapptemplate.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    private val _title = MutableLiveData<String>().apply {
        value = "Início"
    }
    val title: LiveData<String> = _title

    private val _plantCount = MutableLiveData<String>().apply {
        value = "0"
    }
    val plantCount: LiveData<String> = _plantCount

    private val _insectCount = MutableLiveData<String>().apply {
        value = "0"
    }
    val insectCount: LiveData<String> = _insectCount

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUserStats()
    }

    fun loadUserStats() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Carregar contagem de plantas (caminho unificado: usuarios/plantas)
        database.getReference("usuarios/$userId/plantas")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    _plantCount.value = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tratar erro
                }
            })

        // Carregar contagem de insetos (caminho unificado: usuarios/insetos)
        database.getReference("usuarios/$userId/insetos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount
                    _insectCount.value = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tratar erro
                }
            })
    }
}