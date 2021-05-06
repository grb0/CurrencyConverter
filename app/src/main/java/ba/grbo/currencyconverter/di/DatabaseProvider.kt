package ba.grbo.currencyconverter.di

import android.content.Context
import ba.grbo.currencyconverter.data.source.local.db.CurrencyConverterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
object DatabaseProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideCurrencyConverterDatabase(
        @ApplicationContext context: Context
    ): CurrencyConverterDatabase {
        return CurrencyConverterDatabase.getInstance(context)
    }
}