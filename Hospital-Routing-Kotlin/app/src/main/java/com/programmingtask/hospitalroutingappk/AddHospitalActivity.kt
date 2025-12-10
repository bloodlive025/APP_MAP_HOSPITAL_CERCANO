package com.programmingtask.hospitalroutingappk

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DatabaseReference
import java.util.UUID

class AddHospitalActivity: AppCompatActivity() {

    private lateinit var opcionEspecialidad: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnUbicacion: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_hospital)
        val hospitalReferencia = OnDatabase.tablaBaseDeDatos("CentrosMedicos")

        var nombreHospital = findViewById<TextView>(R.id.etNombre)
        btnGuardar = findViewById<Button>(R.id.btnGuargarAddHospital)
        opcionEspecialidad = findViewById<Spinner>(R.id.spEspecialidad)
        btnUbicacion = findViewById<Button>(R.id.btnUbicacionAddHospital)
        var chipEspecialidades = findViewById<ChipGroup>(R.id.cgespecialidades)




        val especialidades = arrayOf(
            "Seleccionar item: ",
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
        opcionEspecialidad.setOnItemSelectedListener(object: android.widget.AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val especialidadSeleccionada = parent.getItemAtPosition(position).toString()
                val yaExiste = (0 until chipEspecialidades.childCount).any { index ->
                    val chip = chipEspecialidades.getChildAt(index) as com.google.android.material.chip.Chip
                    chip.text == especialidadSeleccionada
                }

                if (!yaExiste && position != 0) {
                    // Crear chip dinámico
                    val chip =
                        com.google.android.material.chip.Chip(this@AddHospitalActivity).apply {
                            text = especialidadSeleccionada
                            isCloseIconVisible = true   // X para borrar
                            setOnCloseIconClickListener {
                                chipEspecialidades.removeView(this)
                            }
                            setTextAppearance(R.style.CustomChipStyle)
                        }
                    chipEspecialidades.addView(chip)
                }

            }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // No hacer nada
                }

            })

        btnUbicacion.setOnClickListener {

        }


        btnGuardar.setOnClickListener {
            if (nombreHospital.text.toString().trim().isEmpty()) {
                Toast.makeText(applicationContext, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            guardarContacto(nombreHospital,chipEspecialidades,hospitalReferencia)
        }

        btn


    }

    fun guardarContacto(nombreHospital: TextView, chipEspecialidades :ChipGroup,referencia: DatabaseReference){

        var nombreHospital = nombreHospital.text.toString().trim()
        var id = UUID.randomUUID()
        var listaEspecialidades: ArrayList<String> = ArrayList<String>()
        for ( i in 0 until chipEspecialidades.childCount){
            var chip = chipEspecialidades.getChildAt(i) as Chip
            listaEspecialidades.add(chip.text.toString().trim())
        }

        var hospital = CentroMedico(id.toString(),nombreHospital,listaEspecialidades)



        val key = referencia.push().key
        if (key != null) {
            referencia.child(key).setValue(hospital)
                .addOnSuccessListener {
                    Toast.makeText(this, "Hospital guardado exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }





    }



}