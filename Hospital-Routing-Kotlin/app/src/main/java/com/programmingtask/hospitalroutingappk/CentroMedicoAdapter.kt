package com.programmingtask.hospitalroutingappk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class CentroMedicoAdapter (
    private val context: HospitalActivity,
    private val listaCentros: MutableList<CentroMedico>,
): BaseAdapter(){

    interface OnCentroMedicoListener {
        fun onVerUbicacion(centro: CentroMedico, posicion: Int)
        fun onModificar(centro: CentroMedico, posicion: Int)
        fun onEliminar(centro: CentroMedico, posicion: Int)
    }

    override fun getCount(): Int {
        return listaCentros.size
    }

    override fun getItem(position: Int): Any {
        return listaCentros[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.hospital,parent,
            false)

        val centroMedico = listaCentros[position]

        val tvNombreHospital = view.findViewById<TextView>(R.id.lblnombreHospital)
        val tvEspecialidades = view.findViewById<TextView>(R.id.lblespecialidades)
        val tvHorarioAtencion = view.findViewById<TextView>(R.id.lblhorarioatencion)

        val btnModificarHospital = view.findViewById<Button>(R.id.btnmodificarhospital)
        val btnEliminarHospital = view.findViewById<Button>(R.id.btnEliminarHospital)


        tvNombreHospital.text = centroMedico.nombre
        tvEspecialidades.text = centroMedico.getEspecialidadesTexto()
        tvHorarioAtencion.text = centroMedico.getHorarioDeAtencionTexto()

        btnEliminarHospital.setOnClickListener {
            // Tu código aquí
            Toast.makeText(view.context,"Boton Eliminar presionado", Toast.LENGTH_SHORT).show()

        }

        btnModificarHospital.setOnClickListener {
            Toast.makeText(view.context,"Boton Modificar presionado", Toast.LENGTH_SHORT).show()
        }


        return view


    }




}