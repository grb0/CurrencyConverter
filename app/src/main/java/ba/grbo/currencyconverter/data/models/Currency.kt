package ba.grbo.currencyconverter.data.models

import android.content.Context
import androidx.annotation.StringRes
import ba.grbo.currencyconverter.data.models.CurrencyName.*

data class Currency(
        @StringRes val nameRes: Int,
        val code: Code,
) {
    lateinit var name: String

    fun getCurrencyName(currencyName: CurrencyName, context: Context) = when (currencyName) {
        NAME -> context.getString(nameRes)
        CODE -> code.name
        BOTH_CODE -> "${code.name} — ${context.getString(nameRes)}"
        BOTH_NAME -> "${context.getString(nameRes)} — ${code.name}"
    }.also { name = it }

    val isNameInitialized
        get() = ::name.isInitialized
}