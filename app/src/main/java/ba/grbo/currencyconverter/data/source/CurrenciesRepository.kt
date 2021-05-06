package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.db.Currency
import ba.grbo.currencyconverter.data.models.db.CurrencyWithExchangeRates
import ba.grbo.currencyconverter.data.models.db.CurrencyWithMostRecentExchangeRate
import ba.grbo.currencyconverter.data.models.db.ExchangeRate
import kotlinx.coroutines.flow.Flow
import java.util.*

interface CurrenciesRepository {
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): Result<Boolean>

    suspend fun updateCurrency(currency: Currency): Result<Boolean>

    suspend fun getCurrencyWithCustomExchangeRatesRange(
        code: ba.grbo.currencyconverter.data.models.domain.Currency.Code,
        fromDate: Date,
        toDate: Date
    ): Result<CurrencyWithExchangeRates>

    suspend fun getCurrenciesWithMostRecentExchangeRate(): Result<List<CurrencyWithMostRecentExchangeRate>>

    fun observeIsFavorite(): Result<Flow<List<Boolean>>>
}