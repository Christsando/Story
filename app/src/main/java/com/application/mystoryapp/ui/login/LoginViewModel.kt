package com.application.mystoryapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.mystoryapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository): ViewModel(){

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.requestLogin(email, password)
            if (result.isSuccess) {
                // todo is success change to StoryActivity
            } else {
                // todo Handle login failure
            }
        }
    }
}