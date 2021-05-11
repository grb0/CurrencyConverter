package ba.grbo.currencyconverter.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ba.grbo.currencyconverter.data.models.database.ExchangeRate
import ba.grbo.currencyconverter.data.models.database.Miscellaneous
import ba.grbo.currencyconverter.data.models.database.UnexchangeableCurrency
import ba.grbo.currencyconverter.util.Constants.CURRENCY_CONVERTER_DATABASE

@Database(
    entities = [UnexchangeableCurrency::class, ExchangeRate::class, Miscellaneous::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class CurrencyConverterDatabase : RoomDatabase() {
    abstract val exchangeRateDao: ExchangeRateDao
    abstract val miscellaneousDao: MiscellaneousDao
    abstract val currencyDao: CurrencyDao

    companion object {
        @Volatile
        private lateinit var INSTANCE: CurrencyConverterDatabase

        fun getInstance(context: Context) = if (::INSTANCE.isInitialized) INSTANCE
        else synchronized(this) { buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(
                context,
                CurrencyConverterDatabase::class.java,
                CURRENCY_CONVERTER_DATABASE
            )
            .createFromAsset(CURRENCY_CONVERTER_DATABASE)
            .build()
    }
}