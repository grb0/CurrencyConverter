package ba.grbo.currencyconverter.data.models.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ba.grbo.currencyconverter.data.models.domain.Currency.Code
import ba.grbo.currencyconverter.util.Constants.CURRENCIES_TABLE
import ba.grbo.currencyconverter.util.Constants.NAME

@Entity(
    tableName = CURRENCIES_TABLE,
    indices = [Index(value = [NAME], unique = true)]
)
data class Currency(
    @PrimaryKey
    val code: Code,
    val nameResourceName: String,
    val flagResourceName: String,
    val isFavorite: Boolean
)