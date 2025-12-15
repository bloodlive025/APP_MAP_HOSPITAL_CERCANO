package com.programmingtask.hospitalroutingappk

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.programmingtask.hospitalroutingappk.EspecialidadesRepository.listaEspecialidades

class EspecialidadController {
    lateinit var listener: ValueEventListener

    fun cargarEspecialidades(ref: com.google.firebase.database.DatabaseReference,adapter: EspecialidadAdapter,context: Context) {

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                listaEspecialidades.clear()

                for (especialidadSnapshot in snapshot.children) {

                    val id = especialidadSnapshot.key              // UUID
                    val nombre = especialidadSnapshot.getValue(String::class.java)

                    if (id != null && nombre != null) {
                        listaEspecialidades.add(
                            Especialidades(id, nombre)
                        )
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

    fun listarEspecialidades(ref: com.google.firebase.database.DatabaseReference,context: Context,
                                 onFinish: (MutableList<Especialidades>) -> Unit
    ){
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                listaEspecialidades.clear()

                for (especialidadSnapshot in snapshot.children) {
                    val id = especialidadSnapshot.key              // UUID
                    val nombre = especialidadSnapshot.getValue(String::class.java)
                    if (id != null && nombre != null) {
                        listaEspecialidades.add(
                            Especialidades(id, nombre)
                        )
                    }
                    onFinish(listaEspecialidades) //  AQUÍ Firebase YA RESPONDIÓ

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar Especialidades: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(listener)
    }




    fun removerListener(ref: com.google.firebase.database.DatabaseReference) {
        if (::listener.isInitialized) {
            ref.removeEventListener(listener)
        }
    }

}