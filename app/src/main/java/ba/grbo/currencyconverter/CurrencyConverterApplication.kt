package ba.grbo.currencyconverter

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import ba.grbo.currencyconverter.data.models.preferences.Language
import ba.grbo.currencyconverter.data.models.preferences.Theme
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class CurrencyConverterApplication : Application() {
    @Inject
    lateinit var theme: Theme

    @Inject
    lateinit var language: Locale

    override fun onCreate() {
        super.onCreate()
        setTheme(theme)
        initializeTimber()
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    fun setTheme(theme: Theme): Boolean {
        this.theme = theme
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Theme.DEVICE -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
        return true
    }

    fun setLanguage(language: Language) {
        this.language = language.toLocale()
    }
}