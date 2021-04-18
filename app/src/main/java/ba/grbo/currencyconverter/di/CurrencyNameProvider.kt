package ba.grbo.currencyconverter.di

import android.app.Application
import androidx.preference.PreferenceManager
import ba.grbo.currencyconverter.R
import ba.grbo.currencyconverter.data.models.CurrencyName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object CurrencyNameProvider {
    @Provides
    fun provideCurrencyName(application: Application): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        return sharedPreferences.getString(
                application.getString(R.string.key_currency_name),
                CurrencyName.BOTH_CODE
        ) ?: CurrencyName.BOTH_CODE
    }
}