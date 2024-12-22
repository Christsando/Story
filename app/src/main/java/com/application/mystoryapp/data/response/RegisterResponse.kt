package com.application.mystoryapp.data.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

// Request body data class
data class RegisterRequest(

	//field for name user
	val name: String,

	//field for email user
	val email: String,

	//field for password user
	val password: String
)