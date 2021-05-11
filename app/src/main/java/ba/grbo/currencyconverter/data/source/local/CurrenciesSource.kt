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
    private val currencyDao: CurrencyDao,
    @DispatcherIO private val coroutineDispatcher: CoroutineDispatcher
) : LocalCurrenciesSource {
    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                exchangeRateDao.insert(exchangeRate)
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>> =
        try {
            Success(exchangeRateDao.observeMostRecentExchangeRates())
        } catch (e: Exception) {
            Error(e)
        }

    override suspend fun updateMiscellaneous(miscellaneous: Miscellaneous): DatabaseResult<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                miscellaneousDao.update(miscellaneous)
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getMiscellaneous(): DatabaseResult<Miscellaneous> {
        return withContext(coroutineDispatcher) {
            try {
                Success(miscellaneousDao.getMiscellaneous())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun updateUnexchangeableCurrency(
        unexchangeableCurrency: UnexchangeableCurrency
    ): DatabaseResult<Boolean> = withContext(coroutineDispatcher) {
        try {
            currencyDao.update(unexchangeableCurrency)
            Success(true)
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>> {
        return withContext(coroutineDispatcher) {
            try {
                Success(currencyDao.getExchangeableCurrencies())
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
            Success(currencyDao.getMultiExchangeableCurrency(code, fromDate, toDate))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>> {
        return try {
            Success(currencyDao.observeAreUnexchangeableCurrenciesFavorite())
        } catch (e: Exception) {
            Error(e)
        }
    }
}