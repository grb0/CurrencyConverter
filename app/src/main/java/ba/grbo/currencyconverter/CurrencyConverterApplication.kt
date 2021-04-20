package ba.grbo.currencyconverter

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import ba.grbo.currencyconverter.ui.fragments.SettingsFragment
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CurrencyConverterApplication : Application() {
    @Inject
    lateinit var theme: String
    override fun onCreate() {
        super.onCreate()
        setTheme(theme)
        initializeTimber()
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    fun setTheme(theme: String): Boolean {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                SettingsFragment.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                SettingsFragment.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                SettingsFragment.DEVICE -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> throw IllegalArgumentException("Unknown theme: $theme")
            }
        )
        return true
    }
}