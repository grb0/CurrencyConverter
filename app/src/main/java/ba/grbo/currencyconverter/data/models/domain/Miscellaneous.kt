package ba.grbo.currencyconverter.data.models.domain

import ba.grbo.currencyconverter.data.models.database.Miscellaneous as DatabaseMiscellaneous

data class Miscellaneous(
    val showOnlyFavorites: Boolean,
    val lastUsedFromCurrency: ExchangeableCurrency,
    val lastUsedToCurrency: ExchangeableCurrency
) {
    fun toDatabase(currencies: List<ExchangeableCurrency>) = DatabaseMiscellaneous(
        showOnlyFavorites,
        currencies.indexOfFirst { it.code == lastUsedFromCurrency.code },
        currencies.indexOfFirst { it.code == lastUsedToCurrency.code }
    )

    fun syncWithLocale(syncedCurrencies: List<ExchangeableCurrency>): Miscellaneous {
        return Miscellaneous(
            showOnlyFavorites,
            syncedCurrencies.find { it.code == lastUsedFromCurrency.code }!!,
            syncedCurrencies.find { it.code == lastUsedToCurrency.code }!!
        )
    }
}