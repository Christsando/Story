package com.application.mystoryapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.mystoryapp.pref.UserModel
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.data.repository.UserRepository
import com.application.mystoryapp.pref.dataStore
import com.application.mystoryapp.databinding.ActivityLoginBinding
import com.application.mystoryapp.di.Injection
import com.application.mystoryapp.ui.signup.SignupActivity
import com.application.mystoryapp.ui.story.StoryActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var userRepository: UserRepository
    lateinit var userPreferences: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize userRepository and userPreferences using Injection
        userRepository = Injection.provideRepository(this)
        userPreferences = UserPreference.getInstance(applicationContext.dataStore)

        // hide the action bar
        supportActionBar?.hide()

        moveToRegister()
        binding.buttonLogin.setOnClickListener {
            handleLogin()
        }
    }

    fun moveToRegister(){
        binding.tvLinkRegist.setOnClickListener(){
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    fun handleLogin(){
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Validate inputs and show errors if needed
        when {
            email.isEmpty() -> {
                binding.emailEditText.error = "Email cannot be empty"
                return
            }
            password.isEmpty() -> {
                binding.passwordEditText.error = "Password cannot be empty"
                return
            }
        }

        lifecycleScope.launch {
            try {
                val result = userRepository.requestLogin(email, password)
                result.onSuccess { loginResponse ->
                    loginResponse.loginResult?.token?.let { token ->
                        // Save the token in preferences
                        val userModel = UserModel(email, token, true)
                        userPreferences.saveSession(userModel)
                    }
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }.onFailure {
                    val errorMessage = it.message ?: "Login failed"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, StoryActivity::class.java)
        startActivity(intent)
        finish()
    }
}