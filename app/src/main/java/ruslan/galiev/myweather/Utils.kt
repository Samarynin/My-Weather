package ruslan.galiev.myweather

import ruslan.galiev.myweather.setting.PressureUnit
import ruslan.galiev.myweather.setting.SpeedUnit
import ruslan.galiev.myweather.setting.TemperatureUnit
import java.text.DecimalFormat

fun convertTemperature(temperature: Double, unit: TemperatureUnit): Int {
    return when (unit) {
        TemperatureUnit.Kelvin -> temperature.toInt()
        TemperatureUnit.Celsius -> (temperature - 273.15).toInt()
        TemperatureUnit.Fahrenheit -> ((temperature - 273.15) * 9 / 5 + 32).toInt()
    }
}

fun getTemperatureText(temperature: Int, unit: TemperatureUnit): String {
    return when (unit) {
        TemperatureUnit.Kelvin -> "$temperature°K"
        TemperatureUnit.Celsius -> "$temperature℃"
        TemperatureUnit.Fahrenheit -> "$temperature℉"
    }
}

fun convertPressure(pressure: Double, unit: PressureUnit): Int {
    return when (unit) {
        PressureUnit.hPa -> pressure.toInt()
        PressureUnit.Pa -> (pressure * 100).toInt()
    }
}

fun getPressureText(pressure: Int, unit: PressureUnit): String {
    return when (unit) {
        PressureUnit.hPa -> "$pressure гПа"
        PressureUnit.Pa -> "$pressure Па"
    }
}

fun convertSpeed(speed: Double, unit: SpeedUnit): Double {
    return when (unit) {
        SpeedUnit.metresPerSecond -> speed
        SpeedUnit.kilometersPerSHour -> speed * 3.6
        SpeedUnit.milesPerHour -> speed * 2.237
    }
}

fun getSpeedText(speed: Double, unit: SpeedUnit): String {
    val df = DecimalFormat("#.#")
    return when (unit) {
        SpeedUnit.metresPerSecond -> "${df.format(speed)} м/с"
        SpeedUnit.kilometersPerSHour -> "${df.format(speed)} км/ч"
        SpeedUnit.milesPerHour -> "${df.format(speed)} миль/ч"
    }
}