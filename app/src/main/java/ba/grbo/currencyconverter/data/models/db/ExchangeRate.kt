package ba.grbo.currencyconverter.data.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ba.grbo.currencyconverter.data.models.domain.Currency.Code
import ba.grbo.currencyconverter.util.Constants.CHILD_COLUMNS
import ba.grbo.currencyconverter.util.Constants.EXCHANGE_RATES_TABLE
import ba.grbo.currencyconverter.util.Constants.PARENT_COLUMNS
import java.util.*

@Entity(
    tableName = EXCHANGE_RATES_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Currency::class,
            parentColumns = [PARENT_COLUMNS],
            childColumns = [CHILD_COLUMNS],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.RESTRICT
        )
    ]
)
class ExchangeRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(index = true)
    val code: Code,
    val value: Double,
    val date: Date
) {
    constructor(
        code: Code,
        value: Double,
        date: Date
    ) : this(0, code, value, date)
}