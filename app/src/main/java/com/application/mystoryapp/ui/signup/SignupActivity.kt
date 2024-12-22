package com.application.mystoryapp.ui.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.application.mystoryapp.data.database.StoryDatabase
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.data.repository.UserRepository
import com.application.mystoryapp.pref.dataStore
import com.application.mystoryapp.data.retrofit.ApiConfig
import com.application.mystoryapp.databinding.ActivitySignupBinding
import com.application.mystoryapp.ui.login.LoginActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var binding: ActivitySignupBinding
    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up ViewBinding
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserRepository
        val apiService = ApiConfig.getApiService()
        val userPreference = UserPreference.getInstance(dataStore)
        val storyDatabase = StoryDatabase.getDatabase(this)
        userRepository = UserRepository.getInstance(apiService, userPreference, storyDatabase)

        // Initiate ViewModel for signup
        val signupViewModelFactory = SignupViewModelFactory(userRepository)
        signupViewModel = ViewModelProvider(this, signupViewModelFactory).get(SignupViewModel::class.java)

        moveToLogin()
        binding.signupButton.setOnClickListener {
            handleSignup()
        }

        signupViewModel.signupResult.observe(this) { result ->
            result.onSuccess { response ->
                Toast.makeText(this, response.message ?: "Signup successful", Toast.LENGTH_SHORT).show()

                // Redirect ke Login setelah berhasil signup
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(this, error.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
            }
        }

        // hide the action bar
        supportActionBar?.hide()
    }

    private fun moveToLogin(){
        binding.linkLogin.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleSignup() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        signupViewModel.signup(name, email, password)
    }
}