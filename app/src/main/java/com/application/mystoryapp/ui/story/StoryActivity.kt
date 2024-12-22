package com.application.mystoryapp.ui.story

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.mystoryapp.ui.maps.MapsActivity
import com.application.mystoryapp.R
import com.application.mystoryapp.databinding.ActivityStoryBinding
import com.application.mystoryapp.di.Injection
import com.application.mystoryapp.adapter.ListStoryPagingAdapter
import com.application.mystoryapp.adapter.LoadingStateAdapter
import com.application.mystoryapp.ui.addstory.AddStoryActivity
import com.application.mystoryapp.ui.login.LoginActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //check is the user login?
        storyViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else{setupRecyclerView(user.token)}
        }

        binding.FabAddStory.setOnClickListener {
            var intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView(token: String) {
        val storiesAdapter = ListStoryPagingAdapter()

        // Set up RecyclerView
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = storiesAdapter
        }

        // Collect PagingData and submit to the adapter
        lifecycleScope.launch {
            binding.rvStories.adapter = storiesAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    storiesAdapter.retry()
                }
            )
            storyViewModel.getStories(token).collectLatest { pagingData ->
                storiesAdapter.submitData(pagingData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        storyViewModel.getListStories() // Refresh data cerita
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_button -> {
                storyViewModel.logout()
                true
            }
            R.id.open_map -> {
                var intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}