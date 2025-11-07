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
        value = "Bem-vindo ao VBase"
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

    fun loadUserStats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens").child(userId)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var plantas = 0
                var insetos = 0

                for (itemSnapshot in snapshot.children) {
                    val tipo = itemSnapshot.child("tipo").getValue(String::class.java)
                    when (tipo) {
                        "planta" -> plantas++
                        "inseto" -> insetos++
                    }
                }

                _plantCount.value = plantas.toString()
                _insectCount.value = insetos.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Erro ao carregar estatísticas
                _plantCount.value = "0"
                _insectCount.value = "0"
            }
        })
    }
}