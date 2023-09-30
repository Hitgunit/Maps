package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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
    //
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    //
    private var updateCount = 0




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
        locationRequest = LocationRequest.create().apply {
            interval = 20000  // Establece la tasa de actualización en 20 segundos.
            fastestInterval = 10000  // Establece la tasa más rápida a 10 segundos si otros aplicativos solicitan actualizaciones más rápido.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.let {
                    for (location in it.locations) {
                        val ubicacionExacta = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(ubicacionExacta).title("Mi ubicación")
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionExacta, 15f))

                        updateCount++
                        if (updateCount >= 15) {
                            localizacion.removeLocationUpdates(this)
                            // Aquí puedes añadir cualquier acción adicional que quieras realizar.
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            localizacion.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }



}
