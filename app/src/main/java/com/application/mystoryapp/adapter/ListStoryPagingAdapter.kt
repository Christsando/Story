package com.application.mystoryapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.application.mystoryapp.data.database.StoryEntity
import com.application.mystoryapp.databinding.StoryListBinding
import com.application.mystoryapp.ui.detailstory.StoryDetail
import com.bumptech.glide.Glide

class ListStoryPagingAdapter: PagingDataAdapter <StoryEntity, ListStoryPagingAdapter.ListViewHolder>(DIFF_CALLBACK) {

    class ListViewHolder(val binding: StoryListBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(stories : StoryEntity){
            Glide.with(binding.ivItem.context)
                .load(stories.photoUrl)
                .into(binding.ivItem)
            binding.tvTitle.text = stories.name
            binding.tvDescription.text = stories.description
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ListViewHolder {
        val binding = StoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null){
            holder.bind(story)
        }

        // set on click to detail story
        holder.itemView.setOnClickListener(){
            val context = holder.itemView.context
            val intentDetail = Intent(context, StoryDetail::class.java)

            // using story id to pass data
            intentDetail.putExtra(StoryDetail.EXTRA_STORY_ID, story?.id)

            // Add transition animation
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                context as Activity,
                androidx.core.util.Pair(holder.binding.ivItem, "photo_transition"),
                androidx.core.util.Pair(holder.binding.tvTitle, "title_transition")
            )

            context.startActivity(intentDetail, options.toBundle())
        }
    }

    companion object{
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}