package mx.itson.cheemstour

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.utils.RetrofitUtil
import mx.itson.cheemstour.utils.vibratePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class TripFormActivity : AppCompatActivity() {

    // Variable para saber si se está editando un trip existente
    private var existingTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_form)

        // Vinculación de los campos de texto y botón del formulario
        val etName = findViewById<EditText>(R.id.text_name)
        val etCity = findViewById<EditText>(R.id.text_city)
        val etLat = findViewById<EditText>(R.id.text_latitude)
        val etLon = findViewById<EditText>(R.id.text_longitude)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // Intentar recibir el objeto completo para editarlo
        existingTrip = intent.getSerializableExtra("trip_editar") as? Trip

        if (existingTrip != null) {
            // Rellenar campos con la información existente
            etName.setText(existingTrip?.name)
            etCity.setText(existingTrip?.city)
            etLat.setText(existingTrip?.latitude.toString())
            etLon.setText(existingTrip?.longitude.toString())
            btnSave.text = getString(R.string.update_btn)
            Log.i("FormMode", "Editando viaje ID: ${existingTrip?.id}")
        } else {
            //  Modo Creación: Recibir solo coordenadas del mapa para que los otros campos sean llenados por el usuario
            val latRecibida = intent.getDoubleExtra("latitud", 0.0)
            val lonRecibida = intent.getDoubleExtra("longitud", 0.0)

            if (latRecibida != 0.0 && lonRecibida != 0.0) {
                etLat.setText(latRecibida.toString())
                etLon.setText(lonRecibida.toString())
            }
            Log.i("FormMode", "Creando nuevo viaje")
        }

        // Evento click para validar los datos e iniciar el proceso de guardado o de edición
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val city = etCity.text.toString()
            val latStr = etLat.text.toString()
            val lonStr = etLon.text.toString()

            // Validación de campos vacíos
            if (name.isEmpty() || city.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.data_unvalid_form), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Conversión de coordenadas a formato numérico decimal
                val latValue = latStr.toDouble()
                val lonValue = lonStr.toDouble()

                if (existingTrip != null) {
                    // Actualizar (put)
                    val updatedTrip = Trip(existingTrip!!.id, name, city, latValue, lonValue)
                    updateDB(updatedTrip)
                } else {
                    // Guardar (post)
                    val newTrip = Trip(0, name, city, latValue, lonValue)
                    saveDB(newTrip)
                }

            } catch (e: Exception) {
                // Captura errores de conversión si las coordenadas no son números válidos
                Toast.makeText(this, getString(R.string.data_unvalid_form), Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * Envía una petición POST por medio de Retrofit para registrar un nuevo viaje en la base de datos
     */
    private fun saveDB(trip: Trip) {
        val call = RetrofitUtil.getApi().saveTrip(trip)
        executeCall(call, getString(R.string.save_msg), null)
    }

    /**
     * Envía una petición PUT vía Retrofit para actualizar un registro existente por medio de su ID
     */
    private fun updateDB(trip: Trip) {
        // Usamos la función updateTrip que agregamos a la interface CheemsAPI
        val call = RetrofitUtil.getApi().updateTrip(trip.id, trip)
        // Pasamos el objeto 'trip' modificado para que executeCall pueda retornarlo al Activity anterior
        executeCall(call, getString(R.string.update_msg), trip)
    }

    /**
     * Método adaptado para retornar el objeto modificado en caso de éxito
     */
    private fun executeCall(call: Call<Boolean>, successMsg: String, tripToReturn: Trip?) {
        // Ejecuta la petición de forma asíncrona
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                // Verifica que el servidor de Render haya procesado la transacción con éxito
                if (response.isSuccessful && response.body() == true) {
                    Toast.makeText(this@TripFormActivity, successMsg, Toast.LENGTH_LONG).show()

                    //Vibración si el servidor emite una respuesta exitosa
                    vibratePhone()
                    // Si se trata de un viaje modificado, se envia de vuelta mediante el intent de resultado
                    if (tripToReturn != null) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("trip_actualizado", tripToReturn)
                        setResult(Activity.RESULT_OK, resultIntent)
                        vibratePhone()
                    } else {
                        // Envía una señal de éxito estándar sin adjuntar objetos si se trata de un trip nuevo
                        setResult(Activity.RESULT_OK)
                    }

                    // Cierra el formulario y regresa a la actividad anterior
                    finish()
                } else {
                    Toast.makeText(this@TripFormActivity, "Error en el servidor al procesar datos", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                // Registra el error en Logcat e informa si hay un fallo de red o tiempo de espera agotado
                Log.e("API_ERROR", t.message.toString())
                Toast.makeText(this@TripFormActivity, "Fallo de conexión con el servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}