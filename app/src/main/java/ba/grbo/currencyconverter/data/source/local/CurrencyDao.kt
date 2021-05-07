package ba.grbo.currencyconverter.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ba.grbo.currencyconverter.data.models.database.ExchangeableCurrency
import ba.grbo.currencyconverter.data.models.database.MultiExchangeableCurrency
import ba.grbo.currencyconverter.data.models.database.UnexchangeableCurrency
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class CurrencyDao(database: CurrencyConverterDatabase) {
    private val exchangeRateDao = database.exchangeRateDao

    @Update
    abstract suspend fun update(currency: UnexchangeableCurrency)

    @Query("SELECT * FROM unexchangeable_currencies_table WHERE code = :code")
    abstract suspend fun getUnexchangeableCurrency(code: String): UnexchangeableCurrency

    @Query("SELECT * FROM unexchangeable_currencies_table")
    abstract suspend fun getUnexchangeableCurrencies(): List<UnexchangeableCurrency>

    @Transaction
    open suspend fun getMultiExchangeableCurrency(
        code: String,
        fromDate: Date,
        toDate: Date
    ): MultiExchangeableCurrency {
        val unexchangeableCurrency = getUnexchangeableCurrency(code)
        val exchangeRates = exchangeRateDao.getExchangeRates(
            code,
            fromDate,
            toDate
        )

        return unexchangeableCurrency.toMultiExchangeableCurrency(exchangeRates)
    }

    @Transaction
    open suspend fun getExchangeableCurrencies(): List<ExchangeableCurrency> {
        val unexchangeableCurrencies = getUnexchangeableCurrencies()
        val mostRecentExchangeRates = exchangeRateDao.getMostRecentExchangeRates()

        return unexchangeableCurrencies.zip(mostRecentExchangeRates).map {
            it.first.toExchangeableCurrency(it.second)
        }
    }

    @Query("SELECT isFavorite FROM unexchangeable_currencies_table")
    abstract fun observeAreUnexchangeableCurrenciesFavorite(): Flow<List<Boolean>>
}