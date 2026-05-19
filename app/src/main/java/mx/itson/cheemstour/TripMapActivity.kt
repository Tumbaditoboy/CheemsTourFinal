package mx.itson.cheemstour

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import mx.itson.cheemstour.entities.Trip
import mx.itson.cheemstour.utils.RetrofitUtil
import mx.itson.cheemstour.utils.vibratePhone
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TripMapActivity : AppCompatActivity(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var newMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }



    fun getTrips() {
        val call: Call<List<Trip>> = RetrofitUtil.getApi().getTrips()
        call.enqueue(object : Callback<List<Trip>> {
            override fun onResponse(call: Call<List<Trip>>, response: Response<List<Trip>>) {
                if (response.isSuccessful && response.body() != null) {
                    val trips: List<Trip> = response.body()!!
                   map?.clear() //Limpiar para evitar duplicados al refrescar

                    trips.forEach { t ->
                        val latLng = LatLng(t.latitude, t.longitude)
                       val marker = map?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(t.name)
                                .snippet(t.city)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cheems))
                        )
                        //agregado
                        marker?.tag = t
                        Log.d("MapDebug", "Marcador añadido: ${t.name} en ID: ${t.id}")
                    }
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Log.e("Error API", t.message.toString())
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        val userCity = LatLng(27.9183, -110.8980)

        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userCity, 15f))

        getTrips()

        // Dar un click para mostrar el marcador
        map!!.setOnMapClickListener { latLng ->
            // se quita algún marcador previo
            newMarker?.remove()

            // Creamos el nuevo marcador y lo guardamos en la variable global
            newMarker = map!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.drag_msg))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
            )

            newMarker?.showInfoWindow()
        }

        // Evento cuando se hace click en un marcador existente
        map!!.setOnMarkerClickListener { marker ->
            // Si el marcador tiene un objeto Trip guardado en el tag...
            if (marker.tag is Trip) {
                val tripSelected = marker.tag as Trip

                // Log de información
                Log.i("Navigation", "Navegando a detalles de: ${tripSelected.name}")

                // Abrir la actividad de detalles (la crearemos a continuación)
                val intent = Intent(this@TripMapActivity, TripDetailActivity::class.java)
                intent.putExtra("trip_objeto", tripSelected) // Pasamos el objeto completo
                startActivity(intent)
            }
            false // Devolvemos false para que se muestre el InfoWindow normal
        }

        // Evento para arrastrar el marcador
        map!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {
                // Mientras se mueve
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // Cuando el usuario suelta el marcador
                val posicionFinal = marker.position

                Toast.makeText(this@TripMapActivity, getString(R.string.location_set_msg), Toast.LENGTH_SHORT).show()

                vibratePhone()

                // Se abre el formulario cargando los datos de la posición final
                val intent = Intent(this@TripMapActivity, TripFormActivity::class.java)
                intent.putExtra("latitud", posicionFinal.latitude)
                intent.putExtra("longitud", posicionFinal.longitude)
                startActivity(intent)
            }
        })
    }

    //Refrescar
    override fun onResume() {
        super.onResume()
        if (map != null) getTrips()
    }
}