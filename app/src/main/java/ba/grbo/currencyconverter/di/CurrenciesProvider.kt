package ba.grbo.currencyconverter.di

import android.content.Context
import ba.grbo.currencyconverter.data.models.db.toDomain
import ba.grbo.currencyconverter.data.models.domain.Currency
import ba.grbo.currencyconverter.data.source.CurrenciesRepository
import ba.grbo.currencyconverter.data.source.Result.Error
import ba.grbo.currencyconverter.data.source.Result.Success
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
    ): MutableStateFlow<List<Currency>> {
        val currencies = MutableStateFlow<List<Currency>>(listOf())

        scope.launch {
            val dbCurrenciesResult = repository.getCurrenciesWithMostRecentExchangeRate()
            val dbCurrencies = if (dbCurrenciesResult is Success) dbCurrenciesResult.data
            else {
                dbCurrenciesResult as Error
                throw dbCurrenciesResult.exception
            }

            currencies.value = dbCurrencies.toDomain(context)

            val dbIsFavoriteResult = repository.observeIsFavorite()
            val dbIsFavorite = if (dbIsFavoriteResult is Success) dbIsFavoriteResult.data
            else {
                dbIsFavoriteResult as Error
                throw  dbIsFavoriteResult.exception
            }

            fun updateCurrencies(list: List<Boolean>) {
                list.forEachIndexed { index, isFavorite ->
                    if (scope.isActive) {
                        currencies.value[index].isFavorite = isFavorite
                    }
                }
            }

            dbIsFavorite
                .distinctUntilChanged()
                .onEach { updateCurrencies(it) }
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