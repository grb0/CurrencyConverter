package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.db.Currency
import ba.grbo.currencyconverter.data.models.db.CurrencyWithExchangeRates
import ba.grbo.currencyconverter.data.models.db.CurrencyWithMostRecentExchangeRate
import ba.grbo.currencyconverter.data.models.db.ExchangeRate
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class DefaultCurrenciesRepository @Inject constructor(
    private val localCurrenciesSource: LocalCurrenciesSource
) : CurrenciesRepository {
    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): Result<Boolean> {
        return localCurrenciesSource.insertExchangeRate(exchangeRate)
    }

    override suspend fun updateCurrency(currency: Currency): Result<Boolean> {
        return localCurrenciesSource.updateCurrency(currency)
    }

    override suspend fun getCurrencyWithCustomExchangeRatesRange(
        code: ba.grbo.currencyconverter.data.models.domain.Currency.Code,
        fromDate: Date,
        toDate: Date
    ): Result<CurrencyWithExchangeRates> {
        return localCurrenciesSource.getCurrencyWithCustomExchangeRatesRange(code, fromDate, toDate)
    }

    override suspend fun getCurrenciesWithMostRecentExchangeRate(): Result<List<CurrencyWithMostRecentExchangeRate>> {
        return localCurrenciesSource.getCurrenciesWithMostRecentExchangeRate()
    }

    override fun observeIsFavorite(): Result<Flow<List<Boolean>>> {
        return localCurrenciesSource.observeIsFavorite()
    }
}