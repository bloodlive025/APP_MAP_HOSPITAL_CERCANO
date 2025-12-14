package com.programmingtask.hospitalroutingappk

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnbuscar : Button
    private lateinit var btnHospital : Button
    private lateinit var btnEspecialidad: Button

    private lateinit var opcionEspecialidad: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.menu_principal)

        btnbuscar = findViewById<Button>(R.id.btnbuscar)
        btnHospital = findViewById<Button>(R.id.btnhospitalmenuprincipal)
        btnEspecialidad = findViewById<Button>(R.id.btnespecialidadesmenuprincipal)

        opcionEspecialidad = findViewById(R.id.spinner)

        //Cambiar para poder obtener las especcialidades de la base de datos
        val especialidades = arrayOf(
            "Cardiología",
            "Dermatología",
            "Pediatría",
            "Neurología",
            "Traumatología"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            especialidades
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        opcionEspecialidad.adapter = adapter



        btnHospital.setOnClickListener {
            val intent = Intent(this, HospitalActivity::class.java)
            startActivity(intent)

        }

        btnEspecialidad.setOnClickListener {
            val intent = Intent(this,EspecialidadActivity::class.java)
            startActivity(intent)
        }

        btnbuscar.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("Especialidad",opcionEspecialidad.selectedItem.toString())
            startActivity(intent)
        }








    }

}