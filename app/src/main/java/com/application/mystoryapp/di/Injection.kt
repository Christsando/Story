package com.application.mystoryapp.di

import android.content.Context
import com.application.mystoryapp.data.database.StoryDatabase
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.pref.dataStore
import com.application.mystoryapp.data.retrofit.ApiConfig
import com.application.mystoryapp.data.repository.UserRepository


object Injection {
    fun provideRepository(context: Context): UserRepository {
        val database = StoryDatabase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, pref, database)
    }
}