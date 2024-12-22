package com.application.mystoryapp.pref

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)