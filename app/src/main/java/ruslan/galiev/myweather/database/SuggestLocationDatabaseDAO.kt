package ruslan.galiev.myweather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SuggestLocationDatabaseDAO {
    @Query("SELECT * FROM suggest_locations")
    suspend fun getSuggestLocation(): List<SuggestLocation>

    @Insert
    fun insertMany(suggestLocations: List<SuggestLocation>)
}