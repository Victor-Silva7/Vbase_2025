package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
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
    private lateinit var googleLoginButton: SignInButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"

    companion object {
        private const val RC_SIGN_IN_COMPANION = 9001
        private const val TAG_COMPANION = "signInWithEmail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)

        // Inicializa o Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        googleLoginButton = findViewById<SignInButton>(R.id.button_google_login)



        // Configuration do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configurar listeners para os botões
        loginButton.setOnClickListener {
            // Implementar lógica de login
            performLogin()
        }

        registerLink.setOnClickListener {
            // Implementar navegação para tela de registro
            navigateToRegister()
        }

        googleLoginButton.setOnClickListener {
            // Implementar login com Google
            signInGoogle()
        }

        // Set up the sign-in button click handler
        googleLoginButton.setOnClickListener {
            signInGoogle()
        }
    }

    private fun performLogin() {
        // Lógica de login com email e senha
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        
        // Validar campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Implementar autenticação real aqui
        // Por exemplo, chamar um serviço de autenticação
        // Para este exemplo, vamos simular um login bem-sucedido
        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
        
        // Navegar para a MainActivity após login bem-sucedido
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        // Navegar para a MainActivity e selecionar o fragmento de registro
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "registro")
        }
        startActivity(intent)
        finish()
    }



    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Login bem-sucedido, navegar para a atividade principal
            Toast.makeText(this, "Login bem-sucedido! Bem-vindo, ${user.displayName ?: user.email}", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Falha no login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Resultado retornado ao iniciar o Intent de login do Google
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Tratar falha de login
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Falha no login com Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido, navegar para a atividade principal ou atualizar UI
                    Log.d(TAG, "signInWithGoogle:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    // Tratar falha de login
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }
}