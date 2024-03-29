package ruslan.galiev.myweather.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDatabaseDAO {
    @Insert
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("SELECT * FROM locations WHERE isCurrentLocation = 0 ORDER BY createdTime ASC")
    fun getWatchingLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE isCurrentLocation = 0 ORDER BY createdTime ASC")
    fun getLocations(): List<Location>

    @Query("SELECT * FROM locations WHERE isCurrentLocation = 1")
    suspend fun getCurrentLocation(): Location?

    @Query("SELECT * FROM locations WHERE latitude= :latitude AND longitude=:longitude AND isCurrentLocation = 0")
    suspend fun getLocation(latitude: Double, longitude: Double): Location?
}