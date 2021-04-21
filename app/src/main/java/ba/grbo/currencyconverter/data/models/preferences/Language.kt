package ba.grbo.currencyconverter.data.models.preferences

import java.util.*

enum class Language {
    BOSNIAN,
    ENGLISH,
    GERMAN;

    fun toLocale(): Locale = when (this) {
        BOSNIAN -> Locale("bs")
        ENGLISH -> Locale.ENGLISH
        GERMAN -> Locale.GERMAN
    }
}