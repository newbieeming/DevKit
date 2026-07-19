package com.newbieeming.devkit.feature.timesync

import com.newbieeming.devkit.core.ui.FeatureEntry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeSyncEntryModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideTimeSyncEntry(): FeatureEntry = TimeSyncEntry()
}
