package ba.grbo.currencyconverter.di

import android.app.Application
import androidx.core.content.ContextCompat
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.source.local.static.CountryResources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object CountriesProvider {
    // Intentionaly doing mapping on the main thread, so it blocks until countries are ready, in
    // meantime a splash will be shown
    @ActivityRetainedScoped
    @Provides
    fun provideCountries(application: Application): List<Country> {
        return CountryResources.value.map {
            Country(
                Currency(
                    Currency.Name(
                        it.currencyCode.name,
                        application.getString(it.currencyName)
                    ),
                    it.currencyCode
                ),
                ContextCompat.getDrawable(application, it.flag)!!
            )
        }
    }
}