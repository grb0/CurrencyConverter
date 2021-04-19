package ba.grbo.currencyconverter.di

import android.app.Application
import androidx.preference.PreferenceManager
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
    @ba.grbo.currencyconverter.di.CurrencyUiName
    @Provides
    fun provideUiName(application: Application): Currency.UiName {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        val uiName = sharedPreferences.getString(
            application.getString(R.string.key_currency_name),
            Currency.UiName.CODE_AND_NAME.name
        ) ?: Currency.UiName.CODE_AND_NAME.name

        return Currency.UiName.valueOf(uiName)
    }

    @ba.grbo.currencyconverter.di.FilterBy
    @Provides
    fun provideFilterBy(application: Application): FilterBy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        val filterBy = sharedPreferences.getString(
            application.getString(R.string.key_filter_by),
            FilterBy.BOTH.name
        ) ?: FilterBy.BOTH.name

        return FilterBy.valueOf(filterBy)
    }
}