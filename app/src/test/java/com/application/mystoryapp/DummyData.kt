package com.application.mystoryapp

import com.application.mystoryapp.data.response.AddStoryResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object DummyData {
    fun generateDummyImageMultipart(): MultipartBody.Part {
        val dummyImage = "dummy_image_data".toByteArray()
        return MultipartBody.Part.createFormData(
            "photo",
            "dummy.jpg",
            dummyImage.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
    }

    fun generateDummyDescription(): RequestBody {
        return "Test description".toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun generateDummyAddStoryResponse(isError: Boolean = false): AddStoryResponse {
        return AddStoryResponse(
            error = isError,
            message = if (isError) "Upload failed" else "Story created successfully"
        )
    }
}