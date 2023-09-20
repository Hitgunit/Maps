package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    //Variable que guarda maps
    private lateinit var mMap: GoogleMap
    //Obtiene la informacion de la ubicacion
    private lateinit var localizacion: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Se inicializa la obtencion de localizacion
        localizacion = LocationServices.getFusedLocationProviderClient(this)

        // Obtención del mapa de XML (Fragment)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //se incia cuando el mapa esta listo
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        //Verifica y obtiene los permisos de ubicacion
        Permisos()
    }

    //Verificacion de permisos
    private fun Permisos() {
        //Verifica si se le dio permiso de ubicacion
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //Si no tiene persmisos se le pide la usuario los permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }else {
            //Si el permiso esta concedido, entonces procede a solicitar la ubicacion actual
            ObtenerUbicacion()
        }
    }

    //maneja la respuesta de la solicitud de ubicacion (Se llama automaticamente)
    override fun onRequestPermissionsResult(
        //Inidica la solicitud de permiso se planea hacer
        requestCode: Int,
        //Guarda los nombres de los permisos solicitados
        permissions: Array<String>,
        //Guarda los resultados de los permisos
        grantResults: IntArray
    ) {
        // Llama al método super para sobreescribir
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //El valor 1 representa la solicitud para la ubicacion
        when (requestCode) {
            1 -> {
                //Verifica si el permiso fue concedido por el usuario
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Si fue el caso entonces inicia la corrutina
                    CoroutineScope(Dispatchers.Main).launch {
                        ObtenerUbicacion()
                    }
                } else {
                    //Si no fue el caso, entonces le notifica al usuario que no puede funcionar sin el permiso
                    Toast.makeText(this, "La aplicación no puede funcionar sin él permiso", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    //
    private fun ObtenerUbicacion() {
        //Se inicia la corrutina
        CoroutineScope(Dispatchers.Main).launch {
            //Permite saber al programa cuantas veces se repetira
            var contador = 0
            //se repite hasta que llegue a 15 veces, ya que cada bucle finaliza depsues de 20 seg (20 * 15 = 300seg) que son 5 minutos
            while (contador < 15) {
                // Vuelve a checar los permisos ya que android lo pide asi
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //Se obtiene la ultima ubicacion del usuario
                    localizacion.lastLocation.addOnSuccessListener { location: Location ->
                        //Guarda la ubicacion
                        val ubicacionExacta = LatLng(location.latitude, location.longitude)
                        //Agrega un marker en el mapa
                        mMap.addMarker(
                            MarkerOptions().position(ubicacionExacta).title("Mi ubicación")
                        )
                        //Se mueve la camara a esa ubicacion
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionExacta, 15f))
                    }
                    //Tiene un delay de 20 segundos para repetir
                    delay(20000)
                    contador++
                } else {
                    // Rompe el loop si no se dan los permisos
                    break
                }
            }
        }
    }

}
