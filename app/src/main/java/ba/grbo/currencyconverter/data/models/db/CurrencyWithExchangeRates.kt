package ba.grbo.currencyconverter.data.models.db

import androidx.room.Embedded
import androidx.room.Relation
import ba.grbo.currencyconverter.util.Constants.CHILD_COLUMNS
import ba.grbo.currencyconverter.util.Constants.PARENT_COLUMNS

data class CurrencyWithExchangeRates(
    @Embedded val currency: Currency,

    @Relation(
        parentColumn = PARENT_COLUMNS,
        entityColumn = CHILD_COLUMNS,
        entity = ExchangeRate::class
    )
    val exchangeRates: List<ExchangeRate>
)