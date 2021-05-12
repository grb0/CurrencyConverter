package ba.grbo.currencyconverter.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ba.grbo.currencyconverter.data.models.database.Miscellaneous
import kotlinx.coroutines.flow.Flow

@Dao
interface MiscellaneousDao {
    @Update
    suspend fun update(miscellaneous: Miscellaneous): Int

    @Query("SELECT * FROM miscellaneous_table LIMIT 1")
    fun get(): Miscellaneous

    @Query("SELECT * FROM miscellaneous_table LIMIT 1")
    fun observe(): Flow<Miscellaneous>
}