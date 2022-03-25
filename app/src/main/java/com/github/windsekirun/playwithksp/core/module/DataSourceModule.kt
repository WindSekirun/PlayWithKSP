package com.github.windsekirun.playwithksp.core.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    fun provideSharedPreference(application: Application): SharedPreferences {
        return application.getSharedPreferences(
            "com.github.windsekirun.playwithksp.app_preferences",
            Context.MODE_PRIVATE
        )
    }
}