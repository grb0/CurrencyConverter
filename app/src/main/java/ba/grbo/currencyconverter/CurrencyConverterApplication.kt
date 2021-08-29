package ba.grbo.currencyconverter

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import ba.grbo.currencyconverter.util.*
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class CurrencyConverterApplication : Application() {
    @Inject
    lateinit var theme: Theme

    override fun onCreate() {
        super.onCreate()
        setTheme(theme)
        initTimber()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.updateLocale())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (getLanguage() == Language.DEVICE && didLanguageChange(newConfig)) exitProcess(0)
    }

    @Suppress("DEPRECATION")
    private fun didLanguageChange(newConfig: Configuration): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.locales[0] != resources.configuration.locales[0]
        } else newConfig.locale != resources.configuration.locale
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

    private fun initTimber() {
        Timber.plant(if (BuildConfig.DEBUG) Trees.Debug else Trees.Release)
    }
}