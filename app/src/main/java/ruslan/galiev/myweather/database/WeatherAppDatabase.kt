package ruslan.galiev.myweather.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

@Database(entities = [Location::class, SuggestLocation::class], version = 1, exportSchema = true)
abstract class WeatherAppDatabase : RoomDatabase() {

    abstract val locationDatabaseDAO: LocationDatabaseDAO
    abstract val suggestLocationDatabaseDAO: SuggestLocationDatabaseDAO

    companion object {
        @Volatile
        private var INSTANCE: WeatherAppDatabase? = null

        fun getInstance(context: Context): WeatherAppDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeatherAppDatabase::class.java,
                        "weather_app.db"
                    ).addCallback(
                        object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                // insert the data on the IO Thread
                                Executors.newSingleThreadExecutor().execute {
                                    getInstance(context).suggestLocationDatabaseDAO
                                        .insertMany(suggestLocationsData)
                                }
                            }
                        }
                    )
                        .fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}