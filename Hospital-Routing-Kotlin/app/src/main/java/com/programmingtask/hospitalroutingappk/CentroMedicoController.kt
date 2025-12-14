package com.programmingtask.hospitalroutingappk

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class CentroMedicoController {

    fun AddCentroMedico(ref:com.google.firebase.database.DatabaseReference,centroMedico: CentroMedico ,callback: (Boolean) -> Unit){
        val key = ref.push().key
        if (key != null) {
            ref.child(key).setValue(centroMedico)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }
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
}