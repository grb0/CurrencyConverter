package ba.grbo.currencyconverter.di

import ba.grbo.currencyconverter.data.source.local.CurrencyConverterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@InstallIn(ActivityRetainedComponent::class)
@Module
object DaosProvider {
    @Provides
    fun provideCurrencyDao(database: CurrencyConverterDatabase) = database.currencyDao

    @Provides
    fun provideExchangeRateDao(database: CurrencyConverterDatabase) = database.exchangeRateDao
}