package ba.grbo.currencyconverter.di

import android.app.Application
import android.content.SharedPreferences
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.models.FilterBy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object PreferencesProvider {
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