package ruslan.galiev.myweather.detail

data class DailyWeatherInformation(
    val datetime: Long,
    val chanceOfRain: Double?,
    val chanceOfSnow: Double?,
    val icon: String,
    val temperatureMax: Double,
    val temperatureMin: Double
)