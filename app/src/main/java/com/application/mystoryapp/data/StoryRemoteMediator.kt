package com.application.mystoryapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.application.mystoryapp.data.database.RemoteKeys
import com.application.mystoryapp.data.database.StoryDatabase
import com.application.mystoryapp.data.database.StoryEntity
import com.application.mystoryapp.data.retrofit.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val token: String,
    private val apiService: ApiService,
    private val database: StoryDatabase
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                // If no last item, start from page 1
                lastItem?.let {
                    // Assuming API uses 1-based pagination
                    val lastRemoteKey = getLastRemoteKey(state)
                    lastRemoteKey?.nextKey ?: 1
//                    state.pages.lastOrNull()?.nextKey ?: 1
                } ?: 1
            }
        }

        return try {
            val response = apiService.getStories("Bearer $token", page, state.config.pageSize)
            val stories = response.listStory.map {
                StoryEntity(it.id, it.photoUrl, it.createdAt, it.name, it.description, it.lon, it.lat)
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.storyDao().deleteAll()
                    database.remoteKeysDao().clearRemoteKeys()
                }

                val nextKey = if (stories.size < state.config.pageSize) null else page + 1
//                val nextKey = if (stories.isEmpty()) null else page + 1

                val keys = stories.map {
                    RemoteKeys(id = it.id, prevKey = if (page == 1) null else page - 1, nextKey = nextKey)
                }

                database.remoteKeysDao().insertAll(keys)
                database.storyDao().insertStory(stories)
//                if (loadType == LoadType.REFRESH) {
//                    database.storyDao().deleteAll()
//                }
//                database.storyDao().insertStory(stories)
            }

            MediatorResult.Success(endOfPaginationReached = stories.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { story ->
                database.remoteKeysDao().getRemoteKeyById(story.id)
            }
    }
}