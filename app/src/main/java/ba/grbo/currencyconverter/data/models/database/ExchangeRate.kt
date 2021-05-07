package ba.grbo.currencyconverter.data.models.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ba.grbo.currencyconverter.data.models.shared.Evaluable
import ba.grbo.currencyconverter.util.Constants
import java.util.*

@Entity(
    tableName = Constants.EXCHANGE_RATES_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = UnexchangeableCurrency::class,
            parentColumns = [Constants.PARENT_COLUMNS],
            childColumns = [Constants.CHILD_COLUMNS],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.RESTRICT
        )
    ]
)
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(index = true)
    val code: String,
    override val value: Double,
    override val date: Date
) : Evaluable {
    constructor(
        code: String,
        value: Double,
        date: Date
    ) : this(0, code, value, date)
}