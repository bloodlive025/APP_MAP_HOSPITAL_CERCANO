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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.programmingtask.hospitalroutingappk.HospitalesRepository.listaCentros

//import com.google.firebase.storage.storage

class HospitalActivity: AppCompatActivity() {

    private lateinit var btnNuevoHospital: Button

    private lateinit var adapter: CentroMedicoAdapter

    private lateinit var centroMedicoController : CentroMedicoController
    private lateinit var referencia: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_menu_hospital)
        referencia = OnDatabase.tablaBaseDeDatos("CentrosMedicos")
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

        centroMedicoController = CentroMedicoController()

        centroMedicoController.cargarHospitales(referencia,adapter,this)

    }


    override fun onDestroy() {
        super.onDestroy()
        centroMedicoController.removerListener(referencia)
    }



}