package ba.grbo.currencyconverter.data.models.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ba.grbo.currencyconverter.data.models.domain.ExchangeableCurrency
import ba.grbo.currencyconverter.data.models.domain.Miscellaneous
import ba.grbo.currencyconverter.util.Constants.MISCELLANEOUS_TABLE

@Entity(tableName = MISCELLANEOUS_TABLE)
data class Miscellaneous(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val showOnlyFavorites: Boolean,
    val lastUsedFromCurrencyPosition: Int,
    val lastUsedToCurrencyPosition: Int
) {
    constructor(
        showOnlyFavorites: Boolean,
        lastUsedFromCurrencyPosition: Int,
        lastUsedToCurrencyPosition: Int
    ) : this(1, showOnlyFavorites, lastUsedFromCurrencyPosition, lastUsedToCurrencyPosition)

    fun toDomain(exchangeableCurrencies: List<ExchangeableCurrency>) = Miscellaneous(
        showOnlyFavorites,
        exchangeableCurrencies[lastUsedFromCurrencyPosition],
        exchangeableCurrencies[lastUsedToCurrencyPosition]
    )
}
