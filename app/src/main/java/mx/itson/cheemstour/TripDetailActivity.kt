package mx.itson.cheemstour

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.entities.Weather
import mx.itson.cheemstour.utils.RetrofitUtil
import mx.itson.cheemstour.utils.vibratePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import mx.itson.cheemstour.BuildConfig

class TripDetailActivity : AppCompatActivity() {

    private lateinit var trip: Trip

    private val API_KEY = BuildConfig.WEATHER_API_KEY

    // Vistas de la interfaz gráfica
    private lateinit var tvName: TextView
    private lateinit var tvCity: TextView
    private lateinit var rootLayout: LinearLayout

    // Abre el formulario y captura el objeto actualizado al retornar
    private val editTripLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val tripEditado = data?.getSerializableExtra("trip_actualizado") as? Trip

            if (tripEditado != null) {
                // Reemplaza los datos locales con la nueva información del formulario
                this.trip = tripEditado

                // Refresca la pantalla y vibrar
                updateUIWithTripData()
                vibratePhone()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        // Recupera el objeto enviado desde el mapa principal
        trip = intent.getSerializableExtra("trip_objeto") as Trip

        // Vinculación de las vistas del layout
        tvName = findViewById(R.id.tv_detail_name)
        tvCity = findViewById(R.id.tv_detail_city)
        rootLayout = findViewById(R.id.layout_root_detail)
        val btnDelete = findViewById<Button>(R.id.btn_delete_trip)
        val btnEdit = findViewById<Button>(R.id.btn_edit_trip)

        // Renderiza la información inicial del viaje
        updateUIWithTripData()

        // Petición para eliminar el registro
        btnDelete.setOnClickListener {
            deleteTrip(trip.id)
        }

        // Envía el objeto actual al formulario esperando recibir los cambios de vuelta
        btnEdit.setOnClickListener {
            val intent = Intent(this, TripFormActivity::class.java)
            intent.putExtra("trip_editar", trip)
            editTripLauncher.launch(intent)
        }

        Log.i("DetailView", "Viendo detalles de ID: ${trip.id}")
    }

    /**
     * Muestra los textos informativos en pantalla e inicia la petición del clima
     */
    private fun updateUIWithTripData() {
        tvName.text = trip.name
        tvCity.text = trip.city

        // Consulta del clima basada en las coordenadas del marker
        fetchWeather(trip.latitude, trip.longitude)
    }

    /**
     * Toma los datos del servidor de OpenWeather y distribuye los valores especificos en sys respectivas variables en la UI
     */
    private fun fetchWeather(lat: Double, lon: Double) {
        // Obtiene el idioma actual en el sistema del dispositivo
        val currentLanguage = Locale.getDefault().language

        // Prepara la petición HTTP con el sistema métrico en grados celsius y el idioma del dispositivo
        val call = RetrofitUtil.getApiWeather().getWeather(lat, lon, API_KEY, "metric", currentLanguage)

        call.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    val offset = weather.timezone

                    // Vinculación de componentes para el apartado meteorológico
                    val tvTemp = findViewById<TextView>(R.id.tv_weather_temp)
                    val tvDesc = findViewById<TextView>(R.id.tv_weather_desc)
                    val ivIcon = findViewById<ImageView>(R.id.iv_weather_icon)

                    val tvTempMax = findViewById<TextView>(R.id.tv_detail_temp_max)
                    val tvTempMin = findViewById<TextView>(R.id.tv_detail_temp_min)
                    val tvHumidity = findViewById<TextView>(R.id.tv_detail_humidity)
                    val tvWind = findViewById<TextView>(R.id.tv_detail_wind)
                    val tvSunrise = findViewById<TextView>(R.id.tv_detail_sunrise)
                    val tvSunset = findViewById<TextView>(R.id.tv_detail_sunset)
                    val tvLocalTime = findViewById<TextView>(R.id.tv_detail_local_time)

                    // Cambia la tonalidad del fondo según la temperatura actual, llama a la función ubdateBackgroundColor declarada abajo
                    val currentTemp = weather.main?.temperature?.toInt() ?: 0
                    updateBackgroundColor(rootLayout, currentTemp)

                    tvTemp.text = "$currentTemp°C"

                    // Formatea la primera letra de la descripción a mayúscula
                    tvDesc.text = weather.details?.get(0)?.description?.replaceFirstChar { it.uppercase() }

                    // Concatenación de métricas climáticas usando recursos de Strings
                    tvTempMax.text = getString(R.string.temp_max) + "${weather.main?.tempMax?.toInt()}°C"
                    tvTempMin.text = getString(R.string.temp_min) + "${weather.main?.tempMin?.toInt()}°C"
                    tvHumidity.text = getString(R.string.humidity) + "${weather.main?.humidity}%"

                    val windDir = getWindDirection(weather.wind?.degree ?: 0)
                    tvWind.text = getString(R.string.wind) + "${weather.wind?.speed} m/s ($windDir)"

                    // Conversión del tiempo en formato unix a formato de hora local para que sea legible
                    tvLocalTime.text =  getString(R.string.local_time) + "${formatLocalTime(weather.dt, offset)}"
                    tvSunrise.text = getString(R.string.sunrise)  + "${formatLocalTime(weather.sys?.sunrise ?: 0, offset)}"
                    tvSunset.text = getString(R.string.sunset) + "${formatLocalTime(weather.sys?.sunset ?: 0, offset)}"

                    // Se utiliza Picasso para descargar y renderizar el icono de forma asíncrona
                    val iconCode = weather.details?.get(0)?.icon
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"

                    Picasso.get()
                        .load(iconUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(ivIcon)

                } else {
                    Log.e("WeatherAPI", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.e("WeatherAPI", "Fallo: ${t.message}")
                findViewById<TextView>(R.id.tv_weather_desc).text = getString(R.string.weather_not_available)
            }
        })
    }

    /**
     * Modifica el color del layout de TripDetail dependiendo de la temperatura actual del lugar
     */
    private fun updateBackgroundColor(layout: LinearLayout, temp: Int) {
        val colorHex = when {
            temp <= 15 -> "#BBDEFB" // Clima Frío menor o igual a 15 grados  (Azul)
            temp >= 28 -> "#FFE0B2" // Clima Cálido mayor o igual a 28 grados (Naranja)
            else -> "#F5F5F5"       // Clima Templado o neutro (Gris)
        }
        layout.setBackgroundColor(Color.parseColor(colorHex))
    }

    /**
     * Traduce los segundos UNIX y el desfase de zona horaria a formato HH:mm en base UTC
     */
    private fun formatLocalTime(unixTime: Long, timezoneOffset: Int): String {
        if (unixTime == 0L) return "--:--"
        val date = Date((unixTime + timezoneOffset) * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * Mapea los grados azimutales del viento hacia los recursos de texto en formato rosa de los vientos
     */
    private fun getWindDirection(deg: Int): String {
        return when (deg) {
            in 338..360, in 0..22 -> getString(R.string.north_wind)
            in 23..67 -> getString(R.string.wind_northeast)
            in 68..112 -> getString(R.string.wind_east)
            in 113..157 -> getString(R.string.wind_southeast)
            in 158..202 -> getString(R.string.wind_south)
            in 203..247 -> getString(R.string.wind_southwest)
            in 248..292 -> getString(R.string.wind_west)
            in 293..337 -> getString(R.string.wind_northwest)
            else -> "N/A"
        }
    }

    /**
     * Envía una petición DELETE por Retrofit para remover el viaje de la base de datos
     */
    private fun deleteTrip(id: Int) {
        val call = RetrofitUtil.getApi().deleteTrip(id)
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful && response.body() == true) {

                    //Vibración en caso de éxito, notifica por el toast y cierra la pantalla actual
                    vibratePhone()
                    Toast.makeText(this@TripDetailActivity, getString(R.string.trip_canceled_msg), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TripDetailActivity, getString(R.string.not_deleted), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}")
            }
        })
    }
}