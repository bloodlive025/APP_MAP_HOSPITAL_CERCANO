package com.programmingtask.hospitalroutingappk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory


class SeleccionarUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapa: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var marker: Marker? = null

    private var latSeleccionada: Double? = null
    private var lngSeleccionada: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarUbicacion)


        btnGuardar.setOnClickListener {
            if (latSeleccionada != null && lngSeleccionada != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("latitud", latSeleccionada)
                resultIntent.putExtra("longitud", lngSeleccionada)
                setResult(Activity.RESULT_OK,resultIntent)
                finish()
            }
        }

    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap
        mapa.uiSettings.isZoomControlsEnabled = true

        obtenerUbicacionActual()

        // Cuando el usuario toca en otra parte del mapa → mover marker
        mapa.setOnMapClickListener { latLng ->
            moverMarker(latLng)
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtenerUbicacionActual() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val miUbicacion = LatLng(location.latitude, location.longitude)
                moverMarker(miUbicacion)
                mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 16f))
            }
        }
    }


    private fun moverMarker(latLng: LatLng) {
        marker?.remove()
        marker = mapa.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))

        latSeleccionada = latLng.latitude
        lngSeleccionada = latLng.longitude
    }


}