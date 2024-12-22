package com.application.mystoryapp.ui.detailstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.mystoryapp.data.response.DetailStoryResponse
import com.application.mystoryapp.data.response.Story
import com.application.mystoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.launch

class StoryDetailViewModel : ViewModel() {

    // create live data for story detail
    private val _detailStory = MutableLiveData<Story>()
    val detailStory : LiveData<Story> = _detailStory

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getDetailStory(authToken: String, storyId: String) {
        viewModelScope.launch {
            try {
                val response: DetailStoryResponse = ApiConfig.getApiService().getDetailStory("Bearer $authToken", storyId)
                response.error?.let {
                    if (!it) {
                        _detailStory.value = response.story!!
                    } else {
                        _errorMessage.value = response.message!!
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }
}