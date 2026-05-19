package mx.itson.cheemstour.interfaces

import mx.itson.cheemstour.entities.Weather
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface OpenWeatherAPI {

    /**
     * Obtiene el clima actual basado en coordenadas de latitud/longitud.
     * @param lat Latitud del marker seleccionado.
     * @param lon Longitud del marker seleccionado.
     * @param appid API KEY de OpenWeather.
     * @param units Sistema de unidades (metric = Celsius).
     * @param lang Idioma de la respuesta.
     */
    @GET("weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String
    ): Call<Weather>

}