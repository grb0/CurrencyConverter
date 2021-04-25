package ba.grbo.currencyconverter.di

import android.app.Activity
import ba.grbo.currencyconverter.data.models.managers.Colors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object ColorsProvider {
    @ActivityScoped
    @Provides
    fun provideColorManager(activity: Activity) = Colors(activity)
}