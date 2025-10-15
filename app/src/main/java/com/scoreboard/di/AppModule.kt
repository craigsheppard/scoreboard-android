package com.scoreboard.di

import android.content.Context
import com.scoreboard.data.local.DataStoreManager
import com.scoreboard.data.repository.ScoreboardRepository
import com.scoreboard.data.repository.ScoreboardRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideScoreboardRepository(dataStoreManager: DataStoreManager): ScoreboardRepository {
        return ScoreboardRepositoryImpl(dataStoreManager)
    }
}
