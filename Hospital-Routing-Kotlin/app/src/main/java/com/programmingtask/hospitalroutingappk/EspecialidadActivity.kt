package com.programmingtask.hospitalroutingappk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.programmingtask.hospitalroutingappk.EspecialidadesRepository.listaEspecialidades
import java.util.UUID
import kotlin.collections.mutableListOf

class EspecialidadActivity: AppCompatActivity() {

    private lateinit var btnAñadirEspecialidades: Button
    private lateinit var etNuevaEspecialidad: EditText
    private lateinit var adapter: EspecialidadAdapter
    private lateinit var especialidadController: EspecialidadController
    private lateinit var referencia: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_menu_especialidades)
        referencia = OnDatabase.tablaBaseDeDatos("Especialidades")

        btnAñadirEspecialidades = findViewById<Button>(R.id.btnNuevaEspecialidad)
        var listViewEspecialidades = findViewById<ListView>(R.id.lvEspecialidades)

        etNuevaEspecialidad = findViewById<EditText>(R.id.etNuevaEspecialidad)

        adapter = EspecialidadAdapter(this, listaEspecialidades)
        listViewEspecialidades.adapter = adapter
        especialidadController = EspecialidadController()

        especialidadController.cargarEspecialidades(referencia,adapter,this)

        btnAñadirEspecialidades.setOnClickListener {


            var especialidAniadir = etNuevaEspecialidad.text.toString().trim()
            var reference = OnDatabase.tablaBaseDeDatos("Especialidades")


            if (especialidAniadir.isEmpty() || listaEspecialidades.any {it.nombre.equals(especialidAniadir, ignoreCase = true)}){
                Toast.makeText(this,"Escribe una especialidad no repetida", Toast.LENGTH_SHORT).show()
            }
            else{
                var id = UUID.randomUUID()
                var pushNuevaEspecialidad = Especialidades(id.toString(),etNuevaEspecialidad.text.toString().trim())

                reference.child(id.toString()).setValue(pushNuevaEspecialidad.nombre)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Especialidad guardada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }

            }
            

        }

 


    }
    override fun onDestroy() {
        super.onDestroy()
        especialidadController.removerListener(referencia)
    }






}