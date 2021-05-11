package ba.grbo.currencyconverter.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import ba.grbo.currencyconverter.data.models.database.Miscellaneous

@Dao
interface MiscellaneousDao {
    @Update
    fun update(miscellaneous: Miscellaneous)

    @Query("SELECT * FROM miscellaneous_table LIMIT 1")
    suspend fun getMiscellaneous(): Miscellaneous
}