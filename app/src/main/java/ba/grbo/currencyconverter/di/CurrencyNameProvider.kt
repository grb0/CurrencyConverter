package ba.grbo.currencyconverter.di

import android.app.Application
import ba.grbo.currencyconverter.data.models.CurrencyName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object CurrencyNameProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideCurrencyName(application: Application): CurrencyName {
        // val sharedPreferences = application.getSharedPreferences(
        //         application.getString(R.string.preference_file_key),
        //         Context.MODE_PRIVATE
        // )

        // val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        // sharedPreferences.

        return CurrencyName.BOTH_CODE
    }
}