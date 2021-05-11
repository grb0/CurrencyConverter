package ba.grbo.currencyconverter.data.models.domain

import ba.grbo.currencyconverter.data.models.database.Miscellaneous as DatabaseMiscellaneous1

data class Miscellaneous(
    val showOnlyFavorites: Boolean,
    val lastUsedFromExchangeableCurrency: ExchangeableCurrency,
    val lastUsedToExchangeableCurrency: ExchangeableCurrency
) {
    fun toDatabase(exchangeableCurrencies: List<ExchangeableCurrency>) = DatabaseMiscellaneous1(
        showOnlyFavorites,
        exchangeableCurrencies.indexOfFirst { it.code == lastUsedFromExchangeableCurrency.code },
        exchangeableCurrencies.indexOfFirst { it.code == lastUsedToExchangeableCurrency.code }
    )

    fun syncWithLocale(syncedExchangeableCurrencies: List<ExchangeableCurrency>): Miscellaneous {
        return Miscellaneous(
            showOnlyFavorites,
            syncedExchangeableCurrencies.find { it.code == lastUsedFromExchangeableCurrency.code }!!,
            syncedExchangeableCurrencies.find { it.code == lastUsedToExchangeableCurrency.code }!!
        )
    }
}