package ba.grbo.currencyconverter.data.models

import android.content.Context
import androidx.annotation.StringRes
import ba.grbo.currencyconverter.data.models.CurrencyName.*

data class Currency(
    @StringRes val name: Int,
    val code: Code,
) {
    fun getCurrencyName(currencyName: CurrencyName, context: Context) = when (currencyName) {
        BOTH -> "${code.name} — ${context.getString(name)}"
        CODE -> code.name
        NAME -> context.getString(name)
    }
}