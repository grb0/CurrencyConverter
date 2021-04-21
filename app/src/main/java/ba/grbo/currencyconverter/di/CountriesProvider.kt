package ba.grbo.currencyconverter.di

import android.content.Context
import androidx.core.content.ContextCompat
import ba.grbo.currencyconverter.data.models.Country
import ba.grbo.currencyconverter.data.models.Currency
import ba.grbo.currencyconverter.data.source.local.static.CountryResources
import ba.grbo.currencyconverter.util.updateLocale
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.*

@Module
@InstallIn(ViewModelComponent::class)
object CountriesProvider {
    // Intentionaly doing mapping on the main thread, so it blocks until countries are ready, in
    // meantime a splash will be shown
    @ViewModelScoped
    @Provides
    fun provideCountries(
        @ApplicationContext context: Context,
        language: Locale
    ): List<Country> {
        val modifiedContext = context.updateLocale(language)
        return CountryResources.value.map {
            Country(
                Currency(
                    Currency.Name(
                        it.currencyCode.name,
                        modifiedContext.getString(it.currencyName)
                    ),
                    it.currencyCode
                ),
                ContextCompat.getDrawable(modifiedContext, it.flag)!!
            )
        }
    }
}