package ba.grbo.currencyconverter.data.source

import ba.grbo.currencyconverter.data.models.database.*
import kotlinx.coroutines.flow.Flow
import java.util.*

interface LocalCurrenciesSource {
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean>

    fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>>

    suspend fun updateMiscellaneous(miscellaneous: Miscellaneous): DatabaseResult<Boolean>

    suspend fun getMiscellaneous(): DatabaseResult<Miscellaneous>

    fun observeMiscellaneous(): DatabaseResult<Flow<Miscellaneous>>

    suspend fun updateUnexchangeableCurrency(currency: UnexchangeableCurrency): DatabaseResult<Boolean>

    suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>>

    suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency>

    fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>>
}