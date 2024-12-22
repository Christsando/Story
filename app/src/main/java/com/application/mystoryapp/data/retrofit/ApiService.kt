package com.application.mystoryapp.data.retrofit

import com.application.mystoryapp.data.response.AddStoryResponse
import com.application.mystoryapp.data.response.AllStoryResponse
import com.application.mystoryapp.data.response.DetailStoryResponse
import com.application.mystoryapp.data.response.LoginResponse
import com.application.mystoryapp.data.response.RegisterResponse
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @Multipart
    @POST ("stories")
    suspend fun addStories(
        @Header("Authorization") authHeader: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: Float? = null,
        @Part("lon") lon: Float? = null
    ): AddStoryResponse

    @GET("stories/{id}")
    suspend fun getDetailStory(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String
    ): DetailStoryResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int? = null,  // Optional parameter
        @Query("size") size: Int? = null,  // Optional parameter
        @Query("location") location: Int? = 0 // 0 without location 1 with location
    ): AllStoryResponse
}