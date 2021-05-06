package ba.grbo.currencyconverter.data.source.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ba.grbo.currencyconverter.data.models.db.ExchangeRate
import ba.grbo.currencyconverter.data.models.domain.Currency
import java.util.*

@Dao
interface ExchangeRateDao {
    @Insert
    suspend fun insert(exchangeRate: ExchangeRate)

    @Query("SELECT * FROM exchange_rates_table WHERE code = :code AND date >= :fromDate AND date <= :toDate")
    suspend fun getCurrencyCustomExchangeRatesRange(
        code: Currency.Code,
        fromDate: Date,
        toDate: Date
    ): List<ExchangeRate>
}