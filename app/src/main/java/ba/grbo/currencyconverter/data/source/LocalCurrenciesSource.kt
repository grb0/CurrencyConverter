package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.database.*
import kotlinx.coroutines.flow.Flow
import java.util.*

interface LocalCurrenciesSource {
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): Result<Boolean>

    fun observeMostRecentExchangeRates(): Result<Flow<List<EssentialExchangeRate>>>

    suspend fun updateUnexchangeableCurrency(unexchangeableCurrency: UnexchangeableCurrency): Result<Boolean>

    suspend fun getExchangeableCurrencies(): Result<List<ExchangeableCurrency>>

    suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): Result<MultiExchangeableCurrency>

    fun observeAreUnexchangeableCurrenciesFavorite(): Result<Flow<List<Boolean>>>
}