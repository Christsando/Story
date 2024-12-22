package com.application.mystoryapp.ui.detailstory

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.pref.dataStore
import com.application.mystoryapp.databinding.ActivityStoryDetailBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class StoryDetail : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding
    private val viewModel: StoryDetailViewModel by viewModels()
    private lateinit var userPreferences: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable transitions
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)

        // create back button on actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize UserPreference
        userPreferences = UserPreference.getInstance(applicationContext.dataStore)

        // Get the story ID from intent
        val storyId = intent.getStringExtra(EXTRA_STORY_ID) ?: run {
            Toast.makeText(this, "Story ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve the auth token and fetch story details
        lifecycleScope.launch {
            userPreferences.getSession().collect { userModel ->
                val authToken = userModel.token
                if (authToken.isNotEmpty()) {
                    viewModel.getDetailStory(authToken, storyId)
                } else {
                    Toast.makeText(this@StoryDetail, "Authentication token missing", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.detailStory.observe(this) { story ->
            binding.apply {
                tvDate.text = story.createdAt
                tvUsername.text = story.name
                tvDescription.text = story.description
                Glide.with(this@StoryDetail)
                    .load(story.photoUrl)
                    .into(ivUserPost)
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Navigate back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_STORY_ID = "STORY_ID"
    }
}