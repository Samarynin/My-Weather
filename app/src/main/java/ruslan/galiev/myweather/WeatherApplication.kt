package ruslan.galiev.myweather

import android.app.Application

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {

        }
    }
}