package ba.grbo.currencyconverter

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import ba.grbo.currencyconverter.util.Language
import ba.grbo.currencyconverter.util.Theme
import ba.grbo.currencyconverter.util.getLanguage
import ba.grbo.currencyconverter.util.updateLocale
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
        initializeTimber()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.updateLocale())
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

    private fun initializeTimber() {
        val tree = object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "ba.grbo -> $tag", message, t)
            }

            override fun createStackElementTag(element: StackTraceElement): String {
                return String.format(
                    "%s -> %s",
                    super.createStackElementTag(element),
                    element.methodName
                )
            }
        }
        if (BuildConfig.DEBUG) Timber.plant(tree)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (getLanguage() == Language.DEVICE) exitProcess(0)
    }
}