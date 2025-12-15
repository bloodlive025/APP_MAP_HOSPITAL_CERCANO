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
import java.util.Calendar

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var database: DatabaseReference
    private lateinit var centrosRef: DatabaseReference
    private lateinit var tvLocation: TextView

    private var myMarker: Marker? = null
    private var friendMarker: Marker? = null
    private var polylineRoute: Polyline? = null
    private var routeToHospital: Polyline? = null
    
    private val hospitalMarkers = mutableListOf<Pair<CentroMedico, Marker>>()

    // Cambia esto por un ID único (puedes usar Android ID, o Firebase Auth UID)
    private val myUserId = "user_1"           // Tú
    private val friendUserId = "user_2"       // La otra persona

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_routes)

        tvLocation = findViewById(R.id.tvLocation)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance().reference.child("users")
        centrosRef = FirebaseDatabase.getInstance().reference.child("CentrosMedicos")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        startLocationUpdates()
        listenToFriendLocation()
        loadHospitalMarkers()
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

                // 4. Intentar trazar ruta al hospital más cercano abierto
                tryRouteToNearestOpenHospital(latLng)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun loadHospitalMarkers() {
        centrosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpia markers anteriores de hospitales
                hospitalMarkers.forEach { it.second.remove() }
                hospitalMarkers.clear()

                for (child in snapshot.children) {
                    val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                    val nombre = child.child("nombre").getValue(String::class.java) ?: "Hospital"
                    val latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0
                    val longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0
                    val activo = child.child("activo").getValue(Boolean::class.java) ?: true

                    // Validar coordenadas
                    if (latitud == 0.0 || longitud == 0.0) continue

                    // Parsear especialidades
                    val especialidadesTexto = child.child("especialidadesTexto").getValue(String::class.java) ?: ""
                    val especialidades = especialidadesTexto.split(",").map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .let { ArrayList(it) }

                    // Parsear horarios
                    val horarios = parseHorarios(child)

                    // Crear objeto CentroMedico
                    val centro = CentroMedico(id, nombre, especialidades, horarios, latitud, longitud)

                    // Agregar marker al mapa
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(latitud, longitud))
                            .title(nombre)
                            .snippet(especialidades.joinToString(", "))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    )

                    if (marker != null) {
                        hospitalMarkers.add(centro to marker)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MapActivity,
                    "Error al cargar hospitales: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun parseHorarios(snapshot: DataSnapshot): ArrayList<HorarioAtencion> {
        val horarios = ArrayList<HorarioAtencion>()
        
        // Intentar leer horarios estructurados (0-6)
        for (i in 0..6) {
            val hNode = snapshot.child("horarioAtencion").child(i.toString())
            val horaInicio = hNode.child("horaInicio").getValue(Int::class.java)
            val horaFin = hNode.child("horaFin").getValue(Int::class.java)
            
            if (horaInicio != null && horaFin != null) {
                horarios.add(HorarioAtencion(i, horaInicio, horaFin))
            }
        }

        // Si no hay horarios estructurados, intentar parsear texto
        if (horarios.isEmpty()) {
            val horarioTexto = snapshot.child("horarioDeAtencionTexto").getValue(String::class.java) ?: ""
            horarios.addAll(parseHorarioTexto(horarioTexto))
        }

        return horarios
    }

    private fun parseHorarioTexto(texto: String): List<HorarioAtencion> {
        val horarios = mutableListOf<HorarioAtencion>()
        
        // Ejemplo: "L-V: 8 a 17 S8 a 17 D8 a 17"
        // L-V: Lunes a Viernes (dias 0-4)
        // S: Sábado (dia 5)
        // D: Domingo (dia 6)

        val lvMatch = Regex("""L-?V:?\s*(\d{1,2})\s*a\s*(\d{1,2})""", RegexOption.IGNORE_CASE).find(texto)
        if (lvMatch != null) {
            val inicio = lvMatch.groupValues[1].toIntOrNull() ?: return horarios
            val fin = lvMatch.groupValues[2].toIntOrNull() ?: return horarios
            for (dia in 0..4) {
                horarios.add(HorarioAtencion(dia, inicio, fin))
            }
        }

        val sMatch = Regex("""S:?\s*(\d{1,2})\s*a\s*(\d{1,2})""", RegexOption.IGNORE_CASE).find(texto)
        if (sMatch != null) {
            val inicio = sMatch.groupValues[1].toIntOrNull() ?: return horarios
            val fin = sMatch.groupValues[2].toIntOrNull() ?: return horarios
            horarios.add(HorarioAtencion(5, inicio, fin))
        }

        val dMatch = Regex("""D:?\s*(\d{1,2})\s*a\s*(\d{1,2})""", RegexOption.IGNORE_CASE).find(texto)
        if (dMatch != null) {
            val inicio = dMatch.groupValues[1].toIntOrNull() ?: return horarios
            val fin = dMatch.groupValues[2].toIntOrNull() ?: return horarios
            horarios.add(HorarioAtencion(6, inicio, fin))
        }

        return horarios
    }

    private fun tryRouteToNearestOpenHospital(userLatLng: LatLng) {
        val especialidad = intent.getStringExtra("Especialidad")?.trim()
        if (especialidad.isNullOrEmpty()) return

        val now = Calendar.getInstance()
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) // 1=Domingo, 2=Lunes, ..., 7=Sábado
        val hour24 = now.get(Calendar.HOUR_OF_DAY)

        // Mapear Calendar.DAY_OF_WEEK a índice 0..6 (0=Lunes, 6=Domingo)
        val diaIndex = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // Filtrar hospitales que contengan la especialidad y estén abiertos
        val candidatosAbiertos = hospitalMarkers.map { it.first }.filter { centro ->
            centro.especialidades.any { it.equals(especialidad, ignoreCase = true) } &&
            estaAbierto(centro.horarioAtencion, diaIndex, hour24)
        }

        if (candidatosAbiertos.isEmpty()) {
            Toast.makeText(this, "No hay centros abiertos para: $especialidad", Toast.LENGTH_SHORT).show()
            return
        }

        // Encontrar el más cercano
        val nearest = candidatosAbiertos.minByOrNull { centro ->
            val dLat = centro.latitud - userLatLng.latitude
            val dLng = centro.longitud - userLatLng.longitude
            dLat * dLat + dLng * dLng
        } ?: return

        // Trazar ruta al hospital más cercano
        drawRouteToHospital(userLatLng, LatLng(nearest.latitud, nearest.longitud), nearest.nombre)
    }

    private fun estaAbierto(horarios: ArrayList<HorarioAtencion>?, diaIndex: Int, horaActual24: Int): Boolean {
        if (horarios == null || horarios.isEmpty()) return false
        val hDia = horarios.firstOrNull { it.dia == diaIndex } ?: return false
        return horaActual24 in hDia.horaInicio..hDia.horaFin
    }

    private fun drawRouteToHospital(start: LatLng, end: LatLng, hospitalName: String) {
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
                    routeToHospital?.remove()
                    routeToHospital = map.addPolyline(
                        PolylineOptions().addAll(decoded).color(Color.GREEN).width(16f)
                    )

                    val distance = call.body()?.routes?.firstOrNull()?.legs?.firstOrNull()?.distance?.text
                    val duration = call.body()?.routes?.firstOrNull()?.legs?.firstOrNull()?.duration?.text

                    Toast.makeText(
                        this@MapActivity,
                        "$hospitalName → $distance • $duration",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
                Toast.makeText(this@MapActivity, "Error Firebase: ${error.message}", Toast.LENGTH_LONG).show()
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
                        this@MapActivity,
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