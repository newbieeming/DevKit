package com.newbieeming.devkit.feature.stopwatch

import com.newbieeming.devkit.core.ui.FeatureEntry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StopwatchEntryModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideStopwatchEntry(): FeatureEntry = StopwatchEntry()
}
