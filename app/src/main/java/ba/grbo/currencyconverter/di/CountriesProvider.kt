package ba.grbo.currencyconverter.di

import android.content.Context
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.source.local.static.Countries
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object CountriesProvider {
    // Intentionaly doing mapping on the main thread, so it blocks until countries are ready, in
    // meantime a splash will be shown
    @ActivityRetainedScoped
    @Provides
    fun provideCountries(@ApplicationContext context: Context): MutableList<Country> {
        return if (Countries.areInitialized()) Countries.countries
        else Countries.toCountries(context)
    }
}