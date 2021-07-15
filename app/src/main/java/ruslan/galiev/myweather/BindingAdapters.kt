package ruslan.galiev.myweather

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ruslan.galiev.myweather.responses.RelatedNameLocation
import ruslan.galiev.myweather.setting.PressureUnit
import ruslan.galiev.myweather.setting.SpeedUnit
import ruslan.galiev.myweather.setting.TemperatureUnit
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter(value = ["temperature", "temperatureUnit"], requireAll = true)
fun TextView.setTemperatureFormatted(temperature: Double, unit: TemperatureUnit) = temperature.let {
    text = getTemperatureText(convertTemperature(temperature, unit), unit)
}

@BindingAdapter("time")
fun TextView.setFormattedTime(timestamp: Long) = timestamp.let {
    text = SimpleDateFormat("HH:MM", Locale.ENGLISH).format(Date(timestamp * 1000))
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["chanceOfRain", "chanceOfSnow"], requireAll = true)
fun TextView.setProbabilityOfPrecipitation(chanceOfRain: Int?, chanceOfSnow: Int?) {
    text = if (chanceOfSnow != null) {
        "$chanceOfSnow%"
    } else {
        "$chanceOfRain%"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["windSpeed", "windDegrees", "speedUnit"], requireAll = true)
fun TextView.setFormattedWind(windSpeed: Double, windDegrees: Double, speedUnit: SpeedUnit) {
    text = when {
        windDegrees < 11.25 -> "C"
        windDegrees >= 11.25 && windDegrees < 33.75 -> "CCB"
        windDegrees >= 33.75 && windDegrees < 56.25 -> "СВ"
        windDegrees >= 56.25 && windDegrees < 78.75 -> "ВСВ"
        windDegrees >= 78.75 && windDegrees < 101.25 -> "В"
        windDegrees >= 101.25 && windDegrees < 123.75 -> "ВЮВ"
        windDegrees >= 123.75 && windDegrees < 146.25 -> "ЮВ"
        windDegrees >= 146.25 && windDegrees < 168.75 -> "ЮЮВ"
        windDegrees >= 168.75 && windDegrees < 191.25 -> "Ю"
        windDegrees >= 191.25 && windDegrees < 213.75 -> "ЮЮЗ"
        windDegrees >= 213.75 && windDegrees < 236.25 -> "ЮЗ"
        windDegrees >= 236.25 && windDegrees < 258.75 -> "ЗЮЗ"
        windDegrees >= 258.75 && windDegrees < 281.25 -> "З"
        windDegrees >= 281.25 && windDegrees < 303.75 -> "ЗСЗ"
        windDegrees >= 303.75 && windDegrees < 326.25 -> "СЗ"
        windDegrees >= 326.25 && windDegrees < 348.75 -> "ССЗ"
        else -> "С"
    } + " " + getSpeedText(convertSpeed(windSpeed, speedUnit), speedUnit)
}


@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["pressure", "pressureUnit"], requireAll = true)
fun TextView.setFormattedPressure(pressure: Double, unit: PressureUnit) {
    text = getPressureText(convertPressure(pressure, unit), unit)
}

@SuppressLint("SetTextI18n")
@BindingAdapter("relatedNameLocation")
fun TextView.setRelatedNameLocation(relatedNameLocation: RelatedNameLocation) {
    text = if (relatedNameLocation.state != null) {
        "${relatedNameLocation.state}, ${relatedNameLocation.country}"
    } else {
        relatedNameLocation.country
    }
}
