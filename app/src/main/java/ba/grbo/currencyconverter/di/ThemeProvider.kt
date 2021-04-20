package ba.grbo.currencyconverter.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.ui.fragments.SettingsFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeProvider {
    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }


    @Provides
    fun provideTheme(
        sharedPreferences: SharedPreferences,
        application: Application
    ): String {
        return sharedPreferences.getString(
            application.getString(R.string.key_theme),
            SettingsFragment.DEVICE
        ) ?: SettingsFragment.DEVICE
    }
}