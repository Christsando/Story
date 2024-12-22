package com.application.mystoryapp.ui.addstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.data.response.AddStoryResponse
import com.application.mystoryapp.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel (private val userPreference: UserPreference): ViewModel() {

    private val _uploadResult = MutableLiveData<Result<AddStoryResponse>>()
    val uploadResult: LiveData<Result<AddStoryResponse>> get() = _uploadResult

    fun uploadStory(
        token: String,
        description: RequestBody,
        body: MultipartBody.Part,
        lat: Float? = null,
        lon: Float? = null
    ) {
        viewModelScope.launch {
            try {
                val response = ApiConfig.getApiService().addStories(token, description, body, lat, lon)
                if (response.error == false) {
                    _uploadResult.postValue(Result.success(response))
                } else {
                    _uploadResult.postValue(Result.failure(Exception(response.message)))
                }
            } catch (e: Exception) {
                _uploadResult.postValue(Result.failure(e))
            }
        }
    }
}