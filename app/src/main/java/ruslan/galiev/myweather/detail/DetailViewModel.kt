package ruslan.galiev.myweather.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ruslan.galiev.myweather.convertTemperature
import ruslan.galiev.myweather.database.Location
import ruslan.galiev.myweather.database.LocationDatabaseDAO
import ruslan.galiev.myweather.getTemperatureText
import ruslan.galiev.myweather.network.OpenWeatherApi
import ruslan.galiev.myweather.responses.Hourly
import ruslan.galiev.myweather.responses.WeatherDetailResponse
import ruslan.galiev.myweather.setting.Settings
import java.text.SimpleDateFormat
import java.util.*

const val ONE_HOUR = 3600

class DetailViewModel(val database: LocationDatabaseDAO, application: Application) :
    AndroidViewModel(application) {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var locationName: String = ""

    private var _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    private var _notification = MutableLiveData<String>()
    val notification: LiveData<String>
        get() = _notification

    fun onShowNotificationComplete() {
        _notification.value = ""
    }

    private var _currentWeatherInformation = MutableLiveData<CurrentWeatherInformation>()
    val currentWeatherInformation: LiveData<CurrentWeatherInformation>
        get() = _currentWeatherInformation

    private var _listOfHourlyWeatherInformation = MutableLiveData<List<HourlyWeatherInformation>>()
    val listOfHourlyWeatherInformation: LiveData<List<HourlyWeatherInformation>>
        get() = _listOfHourlyWeatherInformation

    private var _listOfDailyWeatherInformation = MutableLiveData<List<DailyWeatherInformation>>()
    val listOfDailyWeatherInformation: LiveData<List<DailyWeatherInformation>>
        get() = _listOfDailyWeatherInformation

    private var _listOfLocationPhotoURL = MutableLiveData<List<String>>()
    val listOfLocationPhotoURL: LiveData<List<String>>
        get() = _listOfLocationPhotoURL

    private var viewModelJob = Job()
    private var viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var settings = Settings()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getWatchStatus(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        viewModelScope.launch {
            val location = getLocation()
            _location.value = location
        }
    }

    fun handleWatchIntent() {
        if (_location.value == null) {
            watchLocation(latitude, longitude)
        } else {
            unwatchLocation()
        }
    }

    private fun watchLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val setUp =
                OpenWeatherApi.retrofitService.getCurrentWeatherByCoordinatesAsync(
                    latitude, longitude, "ru"
                )
            try {
                val result = setUp.await()
                val location = Location(
                    longitude = longitude,
                    latitude = latitude,
                    timeZoneOffSet = result.offSetTimezone,
                    locationName = result.city,
                    country = result.sys.country,
                    isCurrentLocation = 0,
                    temperature = result.main.temp.toInt()
                )
                insertLocation(location)
                _location.value = location
                _notification.value = "Добавлено в избранное"
            } catch (t: Throwable) {
                _notification.value = t.message
            }
        }
    }

    private fun unwatchLocation() {
        viewModelScope.launch {
            if (_location.value != null) {
                deleteLocation(_location.value!!)
                _location.value = null
                _notification.value = "Удалено"
            }
        }
    }

    private suspend fun insertLocation(location: Location) {
        return withContext(Dispatchers.IO) {
            database.insert(location)
        }
    }

    private suspend fun getLocation(): Location? {
        return withContext(Dispatchers.IO) {
            database.getLocation(latitude, longitude)
        }
    }

    private suspend fun deleteLocation(location: Location) {
        return withContext(Dispatchers.IO) {
            database.delete(location)
        }
    }

    fun getLocationWeatherInformation(latitude: Double, longitude: Double, locationName: String) {
        this.locationName = locationName
        this.latitude = latitude
        this.longitude = longitude
        viewModelScope.launch {
            val setUp =
                OpenWeatherApi.retrofitService.getWeatherDeatilByCoordinatesAsync(
                    latitude, longitude, "ru"
                )
            try {
                val response = setUp.await()
                _currentWeatherInformation.value = parseCurrentWeatherInformation(response)
                _listOfHourlyWeatherInformation.value =
                    parseListHourlyWeatherInformation(response)
                _listOfDailyWeatherInformation.value =
                    parseListDailyWeatherInformation(response)
            } catch (t: Throwable) {

            }
        }
    }

    private fun parseCurrentWeatherInformation(response: WeatherDetailResponse): CurrentWeatherInformation {
        val currentHourWeatherInformation: Hourly? = response.hourly.find {
            response.currentWeatherDetail.datetime - it.datetime < ONE_HOUR
        }

        var chanceOfRain: Int? = null
        var chanceOfSnow: Int? = null

        if (currentHourWeatherInformation!!.snow != null) {
            chanceOfSnow = (currentHourWeatherInformation.pop * 100).toInt()
        } else {
            chanceOfRain = (currentHourWeatherInformation.pop * 100).toInt()
        }

        val todayWeatherInformation = response.daily[0]

        val temperature = response.currentWeatherDetail.temp

        val maxTemperature = todayWeatherInformation.temperatureDetail.max

        val minTemperature = todayWeatherInformation.temperatureDetail.min

        val pressure = response.currentWeatherDetail.pressure

        val speed = response.currentWeatherDetail.windSpeed

        return CurrentWeatherInformation(
            locationName = locationName,
            temperature = temperature,
            sunrise = response.currentWeatherDetail.sunrise,
            sunset = response.currentWeatherDetail.sunset,
            chanceOfRain = chanceOfRain,
            chanceOfSnow = chanceOfSnow,
            humidity = response.currentWeatherDetail.humidity.toInt(),
            windSpeed = speed,
            pressure = pressure,
            uvi = response.currentWeatherDetail.uvi,
            windDegrees = response.currentWeatherDetail.windDegrees,
            visibility = response.currentWeatherDetail.visibility.toInt(),
            weatherStatus = response.currentWeatherDetail.weather[0],
            maxTemperature = maxTemperature,
            minTemperature = minTemperature
        )
    }

    private fun parseListHourlyWeatherInformation(response: WeatherDetailResponse): List<HourlyWeatherInformation> {
        val startTime = (System.currentTimeMillis() / 1000).toInt()
        val endTime = (System.currentTimeMillis() / 1000).toInt() + 86400

        val currentTime = (System.currentTimeMillis() / 1000)
        val sunSet = response.currentWeatherDetail.sunset
        val sunRise = response.currentWeatherDetail.sunrise

        val moment = HourlyWeatherInformation(
            datetime = currentTime,
            type = TimeStage.Now,
            chanceOfRain = null,
            chanceOfSnow = null,
            temperature = null,
            icon = when (currentTime) {
                in sunRise..sunSet -> "day"
                else -> "night"
            }
        )

        val todaySunrise = HourlyWeatherInformation(
            datetime = response.currentWeatherDetail.sunrise,
            type = TimeStage.Sunrise,
            chanceOfRain = null,
            chanceOfSnow = null,
            temperature = null,
            icon = "sunrise"
        )

        val todaySunset = HourlyWeatherInformation(
            datetime = response.currentWeatherDetail.sunset,
            type = TimeStage.Sunset,
            chanceOfRain = null,
            chanceOfSnow = null,
            temperature = null,
            icon = "sunset"
        )

        val tomorrowSunrise = HourlyWeatherInformation(
            datetime = response.daily[1].sunrise,
            type = TimeStage.Sunrise,
            chanceOfRain = null,
            chanceOfSnow = null,
            temperature = null,
            icon = "sunrise"
        )

        val tomorrowSunset = HourlyWeatherInformation(
            datetime = response.daily[1].sunset,
            type = TimeStage.Sunset,
            chanceOfRain = null,
            chanceOfSnow = null,
            temperature = null,
            icon = "sunset"
        )

        val list: List<HourlyWeatherInformation> = response.hourly.map {
            var chanceOfRain: Double? = null
            var chanceOfSnow: Double? = null

            if (it.rain != null) {
                chanceOfRain = it.pop
                chanceOfSnow = null
            } else if (it.snow != null) {
                chanceOfSnow = it.pop
                chanceOfRain = null
            }

            val icon: String
            if (it.datetime < todaySunrise.datetime
                || it.datetime >= todaySunset.datetime && it.datetime < tomorrowSunrise.datetime
            ) {
                icon = when (it.weather[0].id) {
                    in 200..232 -> "thunderstorm"
                    in 300..321 -> "mist_night"
                    in 500..531 -> "rain"
                    in 600..622 -> "snow"
                    in 700..781 -> "mist_night"
                    800 -> "night"
                    else -> "cloudy_night"
                }
            } else {
                icon = when (it.weather[0].id) {
                    in 200..232 -> "thunderstorm"
                    in 300..321 -> "mist_day"
                    in 500..531 -> "rain"
                    in 600..622 -> "snow"
                    in 700..781 -> "mist_day"
                    800 -> "day"
                    else -> "cloudy_day"
                }
            }


            val temperature = it.temp

            HourlyWeatherInformation(
                datetime = it.datetime,
                type = TimeStage.Normal,
                temperature = temperature,
                icon = icon,
                chanceOfSnow = chanceOfSnow,
                chanceOfRain = chanceOfRain
            )
        }

        val result = list.plus(
            listOf(
                tomorrowSunrise, todaySunrise, todaySunset, tomorrowSunset, moment
            )
        )

        return result.sortedBy { it.datetime }.filter {
            it.datetime in startTime until endTime
        }

    }

    private fun parseListDailyWeatherInformation(response: WeatherDetailResponse): List<DailyWeatherInformation> {
        return response.daily.map {
            var chanceOfRain: Double? = null
            var chanceOfSnow: Double? = null

            if (it.rain != null) {
                chanceOfRain = it.pop
                chanceOfSnow = null
            } else if (it.snow != null) {
                chanceOfSnow = it.pop
                chanceOfRain = null
            }

            val icon = when (it.weather[0].id) {
                in 200..232 -> "thunderstorm"
                in 300..321 -> "mist_day"
                in 500..531 -> "rain"
                in 600..622 -> "snow"
                in 700..781 -> "mist_day"
                800 -> "day"
                else -> "cloudy_day"
            }


            DailyWeatherInformation(
                datetime = it.datetime,
                chanceOfRain = chanceOfRain,
                chanceOfSnow = chanceOfSnow,
                icon = icon,
                temperatureMax = it.temperatureDetail.max,
                temperatureMin = it.temperatureDetail.min
            )
        }.sortedBy { it.datetime }
    }

    fun getShareText(): String? {
        if (currentWeatherInformation.value == null) {
            return null
        } else {
            val results: MutableList<String> = mutableListOf()

            val information = currentWeatherInformation.value!!
            val temperatureUnit = settings.temperatureUnit

            val locationName = information.locationName
            val currentTemperature = information.temperature

            results.add("${locationName}'s current temperature is $currentTemperature,")
            results.add("weather is ${information.weatherStatus.description}.")

            val sunrise =
                SimpleDateFormat(
                    "HH:MM",
                    Locale.ENGLISH
                ).format(Date(information.sunrise * 1000))
            val sunset =
                SimpleDateFormat(
                    "HH:MM",
                    Locale.ENGLISH
                ).format(Date(information.sunset * 1000))

            results.add("Sunrise at $sunrise, sunset at $sunset.")

            val temperatureMax =
                getTemperatureText(
                    convertTemperature(information.maxTemperature, temperatureUnit),
                    temperatureUnit
                )
            val temperatureMin =
                getTemperatureText(
                    convertTemperature(information.minTemperature, temperatureUnit),
                    temperatureUnit
                )

            results.add("Minimum temperature near $temperatureMin. Maximum temperature near $temperatureMax.")

            if (information.chanceOfSnow != null) {
                results.add("Chance of snow is ${information.chanceOfSnow}%.")
            } else {
                results.add("Chance of rain is ${information.chanceOfRain!!}%.")
            }

            results.add("Humidity is ${information.humidity}%, UV index is ${information.uvi}")
            results.add("and visibility is ${information.visibility / 1000}km.\nFrom Weather App")
            return results.joinToString(" ")
        }
    }
}
