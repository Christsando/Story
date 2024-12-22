package com.application.mystoryapp.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.application.mystoryapp.DummyData
import com.application.mystoryapp.MainDispatcherRule
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.ui.addstory.AddStoryViewModel
import com.application.mystoryapp.data.retrofit.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddStoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userPreference: UserPreference

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var addStoryViewModel: AddStoryViewModel

    @Before
    fun setUp() {
        addStoryViewModel = AddStoryViewModel(userPreference)
    }

    @Test
    fun `when Upload Story Should Return Success`() = runTest {
        // Get dummy data
        val description = DummyData.generateDummyDescription()
        val dummyImage = DummyData.generateDummyImageMultipart()
        val expectedResponse = DummyData.generateDummyAddStoryResponse()
        val token = "Bearer dummy-token"

        // Mock API response
        `when`(apiService.addStories(token, description, dummyImage, null, null))
            .thenReturn(expectedResponse)

        // Test the upload
        addStoryViewModel.uploadStory(token, description, dummyImage)

        val actualResponse = addStoryViewModel.uploadResult.getOrAwaitValue()

        assertTrue(actualResponse.isSuccess)
        actualResponse.onSuccess { response ->
            assertFalse(response.error)
            assertEquals(expectedResponse.message, response.message)
        }
    }

    @Test
    fun `when Upload Story Should Return Error`() = runTest {
        // Get dummy data
        val description = DummyData.generateDummyDescription()
        val dummyImage = DummyData.generateDummyImageMultipart()
        val expectedResponse = DummyData.generateDummyAddStoryResponse(isError = true)
        val token = "Bearer dummy-token"

        // Mock API error response
        `when`(apiService.addStories(token, description, dummyImage, null, null))
            .thenReturn(expectedResponse)

        // Test the upload
        addStoryViewModel.uploadStory(token, description, dummyImage)

        val actualResponse = addStoryViewModel.uploadResult.getOrAwaitValue()

        assertTrue(actualResponse.isFailure)
        actualResponse.onFailure { exception ->
            assertEquals(expectedResponse.message, exception.message)
        }
    }

    @Test
    fun `when Upload Story with Location Should Return Success`() = runTest {
        // Get dummy data
        val description = DummyData.generateDummyDescription()
        val dummyImage = DummyData.generateDummyImageMultipart()
        val expectedResponse = DummyData.generateDummyAddStoryResponse()
        val token = "Bearer dummy-token"
        val lat = 0.0f
        val lon = 0.0f

        // Mock API response
        `when`(apiService.addStories(token, description, dummyImage, lat, lon))
            .thenReturn(expectedResponse)

        // Test the upload with location
        addStoryViewModel.uploadStory(token, description, dummyImage, lat, lon)

        val actualResponse = addStoryViewModel.uploadResult.getOrAwaitValue()

        assertTrue(actualResponse.isSuccess)
        actualResponse.onSuccess { response ->
            assertFalse(response.error)
            assertEquals(expectedResponse.message, response.message)
        }
    }

    // LiveData testing utility
    private fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }

        this.observeForever(observer)

        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}