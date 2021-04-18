package ba.grbo.currencyconverter.data.models

import android.content.Context
import androidx.annotation.StringRes
import ba.grbo.currencyconverter.data.models.CurrencyName.BOTH_CODE
import ba.grbo.currencyconverter.data.models.CurrencyName.BOTH_NAME
import ba.grbo.currencyconverter.data.models.CurrencyName.CODE
import ba.grbo.currencyconverter.data.models.CurrencyName.NAME

data class Currency(
        @StringRes val nameRes: Int,
        val code: Code,
) {
    lateinit var name: String

    fun initializeName(currencyName: String, context: Context): String {
        return provideName(currencyName, context).also { name = it }
    }

    private fun provideName(currencyName: String, context: Context) = when (currencyName) {
        CODE -> code.name
        NAME -> context.getString(nameRes)
        BOTH_CODE -> "${code.name} — ${context.getString(nameRes)}"
        BOTH_NAME -> "${context.getString(nameRes)} — ${code.name}"
        else -> throw IllegalArgumentException("Unknown currencyName: $currencyName")
    }

    val isNameInitialized
        get() = ::name.isInitialized
}