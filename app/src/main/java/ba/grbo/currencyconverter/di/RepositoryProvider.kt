package ba.grbo.currencyconverter.di

import ba.grbo.currencyconverter.data.source.CurrenciesRepository
import ba.grbo.currencyconverter.data.source.DefaultCurrenciesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class RepositoryProvider {
    @ActivityRetainedScoped
    @Binds
    abstract fun bindCurrenciesRepository(
        repository: DefaultCurrenciesRepository
    ): CurrenciesRepository
}