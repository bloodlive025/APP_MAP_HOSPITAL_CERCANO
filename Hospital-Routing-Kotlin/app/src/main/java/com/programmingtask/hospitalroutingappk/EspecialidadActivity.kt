package com.programmingtask.hospitalroutingappk

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EspecialidadActivity: AppCompatActivity() {

    private lateinit var btnAñadirEspecialidades: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_menu_especialidades)

        btnAñadirEspecialidades = findViewById<Button>(R.id.btnespecialidadesmenuprincipal)

    }


}