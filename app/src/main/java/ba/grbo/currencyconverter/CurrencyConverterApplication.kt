package ba.grbo.currencyconverter

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import ba.grbo.currencyconverter.util.Language
import ba.grbo.currencyconverter.util.Theme
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
}