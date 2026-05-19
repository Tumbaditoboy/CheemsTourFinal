package mx.itson.cheemstour.utils

import com.google.gson.GsonBuilder
import mx.itson.cheemstour.interfaces.CheemsAPI
import mx.itson.cheemstour.interfaces.OpenWeatherAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtil {


    private val gson = GsonBuilder().create()

    /**
     * Retorna el cliente para la API que se encuentra en Render
     */
    fun getApi() : CheemsAPI {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://cheemsapi.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(CheemsAPI::class.java)
    }

    /**
     * Retorna el cliente para la API de OpenWeather
     */
    fun getApiWeather() : OpenWeatherAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(OpenWeatherAPI::class.java)
    }
}