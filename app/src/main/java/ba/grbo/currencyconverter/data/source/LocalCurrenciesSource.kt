package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.database.*
import kotlinx.coroutines.flow.Flow
import java.util.*

interface LocalCurrenciesSource {
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean>

    fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>>

    suspend fun updateUnexchangeableCurrency(unexchangeableCurrency: UnexchangeableCurrency): DatabaseResult<Boolean>

    suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>>

    suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency>

    fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>>
}