package ba.grbo.currencyconverter.data.source.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ba.grbo.currencyconverter.data.models.db.Currency
import ba.grbo.currencyconverter.data.models.db.CurrencyWithExchangeRates
import ba.grbo.currencyconverter.data.models.db.CurrencyWithMostRecentExchangeRate
import ba.grbo.currencyconverter.data.models.domain.Currency.Code
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class CurrencyDao(database: CurrencyConverterDatabase) {
    private val exchangeRateDao = database.exchangeRateDao

    @Update
    abstract suspend fun update(currency: Currency)

    @Query("SELECT * FROM currencies_table WHERE code = :code")
    abstract suspend fun getCurrency(code: Code): Currency

    @Transaction
    open suspend fun getCurrencyWithCustomExchangeRatesRange(
        code: Code,
        fromDate: Date,
        toDate: Date
    ): CurrencyWithExchangeRates {
        val currency = getCurrency(code)
        val exchangeRates = exchangeRateDao.getCurrencyCustomExchangeRatesRange(
            code,
            fromDate,
            toDate
        )

        return CurrencyWithExchangeRates(currency, exchangeRates)
    }

    @Transaction
    @Query("SELECT * FROM currencies_table")
    abstract suspend fun getCurrenciesWithMostRecentExchangeRate(): List<CurrencyWithMostRecentExchangeRate>

    @Query("SELECT isFavorite FROM currencies_table")
    abstract fun observeIsFavorite(): Flow<List<Boolean>>
}