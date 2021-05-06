package ba.grbo.currencyconverter.data.source.local.db

import ba.grbo.currencyconverter.data.models.db.Currency
import ba.grbo.currencyconverter.data.models.db.CurrencyWithExchangeRates
import ba.grbo.currencyconverter.data.models.db.CurrencyWithMostRecentExchangeRate
import ba.grbo.currencyconverter.data.models.db.ExchangeRate
import ba.grbo.currencyconverter.data.models.domain.Currency.Code
import ba.grbo.currencyconverter.data.source.LocalCurrenciesSource
import ba.grbo.currencyconverter.data.source.Result
import ba.grbo.currencyconverter.data.source.Result.Error
import ba.grbo.currencyconverter.data.source.Result.Success
import ba.grbo.currencyconverter.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class CurrenciesSource @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val currencyDao: CurrencyDao,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher
) : LocalCurrenciesSource {
    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): Result<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                exchangeRateDao.insert(exchangeRate)
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun updateCurrency(currency: Currency): Result<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                currencyDao.update(currency)
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getCurrencyWithCustomExchangeRatesRange(
        code: Code,
        fromDate: Date,
        toDate: Date
    ): Result<CurrencyWithExchangeRates> {
        return withContext(coroutineDispatcher) {
            try {
                Success(currencyDao.getCurrencyWithCustomExchangeRatesRange(code, fromDate, toDate))
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getCurrenciesWithMostRecentExchangeRate(): Result<List<CurrencyWithMostRecentExchangeRate>> {
        return withContext(coroutineDispatcher) {
            try {
                Success(currencyDao.getCurrenciesWithMostRecentExchangeRate())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override fun observeIsFavorite(): Result<Flow<List<Boolean>>> {
        return try {
            Success(currencyDao.observeIsFavorite())
        } catch (e: Exception) {
            Error(e)
        }
    }
}