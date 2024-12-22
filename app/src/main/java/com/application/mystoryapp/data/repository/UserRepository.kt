package com.application.mystoryapp.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.application.mystoryapp.data.StoryRemoteMediator
import com.application.mystoryapp.data.database.StoryDatabase
import com.application.mystoryapp.data.database.StoryEntity
import com.application.mystoryapp.pref.UserModel
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.data.response.LoginResponse
import com.application.mystoryapp.data.response.RegisterResponse
import com.application.mystoryapp.data.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(
    val apiService: ApiService,
    val userPreferences: UserPreference,
    val storyDatabase: StoryDatabase
) {

    private val storyDao = storyDatabase.storyDao()

    suspend fun requestLogin(
        email: String,
        password: String
    ): Result<LoginResponse> {
        return try {
            // Make the login request
            val response = apiService.login(email, password)
            Log.d("LoginResponse", "Response: $response")

            if (response != null && response.loginResult != null) {
                // Successfully logged in, save the token and user details
                val userModel = UserModel(email, response.loginResult.token.toString(), true)
                userPreferences.saveSession(userModel)  // Save the session with the token

                Result.success(response)  // Return the successful result with the response
            } else {
                Result.failure(Exception("Login failed: ${response?.message ?: "Unknown error"}"))
            }
        } catch (e: Exception) {
            // Log the exception for debugging
            Log.e("LoginError", "Error during login: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun requestSignup(
        name: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.register(name, email, password).execute()
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Signup failed: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSession(): Flow<UserModel> {
        return userPreferences.getSession()
    }

    suspend fun logout() {
        userPreferences.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
            storyDatabase: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference, storyDatabase)
            }.also { instance = it }
    }

    // paging
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): Flow<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(token, apiService, storyDatabase),
            pagingSourceFactory =  { storyDatabase.storyDao().getAllStory() }
//            { storyDao.getAllStory() } or { StoryPagingSource(apiService, token) }
        ).flow
    }

    suspend fun saveStoriesToDatabase(stories: List<StoryEntity>) {
        storyDatabase.storyDao().insertStory(stories)
    }
}
