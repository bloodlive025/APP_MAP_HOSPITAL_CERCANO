package com.programmingtask.hospitalroutingappk
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

//import com.google.firebase.storage.storage

class HospitalActivity: AppCompatActivity() {

    private lateinit var btnNuevoHospital: Button

    private lateinit var adapter: CentroMedicoAdapter

    private val listaCentros = mutableListOf<CentroMedico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_menu_hospital)
        val hospitalReferencia = OnDatabase.tablaBaseDeDatos("CentrosMedicos")
        //var storageRef =Firebase.storage
        //val hospitalImagen = storageRef.getReference("CentrosMedicos")


        var listViewHospital = findViewById<ListView>(R.id.lvHospital)

        btnNuevoHospital = findViewById<Button>(R.id.btnNuevoHospital)

        btnNuevoHospital.setOnClickListener {
            val intent = Intent(this, AddHospitalActivity::class.java)
            startActivity(intent)
        }

        adapter = CentroMedicoAdapter(this, listaCentros)
        listViewHospital.adapter = adapter

        cargarHospitales(hospitalReferencia)





    }

    private fun cargarHospitales(ref: com.google.firebase.database.DatabaseReference) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Toast.makeText(this@HospitalActivity, "GG", Toast.LENGTH_SHORT).show()

                listaCentros.clear()
                for (hospitalSnapshot in snapshot.children) {
                    val hospital = hospitalSnapshot.getValue(CentroMedico::class.java)
                    hospital?.let {
                        listaCentros.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HospitalActivity, "Error al cargar CentrosMedicos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}