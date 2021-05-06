package ba.grbo.currencyconverter.data.source.local.db

import androidx.room.TypeConverter
import ba.grbo.currencyconverter.data.models.domain.Currency
import java.util.*

class Converters {
    @TypeConverter
    fun codeToString(code: Currency.Code) = code.name

    @TypeConverter
    fun stringToCode(code: String) = Currency.Code.valueOf(code)

    @TypeConverter
    fun dateToLong(date: Date) = date.time

    @TypeConverter
    fun longToDate(date: Long) = Date(date)
}