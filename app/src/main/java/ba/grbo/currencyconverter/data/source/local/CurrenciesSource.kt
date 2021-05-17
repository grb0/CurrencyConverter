package ba.grbo.currencyconverter.data.source.local

import ba.grbo.currencyconverter.data.models.database.*
import ba.grbo.currencyconverter.data.source.DatabaseResult
import ba.grbo.currencyconverter.data.source.Error
import ba.grbo.currencyconverter.data.source.LocalCurrenciesSource
import ba.grbo.currencyconverter.data.source.Success
import ba.grbo.currencyconverter.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class CurrenciesSource @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val miscellaneousDao: MiscellaneousDao,
    private val unexchangeableCurrencyDao: UnexchangeableCurrencyDao,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher
) : LocalCurrenciesSource {
    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                Success(exchangeRateDao.insert(exchangeRate) == exchangeRate.id)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>> =
        try {
            Success(exchangeRateDao.observeAllMostRecent())
        } catch (e: Exception) {
            Error(e)
        }

    override suspend fun updateMiscellaneous(miscellaneous: Miscellaneous): DatabaseResult<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                Success(miscellaneousDao.update(miscellaneous) != 0)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getMiscellaneous(): DatabaseResult<Miscellaneous> {
        return withContext(coroutineDispatcher) {
            try {
                Success(miscellaneousDao.get())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override fun observeMiscellaneous(): DatabaseResult<Flow<Miscellaneous>> {
        return try {
            Success(miscellaneousDao.observe())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun updateUnexchangeableCurrency(
        currency: UnexchangeableCurrency
    ): DatabaseResult<Boolean> = withContext(coroutineDispatcher) {
        try {
            Success(unexchangeableCurrencyDao.update(currency) != 0)
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>> {
        return withContext(coroutineDispatcher) {
            try {
                Success(unexchangeableCurrencyDao.getExchangeableCurrencies())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency> = withContext(coroutineDispatcher) {
        try {
            Success(unexchangeableCurrencyDao.getMultiExchangeableCurrency(code, fromDate, toDate))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>> {
        return try {
            Success(unexchangeableCurrencyDao.observeIsFavorite())
        } catch (e: Exception) {
            Error(e)
        }
    }
}