package ba.grbo.currencyconverter.di

import android.app.Application
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.CurrencyName
import ba.grbo.currencyconverter.data.models.FilterBy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent


@Module
@InstallIn(ActivityRetainedComponent::class)
object CurrencyNameProvider {
    @ba.grbo.currencyconverter.di.CurrencyName
    @Provides
    fun provideCurrencyName(application: Application): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        return sharedPreferences.getString(
                application.getString(R.string.key_currency_name),
                CurrencyName.BOTH_CODE
        ) ?: CurrencyName.BOTH_CODE
    }

    @ba.grbo.currencyconverter.di.FilterBy
    @Provides
    fun provideFilterBy(application: Application): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        return sharedPreferences.getString(
                application.getString(R.string.key_filter_by),
                FilterBy.BOTH
        ) ?: FilterBy.BOTH
    }
}