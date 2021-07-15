package ruslan.galiev.myweather.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ruslan.galiev.myweather.responses.CurrentWeatherApiResponse
import ruslan.galiev.myweather.responses.RelatedNameLocation
import ruslan.galiev.myweather.responses.WeatherDetailResponse

private const val BASE_URL = "https://api.openweathermap.org"
private const val API_KEY = "abde8c55fa65b0e555d4597036a34dc6"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface OneCallApiService {
    @GET("data/2.5/onecall")
    fun getWeatherDeatilByCoordinatesAsync(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String,
        @Query("appid") APP_ID: String = API_KEY,
        @Query("exclude") exclude: String = "minutely,alerts"
    ): Deferred<WeatherDetailResponse>

    @GET("data/2.5/weather")
    fun getCurrentWeatherByCoordinatesAsync(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String,
        @Query("appid") APP_ID: String = API_KEY
    ): Deferred<CurrentWeatherApiResponse>

    @GET("data/2.5/weather")
    fun getCurrentWeatherByCityIDAsync(
        @Query("id") id: Int,
        @Query("lang") language: String,
        @Query("appid") APP_ID: String = API_KEY
    ): Deferred<CurrentWeatherApiResponse>

    @GET("geo/1.0/direct")
    fun getLocationByNameAsync(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") APP_ID: String = API_KEY
    ): Deferred<List<RelatedNameLocation>>
}

object OpenWeatherApi {
    val retrofitService: OneCallApiService by lazy {
        retrofit.create(OneCallApiService::class.java)
    }
}