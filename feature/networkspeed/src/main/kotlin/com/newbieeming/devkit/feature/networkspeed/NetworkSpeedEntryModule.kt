package com.newbieeming.devkit.feature.networkspeed

import com.newbieeming.devkit.core.ui.FeatureEntry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkSpeedEntryModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideNetworkSpeedEntry(): FeatureEntry = NetworkSpeedEntry()
}
