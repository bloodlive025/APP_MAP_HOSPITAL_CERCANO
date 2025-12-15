package com.programmingtask.hospitalroutingappk


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class EspecialidadAdapter (
    private val context: EspecialidadActivity,
    private val listaEspecialidad: MutableList<Especialidades>,
): BaseAdapter(){

    interface OnCentroMedicoListener {
        fun onEliminar(especialidad: Especialidades, posicion: Int)
    }

    override fun getCount(): Int {
        return listaEspecialidad.size
    }

    override fun getItem(position: Int): Any {
        return listaEspecialidad[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.especialidades,parent,
            false)

        val especialidades = listaEspecialidad[position]


        val btnEliminarEspecialidad = view.findViewById<Button>(R.id.btnEliminarEspecialidad)
        val tvEspecialidades = view.findViewById<TextView>(R.id.tvEspecialidadesAdapter)

        tvEspecialidades.text = especialidades.nombre
        btnEliminarEspecialidad.setOnClickListener {
            // Tu código aquí
            val reference = OnDatabase.tablaBaseDeDatos("Especialidades")

            reference.child(especialidades.id.toString()).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Especialidad eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Error al eliminar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }




        return view


    }


}