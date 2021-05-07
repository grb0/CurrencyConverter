package ba.grbo.currencyconverter.data.source.local

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun dateToLong(date: Date) = date.time

    @TypeConverter
    fun longToDate(date: Long) = Date(date)
}