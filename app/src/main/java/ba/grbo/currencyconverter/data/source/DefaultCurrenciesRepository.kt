package ba.grbo.currencyconverter.data.source

import android.content.Context
import ba.grbo.currencyconverter.data.models.database.*
import ba.grbo.currencyconverter.di.DispatcherDefault
import ba.grbo.currencyconverter.util.toDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency as DomainCurrency
import ba.grbo.currencyconverter.data.models.domain.Miscellaneous as DomainMiscellaneous

class DefaultCurrenciesRepository @Inject constructor(
    private val localCurrenciesSource: LocalCurrenciesSource,
    @DispatcherDefault private val coroutineDispatcher: CoroutineDispatcher,
    @ApplicationContext context: Context,
) : CurrenciesRepository {
    private val _exchangeableCurrencies = MutableStateFlow<List<DomainCurrency>>(emptyList())
    override val exchangeableCurrencies: StateFlow<List<DomainCurrency>>
        get() = _exchangeableCurrencies

    override lateinit var miscellaneous: DomainMiscellaneous
    override val exception: MutableStateFlow<Exception?> = MutableStateFlow(null)

    init {
        runBlocking {
            when (val dbResult = getExchangeableCurrencies()) {
                is Success -> {
                    _exchangeableCurrencies.value = dbResult.data.toDomain(context)
                    initMiscellaneous()
                }
                is Error -> exception.tryEmit(dbResult.exception)
            }
        }
    }

    override fun converterDataIsReady(): Boolean {
        return exchangeableCurrencies.value.isNotEmpty() && ::miscellaneous.isInitialized
    }

    override fun exchangebleCurrenciesAreNotEmpty() = exchangeableCurrencies.value.isNotEmpty()

    override fun syncExchangeableCurrenciesWithLocale(scope: CoroutineScope, context: Context) {
        scope.launch(coroutineDispatcher) {
            _exchangeableCurrencies.value = exchangeableCurrencies.value.map {
                it.copy(context = context)
            }
        }
    }

    override fun observeCurrenciesAndMiscellaneous(scope: CoroutineScope) {
        observeMiscellaneous(scope)
        observeForFavoritesChange(scope)
        observeForExchangeRatesChange(scope)
    }

    private suspend fun initMiscellaneous() {
        when (val miscellaneous = localCurrenciesSource.getMiscellaneous()) {
            is Success -> {
                this.miscellaneous = miscellaneous.data.toDomain(_exchangeableCurrencies.value)
            }
            is Error -> exception.tryEmit(miscellaneous.exception)
        }
    }

    private fun observeMiscellaneous(scope: CoroutineScope) {
        when (val miscellaneous = localCurrenciesSource.observeMiscellaneous()) {
            is Success -> miscellaneous.data
                .onEach { this.miscellaneous = it.toDomain(_exchangeableCurrencies.value) }
                .flowOn(coroutineDispatcher)
                .launchIn(scope)
            is Error -> exception.tryEmit(miscellaneous.exception)
        }
    }

    private fun observeForFavoritesChange(scope: CoroutineScope) {
        observeForChange(
            ::observeAreUnexchangeableCurrenciesFavorite,
            ::updateFavorites,
            scope
        )
    }

    private fun observeForExchangeRatesChange(scope: CoroutineScope) {
        observeForChange(
            ::observeMostRecentExchangeRates,
            ::updateExchangeRates,
            scope
        )
    }

    private fun <T> observeForChange(
        dbAction: () -> DatabaseResult<Flow<List<T>>>,
        onEach: (List<T>, CoroutineScope) -> Unit,
        scope: CoroutineScope
    ) {
        when (val dbResult = dbAction()) {
            is Success -> dbResult.data.collect(scope, onEach)
            is Error -> exception.tryEmit(dbResult.exception)
        }
    }

    private fun updateFavorites(list: List<Boolean>, scope: CoroutineScope) {
        list.forEachIndexed { index, isFavorite ->
            if (scope.isActive) exchangeableCurrencies.value[index].isFavorite = isFavorite
        }
    }

    private fun updateExchangeRates(
        exchangeRates: List<EssentialExchangeRate>,
        scope: CoroutineScope
    ) {
        exchangeRates.forEachIndexed { index, essentialExchangeRate ->
            if (scope.isActive) {
                exchangeableCurrencies.value[index].exchangeRate = essentialExchangeRate
            }
        }
    }

    private fun <T> Flow<T>.collect(
        scope: CoroutineScope,
        onEach: (T, CoroutineScope) -> Unit
    ) {
        distinctUntilChanged()
            .onEach { onEach(it, scope) }
            .flowOn(coroutineDispatcher)
            .launchIn(scope)
    }

    override suspend fun insertExchangeRate(exchangeRate: ExchangeRate): DatabaseResult<Boolean> {
        return localCurrenciesSource.insertExchangeRate(exchangeRate)
    }

    override fun observeMostRecentExchangeRates(): DatabaseResult<Flow<List<EssentialExchangeRate>>> {
        return localCurrenciesSource.observeMostRecentExchangeRates()
    }

    override suspend fun updateMiscellaneous(miscellaneous: Miscellaneous): DatabaseResult<Boolean> {
        return localCurrenciesSource.updateMiscellaneous(miscellaneous)
    }

    override suspend fun updateCurrency(currency: DomainCurrency): DatabaseResult<Boolean> {
        return localCurrenciesSource.updateUnexchangeableCurrency(currency.toDatabase())
    }

    override suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): DatabaseResult<MultiExchangeableCurrency> {
        return localCurrenciesSource.getMultiExchangeableCurrency(code, fromDate, toDate)
    }

    override suspend fun getExchangeableCurrencies(): DatabaseResult<List<ExchangeableCurrency>> {
        return localCurrenciesSource.getExchangeableCurrencies()
    }

    override fun observeAreUnexchangeableCurrenciesFavorite(): DatabaseResult<Flow<List<Boolean>>> {
        return localCurrenciesSource.observeAreUnexchangeableCurrenciesFavorite()
    }
}