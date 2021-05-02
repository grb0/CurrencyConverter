package ba.grbo.currencyconverter.util

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.util.*

enum class Language {
    BOSNIAN,
    ENGLISH,
    GERMAN,
    DEVICE;

    fun toLocale(): Locale = when (this) {
        BOSNIAN -> Locale("bs")
        ENGLISH -> Locale.ENGLISH
        GERMAN -> Locale.GERMAN
        DEVICE -> ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    }
}