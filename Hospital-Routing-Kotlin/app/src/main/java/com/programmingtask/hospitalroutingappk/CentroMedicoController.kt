package com.programmingtask.hospitalroutingappk

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.programmingtask.hospitalroutingappk.HospitalesRepository.listaCentros

class CentroMedicoController {
    lateinit var listener: ValueEventListener


    fun AddCentroMedico(ref:com.google.firebase.database.DatabaseReference,centroMedico: CentroMedico ,callback: (Boolean) -> Unit){
        val key = ref.push().key
        if (key != null) {
            ref.child(key).setValue(centroMedico)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }
    }
    fun cargarHospitales(ref: com.google.firebase.database.DatabaseReference,adapter: CentroMedicoAdapter,context: Context) {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                Toast.makeText(context, "Error al cargar CentrosMedicos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        ref.addValueEventListener(listener)

    }

    fun listarHospitales(ref: com.google.firebase.database.DatabaseReference,context: Context,
                         onFinish: (MutableList<CentroMedico>) -> Unit) {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCentros.clear()
                for (hospitalSnapshot in snapshot.children) {
                    val hospital = hospitalSnapshot.getValue(CentroMedico::class.java)
                    hospital?.let {
                        listaCentros.add(it)
                    }
                }
                onFinish(listaCentros)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar CentrosMedicos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        ref.addValueEventListener(listener)

    }


    
    fun removeCentroMedico(ref:com.google.firebase.database.DatabaseReference,centroMedico: CentroMedico ,id:String){


        ref.orderByChild("id")
            .equalTo(id)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    Log.d("DELETE", "No se encontró el centro médico")
                    return@addOnSuccessListener
                }

                for (nodo in snap.children) {
                    val firebaseId = nodo.key   // -Og9BiZsL-YUNrUQUI3a
                    ref.child(firebaseId!!).removeValue()
                }
            }
    }

    fun removerListener(ref: com.google.firebase.database.DatabaseReference) {
        if (::listener.isInitialized) {
            ref.removeEventListener(listener)
        }
    }

}