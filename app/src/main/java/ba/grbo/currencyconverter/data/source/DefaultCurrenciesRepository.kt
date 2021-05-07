package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.database.*
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class DefaultCurrenciesRepository @Inject constructor(
    private val localCurrenciesSource: LocalCurrenciesSource
) : CurrenciesRepository {
    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): Result<Boolean> {
        return localCurrenciesSource.insertExchangeRate(exchangeRate)
    }

    override fun observeMostRecentExchangeRates(): Result<Flow<List<EssentialExchangeRate>>> {
        return localCurrenciesSource.observeMostRecentExchangeRates()
    }

    override suspend fun updateUnexchangeableCurrency(unexchangeableCurrency: UnexchangeableCurrency): Result<Boolean> {
        return localCurrenciesSource.updateUnexchangeableCurrency(unexchangeableCurrency)
    }

    override suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): Result<MultiExchangeableCurrency> {
        return localCurrenciesSource.getMultiExchangeableCurrency(code, fromDate, toDate)
    }

    override suspend fun getExchangeableCurrencies(): Result<List<ExchangeableCurrency>> {
        return localCurrenciesSource.getExchangeableCurrencies()
    }

    override fun observeAreUnexchangeableCurrenciesFavorite(): Result<Flow<List<Boolean>>> {
        return localCurrenciesSource.observeAreUnexchangeableCurrenciesFavorite()
    }
}