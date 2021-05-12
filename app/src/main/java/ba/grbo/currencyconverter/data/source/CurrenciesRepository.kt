package ba.grbo.currencyconverter.data.source

import android.content.Context
import ba.grbo.currencyconverter.data.models.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency as DomainCurrency
import ba.grbo.currencyconverter.data.models.domain.Miscellaneous as DomainMiscellaneous

interface CurrenciesRepository {
    val exchangeableCurrencies: StateFlow<List<DomainCurrency>>
    val exception: MutableStateFlow<Exception?>
    var miscellaneous: DomainMiscellaneous

    fun converterDataIsReady(): Boolean

    fun exchangebleCurrenciesAreNotEmpty(): Boolean

    fun observeCurrenciesAndMiscellaneous(scope: CoroutineScope)

    fun syncExchangeableCurrenciesWithLocale(scope: CoroutineScope, context: Context)

    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean>

    fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>>

    suspend fun updateMiscellaneous(miscellaneous: Miscellaneous): Boolean

    suspend fun updateCurrency(currency: DomainCurrency): Boolean

    suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency>

    suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>>

    fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>>
}