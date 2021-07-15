package ruslan.galiev.myweather.findlocation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import ruslan.galiev.myweather.database.SuggestLocation
import ruslan.galiev.myweather.database.SuggestLocationDatabaseDAO
import ruslan.galiev.myweather.network.OpenWeatherApi
import ruslan.galiev.myweather.responses.RelatedNameLocation

class FindLocationViewModel(
    private val database: SuggestLocationDatabaseDAO,
    private val application: Application
) : ViewModel() {

    private var _listRelatedNameLocations = MutableLiveData<List<RelatedNameLocation>>()
    val listRelatedNameLocations: LiveData<List<RelatedNameLocation>>
        get() = _listRelatedNameLocations

    private var _listOfSuggestLocations = MutableLiveData<List<SuggestLocation>>()
    val listOfSuggestLocations: LiveData<List<SuggestLocation>>
        get() = _listOfSuggestLocations

    private var _notification = MutableLiveData<String>()
    val notification: LiveData<String>
        get() = _notification

    private var _navigateTo = MutableLiveData<String>()

    fun navigateToDetailFragment() {
        _navigateTo.value = "DetailFragment"
    }

    fun onNavigateCompleted() {
        _navigateTo.value = ""
        clearListRelatedNameLocations()
    }

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        loadSuggestLocations()
    }

    private fun loadSuggestLocations() {
        viewModelScope.launch {
            val suggestLocation: List<SuggestLocation> = getSuggestLocation()
            _listOfSuggestLocations.value = suggestLocation
        }
    }

    private suspend fun getSuggestLocation(): List<SuggestLocation> {
        return withContext(Dispatchers.IO) {
            database.getSuggestLocation()
        }
    }

    fun findLocationByName(locationName: String) {
        viewModelScope.launch {
            val setUp = OpenWeatherApi.retrofitService.getLocationByNameAsync(locationName)
            try {
                val result = setUp.await()
                if (result.isEmpty()) {
                    _notification.value = "Not found"
                } else {
                    _listRelatedNameLocations.value = result
                }
            } catch (e: Exception) {
                e.message?.let {

                }
            }
        }
    }

    private fun clearListRelatedNameLocations() {
        _listRelatedNameLocations.value = null
    }

    fun onShowNotificationComplete() {
        _notification.value = ""
    }

}