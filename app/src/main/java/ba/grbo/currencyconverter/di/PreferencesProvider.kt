package ba.grbo.currencyconverter.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.preferences.FilterBy
import ba.grbo.currencyconverter.data.models.preferences.Language
import ba.grbo.currencyconverter.data.models.preferences.Theme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityScopedPreferences {
    @Provides
    fun provideUiName(
        sharedPreferences: SharedPreferences,
        application: Application,
    ): Currency.UiName {
        val uiName = sharedPreferences.getString(
            application.getString(R.string.key_ui_name),
            Currency.UiName.CODE_AND_NAME.name
        ) ?: Currency.UiName.CODE_AND_NAME.name

        return Currency.UiName.valueOf(uiName)
    }

    @Provides
    fun provideFilterBy(
        sharedPreferences: SharedPreferences,
        application: Application
    ): FilterBy {
        val filterBy = sharedPreferences.getString(
            application.getString(R.string.key_filter_by),
            FilterBy.BOTH.name
        ) ?: FilterBy.BOTH.name

        return FilterBy.valueOf(filterBy)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApplicationScopedPreferences {
    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Singleton
    @Provides
    fun provideTheme(
        sharedPreferences: SharedPreferences,
        application: Application
    ): Theme {
        val theme = sharedPreferences.getString(
            application.getString(R.string.key_theme),
            Theme.DEVICE.name
        ) ?: Theme.DEVICE.name

        return Theme.valueOf(theme)
    }

    @Provides
    fun provideLanguage(
        sharedPreferences: SharedPreferences,
        application: Application
    ): Locale {
        val language = sharedPreferences.getString(
            application.getString(R.string.key_language),
            Language.ENGLISH.name
        ) ?: Language.ENGLISH.name

        return Language.valueOf(language).toLocale()
    }
}