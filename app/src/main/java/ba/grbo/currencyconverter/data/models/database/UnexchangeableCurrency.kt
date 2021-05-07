package ba.grbo.currencyconverter.data.models.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ba.grbo.currencyconverter.data.models.shared.Evaluable
import ba.grbo.currencyconverter.data.models.shared.Unexchangeable
import ba.grbo.currencyconverter.util.Constants
import ba.grbo.currencyconverter.util.Constants.UNEXCHANGEABLE_CURRENCIES_TABLE

@Entity(
    tableName = UNEXCHANGEABLE_CURRENCIES_TABLE,
    indices = [Index(value = [Constants.NAME], unique = true)]
)
data class UnexchangeableCurrency(
    @PrimaryKey
    override val code: String,
    override val symbol: String,
    override val nameResourceName: String,
    override val flagResourceName: String,
    override var isFavorite: Boolean
) : Unexchangeable {
    fun toExchangeableCurrency(evaluable: Evaluable) = ExchangeableCurrency(
        code,
        symbol,
        nameResourceName,
        flagResourceName,
        isFavorite,
        evaluable
    )

    fun toMultiExchangeableCurrency(
        evaluables: List<Evaluable>
    ) = MultiExchangeableCurrency(
        code,
        symbol,
        nameResourceName,
        flagResourceName,
        isFavorite,
        evaluables
    )
}
