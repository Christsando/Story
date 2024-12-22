package com.application.mystoryapp.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.mystoryapp.data.repository.UserRepository
import com.application.mystoryapp.data.response.RegisterResponse
import kotlinx.coroutines.launch

class SignupViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _signupResult = MutableLiveData<Result<RegisterResponse>>()
    val signupResult: LiveData<Result<RegisterResponse>> get() = _signupResult

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.requestSignup(name, email, password)
            _signupResult.postValue(result)
        }
    }
}