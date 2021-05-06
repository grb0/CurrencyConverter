package ba.grbo.currencyconverter.data.source.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ba.grbo.currencyconverter.data.models.db.Currency
import ba.grbo.currencyconverter.data.models.db.ExchangeRate
import ba.grbo.currencyconverter.data.models.db.MostRecentExchangeRate
import ba.grbo.currencyconverter.util.Constants.CURRENCY_CONVERTER_DATABASE

@Database(
    entities = [Currency::class, ExchangeRate::class],
    views = [MostRecentExchangeRate::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class CurrencyConverterDatabase : RoomDatabase() {
    abstract val currencyDao: CurrencyDao
    abstract val exchangeRateDao: ExchangeRateDao

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