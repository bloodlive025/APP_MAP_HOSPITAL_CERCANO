package com.programmingtask.hospitalroutingappk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HorarioDeAtencionActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_horario_atencion)
        var etAperturaLunesAVieres = findViewById<EditText>(R.id.etlunesApertura)
        var etCierreLunesAViernes = findViewById<EditText>(R.id.etlunescierre)
        var etAperturaSabado = findViewById<EditText>(R.id.etSabadoApertura)
        var etCierreSabado = findViewById<EditText>(R.id.etCierreSabado)
        var etAperturaDomingo = findViewById<EditText>(R.id.etAperturaDomingo)
        var etCierreDomingo = findViewById<EditText>(R.id.etCierreDomingo)
        var btnGuardarHoraio = findViewById<Button>(R.id.btnGuardarHorario)

        var horario: ArrayList<HorarioAtencion> = ArrayList()





        btnGuardarHoraio.setOnClickListener {
            Toast.makeText(this,etAperturaLunesAVieres.text.toString(),Toast.LENGTH_LONG).show()

            if(etAperturaLunesAVieres.text.toString().isEmpty() || etCierreLunesAViernes.text.toString().isEmpty() || etAperturaSabado.text.toString().isEmpty()
                || etCierreSabado.text.toString().isEmpty() || etAperturaDomingo.text.toString().isEmpty() || etCierreDomingo.text.toString().isEmpty()){


                Toast.makeText(this,"Completa todo",Toast.LENGTH_LONG).show()

            }
            else{
                for  (i in 0 until 5) {
                    var tripleHorario  = HorarioAtencion(i,etAperturaLunesAVieres.text.toString().toInt(),etCierreLunesAViernes.text.toString().toInt())
                    horario.add(tripleHorario)

                }
                var horarioSabado = HorarioAtencion(5,etAperturaSabado.text.toString().toInt(),etCierreSabado.text.toString().toInt())
                horario.add(horarioSabado)

                var horarioDomingo = HorarioAtencion(6,etAperturaDomingo.text.toString().toInt(),etCierreDomingo.text.toString().toInt())
                horario.add(horarioDomingo)




                val resultIntent = Intent()
                resultIntent.putExtra("horarioAtencion", horario)
                Toast.makeText(this,"HorarioRegistradoCoorectamente",Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_OK,resultIntent)
                finish()

            }
        }




    }


}