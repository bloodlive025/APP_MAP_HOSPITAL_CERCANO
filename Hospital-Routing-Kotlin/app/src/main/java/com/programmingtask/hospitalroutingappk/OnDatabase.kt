package com.programmingtask.hospitalroutingappk

import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

object OnDatabase{

    val database = Firebase.database

    fun tablaBaseDeDatos (nombreTabla:String): DatabaseReference{
        val ref = database.getReference(nombreTabla)
        return ref
    }

}