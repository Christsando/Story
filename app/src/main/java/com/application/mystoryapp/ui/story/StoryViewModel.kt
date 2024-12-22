package com.application.mystoryapp.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.application.mystoryapp.data.database.StoryEntity
import com.application.mystoryapp.pref.UserModel
import com.application.mystoryapp.data.repository.UserRepository
import com.application.mystoryapp.data.response.ListStoryItem
import com.application.mystoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StoryViewModel (private val repository: UserRepository): ViewModel() {

    // data live for stories list
    private val _storiesList = MutableLiveData<List<ListStoryItem>>()
    val storiesList: LiveData<List<ListStoryItem>> = _storiesList

    init{
        getListStories()
    }

    fun getStories(token: String): Flow<PagingData<StoryEntity>> {
        return repository.getStories(token).cachedIn(viewModelScope)
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getListStories(){
        viewModelScope.launch {
            try {
                // Collect the session data from Flow
                repository.getSession().collect { session ->

                    // Ensure session is not null
                    val token = "Bearer ${session.token}"

                    if (session.isLogin) {
                        val page = 1  // Example page
                        val location = 0 // 1 for stories with location, 0 for stories without location

                        // Call the API asynchronously
                        val response = ApiConfig.getApiService().getStories(token, page, Int.MAX_VALUE, location)
                        println("API Response: ${response.listStory}")

                        // Handle successful response
                        response.error?.let {
                            if (!it) {
                                _storiesList.value = response.listStory
                                println("Fetched stories: ${response.listStory}")
                            } else {
                                println("Error: ${response.message}")
                            }
                        }
                    } else {
                        println("User is not logged in, cannot fetch stories.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Handle errors
            }
        }
    }
}