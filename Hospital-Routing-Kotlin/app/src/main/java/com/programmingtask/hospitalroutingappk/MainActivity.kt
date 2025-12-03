package com.programmingtask.hospitalroutingappk

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var database: DatabaseReference
    private lateinit var tvLocation: TextView

    private var myMarker: Marker? = null
    private var friendMarker: Marker? = null
    private var polylineRoute: Polyline? = null

    // Cambia esto por un ID único (puedes usar Android ID, o Firebase Auth UID)
    private val myUserId = "user_1"           // Tú
    private val friendUserId = "user_2"       // La otra persona

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvLocation = findViewById(R.id.tvLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance().reference.child("users")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        startLocationUpdates()
        listenToFriendLocation()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                // 1. Actualizo mi posición en el mapa
                if (myMarker == null) {
                    myMarker = map.addMarker(MarkerOptions()
                        .position(latLng)
                        .title("Yo")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))!!
                } else {
                    myMarker?.position = latLng
                }

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                tvLocation.text = "Lat: ${location.latitude}\nLng: ${location.longitude}"

                // 2. Subo mi ubicación a Firebase
                val userData = mapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to ServerValue.TIMESTAMP
                )
                database.child(myUserId).setValue(userData)

                // 3. Redibujar ruta cada vez que ME muevo
                friendMarker?.position?.let { friendPos ->
                    drawRoute(latLng, friendPos)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Escucha la ubicación del amigo en tiempo real
    private fun listenToFriendLocation() {
        database.child(friendUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java)
                val lng = snapshot.child("lng").getValue(Double::class.java)

                if (lat != null && lng != null) {
                    val friendLatLng = LatLng(lat, lng)

                    if (friendMarker == null) {
                        friendMarker = map.addMarker(MarkerOptions()
                            .position(friendLatLng)
                            .title("Amigo")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))!!
                    } else {
                        friendMarker?.position = friendLatLng
                    }

                    // Redibujar ruta cuando el AMIGO se mueve
                    myMarker?.position?.let { myPos ->
                        drawRoute(myPos, friendLatLng)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error Firebase: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun drawRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            val call = service.getRoute(
                "${start.latitude},${start.longitude}",
                "${end.latitude},${end.longitude}",
                getString(R.string.google_maps_key)
            )

            if (call.isSuccessful) {
                val points = call.body()?.routes?.firstOrNull()
                    ?.overviewPolyline?.points ?: return@launch

                val decoded = PolyUtil.decode(points)

                runOnUiThread {
                    polylineRoute?.remove()
                    polylineRoute = map.addPolyline(
                        PolylineOptions().addAll(decoded).color(Color.RED).width(16f)
                    )

                    // Distancia y tiempo (lo muestro en un TextView o Toast)
                    val distance = call.body()?.routes?.firstOrNull()?.legs?.firstOrNull()?.distance?.text
                    val duration = call.body()?.routes?.firstOrNull()?.legs?.firstOrNull()?.duration?.text

                    Toast.makeText(
                        this@MainActivity,
                        "Ruta actualizada → $distance • $duration",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}