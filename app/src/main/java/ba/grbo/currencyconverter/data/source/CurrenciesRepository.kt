package ba.grbo.currencyconverter.data.source

import android.content.Context
import ba.grbo.currencyconverter.data.models.database.EssentialExchangeRate
import ba.grbo.currencyconverter.data.models.database.ExchangeRate
import ba.grbo.currencyconverter.data.models.database.ExchangeableCurrency
import ba.grbo.currencyconverter.data.models.database.MultiExchangeableCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency as DomainCurrency

interface CurrenciesRepository {
    val exchangeableCurrencies: MutableStateFlow<List<DomainCurrency>>
    val exception: MutableSharedFlow<Exception>

    val dummyExchangeableCurrencies: MutableStateFlow<List<DomainCurrency>>

    fun observeCurrencies(scope: CoroutineScope)

    fun syncExchangeableCurrenciesWithLocale(scope: CoroutineScope, context: Context)

    suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean>

    fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>>

    suspend fun updateCurrency(currency: DomainCurrency): DatabaseResult<Boolean>

    suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency>

    suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>>

    fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>>
}