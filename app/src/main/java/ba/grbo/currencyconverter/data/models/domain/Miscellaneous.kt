package ba.grbo.currencyconverter.data.models.domain

import ba.grbo.currencyconverter.data.models.database.Miscellaneous

data class Miscellaneous(
    val showOnlyFavorites: Boolean,
    val lastUsedFromExchangeableCurrency: ExchangeableCurrency,
    val lastUsedToExchangeableCurrency: ExchangeableCurrency
) {
    fun toDatabase(exchangeableCurrencies: List<ExchangeableCurrency>) = Miscellaneous(
        showOnlyFavorites,
        exchangeableCurrencies.indexOfFirst { it.code == lastUsedFromExchangeableCurrency.code },
        exchangeableCurrencies.indexOfFirst { it.code == lastUsedToExchangeableCurrency.code }
    )
}