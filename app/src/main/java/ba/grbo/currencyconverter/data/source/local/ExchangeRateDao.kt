package ba.grbo.currencyconverter.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ba.grbo.currencyconverter.data.models.database.EssentialExchangeRate
import ba.grbo.currencyconverter.data.models.database.ExchangeRate
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ExchangeRateDao {
    @Insert
    suspend fun insert(exchangeRate: ExchangeRate)

    @Query("SELECT * FROM exchange_rates_table WHERE code = :code AND date >= :fromDate AND date <= :toDate")
    suspend fun getExchangeRates(
        code: String,
        fromDate: Date,
        toDate: Date,
    ): List<ExchangeRate>

    @Query("SELECT value, date FROM exchange_rates_table GROUP BY code HAVING MAX(date)")
    suspend fun getMostRecentExchangeRates(): List<EssentialExchangeRate>

    // Provjeriti da li ovaj flow daje tačne vrijednosti kada je noviji exchange rate ubaci
    @Query("SELECT value, date FROM exchange_rates_table GROUP BY code HAVING MAX(date)")
    fun observeMostRecentExchangeRates(): Flow<List<EssentialExchangeRate>>
}