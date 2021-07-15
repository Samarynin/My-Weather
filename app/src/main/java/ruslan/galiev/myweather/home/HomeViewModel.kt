package ruslan.galiev.myweather.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ruslan.galiev.myweather.database.Location
import ruslan.galiev.myweather.database.LocationDatabaseDAO
import ruslan.galiev.myweather.network.OpenWeatherApi
import ruslan.galiev.myweather.setting.Settings

class HomeViewModel(val database: LocationDatabaseDAO, application: Application) :
    AndroidViewModel(application) {

    private var _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?>
        get() = _currentLocation

    private var _notification = MutableLiveData<String?>()
    val notification: LiveData<String?>
        get() = _notification

    var watchingLocations: LiveData<List<Location>>

    private var _navigateTo = MutableLiveData<String>()
    val navigateTo: LiveData<String>
        get() = _navigateTo

    fun navigateToDetailFragment() {
        _navigateTo.value = "DetailFragment"
    }

    fun onNavigateComplete() {
        _navigateTo.value = ""
    }

    var settings: Settings = Settings()

    private var viewModelJob = Job()
    private var viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        watchingLocations = database.getWatchingLocations()
    }

    fun saveLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val language = settings.language ?: "en"
            val setUp =
                OpenWeatherApi.retrofitService.getCurrentWeatherByCoordinatesAsync(
                    latitude = latitude, longitude = longitude, language
                )
            try {
                val result = setUp.await()
                val location = Location(
                    longitude = longitude,
                    latitude = latitude,
                    timeZoneOffSet = result.offSetTimezone,
                    locationName = result.city,
                    country = result.sys.country,
                    isCurrentLocation = 1,
                    temperature = result.main.temp.toInt()
                )
                _currentLocation.value = location
                insertLocation(location)
                _notification.value = null
            } catch (t: Throwable) {
                _notification.value = t.message
            }
        }
    }

    private suspend fun insertLocation(location: Location) {
        return withContext(Dispatchers.IO) {
            database.insert(location)
        }
    }

}

