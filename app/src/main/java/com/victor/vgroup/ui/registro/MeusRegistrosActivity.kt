package com.victor.vgroup.ui.registro

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.victor.vgroup.R
import com.victor.vgroup.databinding.ActivityMeusRegistrosBinding

/**
 * Activity para exibir "Meus Registros"
 * Solução mais estável que navegação de fragments
 */
class MeusRegistrosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeusRegistrosBinding
    private lateinit var viewModel: MeusRegistrosViewModel
    private lateinit var registrosAdapter: RegistrosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("MeusRegistrosActivity", "🔥 onCreate INICIADO")
            
            binding = ActivityMeusRegistrosBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            Log.d("MeusRegistrosActivity", "✅ ContentView set")
            
            // Toolbar removida do layout
            
            viewModel = ViewModelProvider(this)[MeusRegistrosViewModel::class.java]
            Log.d("MeusRegistrosActivity", "✅ ViewModel OK")
            
            setupRecyclerView()
            Log.d("MeusRegistrosActivity", "✅ RecyclerView OK")
            
            setupSwipeRefresh()
            Log.d("MeusRegistrosActivity", "✅ SwipeRefresh OK")
            
            setupFilters()
            Log.d("MeusRegistrosActivity", "✅ Filters OK")
            
            setupBackButton()
            Log.d("MeusRegistrosActivity", "✅ Back Button OK")
            
            setupDebugButton()
            Log.d("MeusRegistrosActivity", "✅ Debug Button OK")
            
            observeViewModel()
            Log.d("MeusRegistrosActivity", "✅ Observers OK")
            
            Log.d("MeusRegistrosActivity", "📡 Verificando autenticação...")
            
            // Verificar se usuário está autenticado
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.e("MeusRegistrosActivity", "❌ USUÁRIO NÃO AUTENTICADO!")
                Toast.makeText(this, "Você precisa estar logado!", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            Log.d("MeusRegistrosActivity", "✅ Usuário autenticado: ${currentUser.uid}")
            Log.d("MeusRegistrosActivity", "✅ Nome: ${currentUser.displayName ?: "Sem nome"}")
            Log.d("MeusRegistrosActivity", "✅ Email: ${currentUser.email ?: "Sem email"}")
            
            Log.d("MeusRegistrosActivity", "📡 Carregando dados...")
            
            // Carregar dados
            viewModel.loadRegistrations()
            
            Log.d("MeusRegistrosActivity", "✅ TUDO OK!")
            
        } catch (e: Exception) {
            Log.e("MeusRegistrosActivity", "❌ ERRO FATAL em onCreate", e)
            e.printStackTrace()
            Toast.makeText(this, "Erro ao inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            registrosAdapter = RegistrosAdapter(
                onItemClick = { registration ->
                    // TODO: Abrir detalhes
                },
                onEditClick = { registration ->
                    // TODO: Editar
                },
                onShareClick = { registration ->
                    // TODO: Compartilhar
                }
            )

            binding.recyclerView.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = registrosAdapter
                setHasFixedSize(true)
            }
            
            Log.d("MeusRegistrosActivity", "✅ RecyclerView configurado")
        } catch (e: Exception) {
            Log.e("MeusRegistrosActivity", "❌ Erro em setupRecyclerView", e)
            throw e
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
        
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_green,
            R.color.secondary_green
        )
    }

    private fun setupBackButton() {
        // btnBack removido do layout - usuário pode usar o botão back do sistema
    }
    
    private fun setupFilters() {
        try {
            binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
                try {
                    if (checkedIds.isNotEmpty()) {
                        val filter = when (checkedIds.first()) {
                            R.id.chipAll -> FiltroCategoria.TODOS
                            R.id.chipPlants -> FiltroCategoria.PLANTAS
                            R.id.chipInsects -> FiltroCategoria.INSETOS
                            else -> FiltroCategoria.TODOS
                        }
                        viewModel.applyFilter(filter)
                    }
                } catch (e: Exception) {
                    Log.e("MeusRegistrosActivity", "Erro no listener de filtros", e)
                }
            }
            
            // Selecionar "Todos" por padrão
            binding.chipAll.isChecked = true
            
        } catch (e: Exception) {
            Log.e("MeusRegistrosActivity", "Erro em setupFilters", e)
            // Continuar sem filtros se houver erro
        }
    }
    
    private fun setupDebugButton() {
        // Botão de debug removido do layout - função desabilitada
    }

    private fun observeViewModel() {
        // Observar registros filtrados
        viewModel.filteredCombinedRegistrations.observe(this) { registrations ->
            Log.d("MeusRegistrosActivity", "📋 Recebeu registrations: ${registrations?.size ?: "null"}")
            
            if (registrations != null) {
                Log.d("MeusRegistrosActivity", "📋 Submetendo ${registrations.size} registros ao adapter")
                registrosAdapter.submitList(registrations)
                
                if (registrations.isEmpty()) {
                    Log.d("MeusRegistrosActivity", "⚠️ Lista vazia - mostrando empty state")
                    showEmptyState()
                } else {
                    Log.d("MeusRegistrosActivity", "✅ Lista com dados - escondendo empty state")
                    hideEmptyState()
                }
            } else {
                Log.d("MeusRegistrosActivity", "⚠️ Registrations é NULL - mostrando empty state")
                showEmptyState()
            }
        }

        // Observar estado de carregamento
        viewModel.isLoading.observe(this) { isLoading ->
            Log.d("MeusRegistrosActivity", "⏳ isLoading: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observar mensagens de erro
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Log.e("MeusRegistrosActivity", "❌ Error message: $errorMessage")
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.d("MeusRegistrosActivity", "📱 onResume - Recarregando dados...")
        
        // Teste direto do Firebase
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        
        if (userId != null) {
            Log.d("MeusRegistrosActivity", "🔍 Teste direto Firebase - userId: $userId")
            
            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            val plantasRef = database.reference.child("usuarios").child(userId).child("plantas")
            
            plantasRef.get().addOnSuccessListener { snapshot ->
                val count = snapshot.childrenCount
                Log.d("MeusRegistrosActivity", "🔍 Firebase TESTE: Encontradas $count plantas")
                
                // MOSTRAR NO CELULAR
                Toast.makeText(
                    this, 
                    "Firebase: $count plantas encontradas", 
                    Toast.LENGTH_LONG
                ).show()
                
                snapshot.children.forEach { child ->
                    Log.d("MeusRegistrosActivity", "🔍 Planta: ${child.key}")
                }
            }.addOnFailureListener { e ->
                Log.e("MeusRegistrosActivity", "🔍 Firebase ERRO: ${e.message}", e)
                
                // MOSTRAR ERRO NO CELULAR
                Toast.makeText(
                    this, 
                    "ERRO Firebase: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.e("MeusRegistrosActivity", "🔍 USUÁRIO NULL!")
            Toast.makeText(this, "ERRO: Usuário não autenticado!", Toast.LENGTH_LONG).show()
        }
        
        // Recarregar dados quando a tela aparece
        viewModel.loadRegistrations()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
