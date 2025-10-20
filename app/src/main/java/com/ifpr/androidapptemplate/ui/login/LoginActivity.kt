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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var googleLoginButton: SignInButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        googleLoginButton = findViewById<SignInButton>(R.id.button_google_login)

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
            performGoogleLogin()
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

    private fun performGoogleLogin() {
        // Implementar login com Google
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Resultado retornado ao iniciar o Intent de login do Google
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Login bem-sucedido
            updateUI(account)
        } catch (e: ApiException) {
            // Login falhou
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            Toast.makeText(this, "Login com Google bem-sucedido! Bem-vindo, ${account.displayName}", Toast.LENGTH_LONG).show()
            // Navegar para a MainActivity após login bem-sucedido
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Falha no login com Google", Toast.LENGTH_SHORT).show()
        }
    }
}