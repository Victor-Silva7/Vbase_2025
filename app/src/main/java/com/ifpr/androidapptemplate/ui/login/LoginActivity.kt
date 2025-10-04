package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.usuario.CadastroUsuarioActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d(TAG, "onCreate iniciado")

        try {
            // Inicializa Firebase
            FirebaseApp.initializeApp(this)
            firebaseAuth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase inicializado com sucesso")

            // Inicializa views
            initViews()
            
            // Configura Google Sign-In
            setupGoogleSignIn()
            
            // Configura listeners
            setupListeners()
            
            Log.d(TAG, "LoginActivity configurado com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar LoginActivity", e)
            Toast.makeText(this, "Erro ao inicializar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        
        Log.d(TAG, "Views inicializadas")
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Setup do ActivityResultLauncher para Google Sign-In (API moderna)
            googleSignInLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                handleGoogleSignInResult(result.data)
            }
            
            Log.d(TAG, "Google Sign-In configurado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar Google Sign-In", e)
            Toast.makeText(this, "Erro na configuração do Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        registerLink.setOnClickListener {
            Log.d(TAG, "Botão cadastro clicado")
            val intent = Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            Log.d(TAG, "Botão login clicado")
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d(TAG, "Tentando login com email: $email")
            signInWithEmail(email, password)
        }

        btnGoogleSignIn.setOnClickListener {
            Log.d(TAG, "Botão Google Sign-In clicado")
            signInWithGoogle()
        }
        
        Log.d(TAG, "Listeners configurados")
    }

    private fun signInWithEmail(email: String, password: String) {
        Log.d(TAG, "Iniciando autenticação Firebase")
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                Log.d(TAG, "Resultado da autenticação recebido")
                
                if (task.isSuccessful) {
                    Log.d(TAG, "Login com email bem-sucedido")
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "Falha no login com email", task.exception)
                    Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro na autenticação Firebase", exception)
                Toast.makeText(this, "Erro: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar Google Sign-In", e)
            Toast.makeText(this, "Erro ao fazer login com Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Falha no Google Sign-In", e)
            Toast.makeText(this, "Falha no login com Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) {
            Log.w(TAG, "Conta do Google é null")
            return
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login com Google bem-sucedido")
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "Falha na autenticação com Firebase", task.exception)
                    Toast.makeText(this, "Falha na autenticação", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d(TAG, "Atualizando UI, usuário: ${user?.email}")
        
        if (user != null) {
            Log.d(TAG, "Navegando para MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d(TAG, "Usuário não logado")
        }
    }
}