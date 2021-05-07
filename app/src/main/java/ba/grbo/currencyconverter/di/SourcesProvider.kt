package ba.grbo.currencyconverter.di

import ba.grbo.currencyconverter.data.source.LocalCurrenciesSource
import ba.grbo.currencyconverter.data.source.local.CurrenciesSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class SourcesProvider {
    @ActivityRetainedScoped
    @Binds
    abstract fun bindLocalSource(source: CurrenciesSource): LocalCurrenciesSource
}