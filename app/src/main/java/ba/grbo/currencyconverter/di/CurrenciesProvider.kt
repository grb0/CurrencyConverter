package ba.grbo.currencyconverter.di

import android.content.Context
import ba.grbo.currencyconverter.data.models.database.EssentialExchangeRate
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.data.source.CurrenciesRepository
import ba.grbo.currencyconverter.data.source.Result.Error
import ba.grbo.currencyconverter.data.source.Result.Success
import ba.grbo.currencyconverter.util.toDomain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Module
@InstallIn(ActivityRetainedComponent::class)
object CurrenciesProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideCurrencies(
        @ApplicationContext context: Context,
        repository: CurrenciesRepository,
        scope: CoroutineScope
    ): MutableStateFlow<List<ExchangeableCurrency>> {
        val currencies = MutableStateFlow<List<ExchangeableCurrency>>(listOf())

        scope.launch {
            val dbCurrenciesResult = repository.getExchangeableCurrencies()
            val dbCurrencies = if (dbCurrenciesResult is Success) dbCurrenciesResult.data
            else {
                dbCurrenciesResult as Error
                throw dbCurrenciesResult.exception
            }

            currencies.value = dbCurrencies.toDomain(context)

            val dbAreFavoritesResult = repository.observeAreUnexchangeableCurrenciesFavorite()
            val dbAreFavorites = if (dbAreFavoritesResult is Success) dbAreFavoritesResult.data
            else {
                dbAreFavoritesResult as Error
                throw  dbAreFavoritesResult.exception
            }

            fun updateFavorites(list: List<Boolean>) {
                list.forEachIndexed { index, isFavorite ->
                    if (scope.isActive) {
                        currencies.value[index].isFavorite = isFavorite
                    }
                }
            }

            dbAreFavorites
                .distinctUntilChanged()
                .onEach { updateFavorites(it) }
                .flowOn(Dispatchers.Default)
                .launchIn(scope)

            val dbMostRecentExchangeRatesResult = repository.observeMostRecentExchangeRates()
            val dbMostRecentExchangeRates = if (dbMostRecentExchangeRatesResult is Success) {
                dbMostRecentExchangeRatesResult.data
            } else {
                dbMostRecentExchangeRatesResult as Error
                throw dbMostRecentExchangeRatesResult.exception
            }

            fun updateExchangeRates(exchangeRates: List<EssentialExchangeRate>) {
                exchangeRates.forEachIndexed { index, essentialExchangeRate ->
                    if (scope.isActive) {
                        currencies.value[index].exchangeRate = essentialExchangeRate
                    }
                }
            }

            dbMostRecentExchangeRates
                .distinctUntilChanged()
                .onEach { updateExchangeRates(it) }
                .flowOn(Dispatchers.Default)
                .launchIn(scope)
        }

        @Suppress("ControlFlowWithEmptyBody")
        runBlocking {
            while (currencies.value.isEmpty()) {
            }
        }

        return currencies
    }

    @ActivityRetainedScoped
    @Provides
    fun provideCoroutineScope(lifecycle: ActivityRetainedLifecycle): CoroutineScope {
        val scope = CoroutineScope(Dispatchers.IO)

        lifecycle.addOnClearedListener {
            scope.cancel() // it happens only when we finally leave the app, not on config. change
        }

        return scope
    }
}