package com.programmingtask.hospitalroutingappk

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddHospitalActivity: AppCompatActivity() {

    private lateinit var opcionEspecialidad: Spinner
    var mBitmap = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888)
    private lateinit var btnGuardar: Button
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var btnUbicacion: Button
    private lateinit var btnHorario: Button
    private lateinit var longitud: TextView
    private lateinit var latitud: TextView
    lateinit var imagenHospital: ImageView
    private lateinit var horarioAtencion: ArrayList<HorarioAtencion>
    var latitudDouble = 0.0
    var longitudDouble = 0.0
    private lateinit var especialidadController: EspecialidadController
    lateinit var centroMedicoImagen : StorageReference
    // Claves para guardar el estado
    companion object {
        private const val KEY_LATITUD = "latitud"
        private const val KEY_LONGITUD = "longitud"
        private const val KEY_HORARIO = "horario"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_hospital)
        val hospitalReferencia = OnDatabase.tablaBaseDeDatos("CentrosMedicos")
        val especialidadReferencia = OnDatabase.tablaBaseDeDatos("Especialidades")

        var storageRef = Firebase.storage
        centroMedicoImagen=storageRef.getReference("CentroMedico")


        especialidadController = EspecialidadController()

        var nombreHospital = findViewById<EditText>(R.id.etNombre)
        btnGuardar = findViewById<Button>(R.id.btnGuargarAddHospital)
        btnUbicacion = findViewById<Button>(R.id.btnUbicacionAddHospital)
        btnHorario = findViewById<Button>(R.id.btnHorarioDeAtencion)
        btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)
        horarioAtencion = ArrayList()
        opcionEspecialidad = findViewById<Spinner>(R.id.spEspecialidad)

        var chipEspecialidades = findViewById<ChipGroup>(R.id.cgespecialidades)
        longitud = findViewById<TextView>(R.id.tvlongitudHospital)
        latitud = findViewById<TextView>(R.id.tvLatitudHospital)
        imagenHospital = findViewById<ImageView>(R.id.ivCentroMedicoAniadirImagen)



        // ✅ RESTAURAR EL ESTADO SI EXISTE
        savedInstanceState?.let {
            latitudDouble = it.getDouble(KEY_LATITUD, 0.0)
            longitudDouble = it.getDouble(KEY_LONGITUD, 0.0)

            // Restaurar horarios
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                horarioAtencion = it.getSerializable(KEY_HORARIO, ArrayList::class.java) as? ArrayList<HorarioAtencion>
                    ?: ArrayList()
            } else {
                @Suppress("DEPRECATION")
                horarioAtencion = it.getSerializable(KEY_HORARIO) as? ArrayList<HorarioAtencion>
                    ?: ArrayList()
            }

            // Actualizar los TextViews
            if (latitudDouble != 0.0) {
                latitud.text = latitudDouble.toString()
            }
            if (longitudDouble != 0.0) {
                longitud.text = longitudDouble.toString()
            }
        }




        especialidadController.listarEspecialidades(especialidadReferencia,this){lista ->
            val especialidades = mutableListOf("Seleccionar Especialidad:")
            especialidades.addAll(lista.map{it.nombre})

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                especialidades
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            opcionEspecialidad.adapter = adapter
            opcionEspecialidad.setOnItemSelectedListener(object: android.widget.AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    val especialidadSeleccionada = parent.getItemAtPosition(position).toString()
                    val yaExiste = (0 until chipEspecialidades.childCount).any { index ->
                        val chip = chipEspecialidades.getChildAt(index) as com.google.android.material.chip.Chip
                        chip.text == especialidadSeleccionada
                    }

                    if (!yaExiste && position != 0) {
                        // Crear chip dinámico
                        val chip =
                            com.google.android.material.chip.Chip(this@AddHospitalActivity).apply {
                                text = especialidadSeleccionada
                                isCloseIconVisible = true   // X para borrar
                                setOnCloseIconClickListener {
                                    chipEspecialidades.removeView(this)
                                }
                                setTextAppearance(R.style.CustomChipStyle)
                            }
                        chipEspecialidades.addView(chip)
                    }

                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // No hacer nada
                }

            })

        }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImage: Uri? = result.data?.data
                selectedImage?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    mBitmap = bitmap
                    imagenHospital.setImageBitmap(bitmap)
                }
            }
        }
        btnSeleccionarImagen.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        btnUbicacion.setOnClickListener {
            val intentUbicacion= Intent(this, SeleccionarUbicacionActivity::class.java)
            seleccionarUbicacionLauncher.launch(intentUbicacion) // Usa el launcher
        }

        btnHorario.setOnClickListener {
            val intentHorario = Intent(this, HorarioDeAtencionActivity::class.java)
            seleccionarHorarioDeAtencionLauncher.launch(intentHorario)
        }




        btnGuardar.setOnClickListener {
            if (nombreHospital.text.toString().trim().isEmpty() || chipEspecialidades.childCount == 0 ||
                longitud.text.toString().trim().isEmpty() || latitud.text.toString().trim().isEmpty()
                ) {
                Toast.makeText(applicationContext, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            guardarContacto(nombreHospital,chipEspecialidades,hospitalReferencia,longitudDouble,latitudDouble,horarioAtencion)
            finish()
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble(KEY_LATITUD, latitudDouble)
        outState.putDouble(KEY_LONGITUD, longitudDouble)
        outState.putSerializable(KEY_HORARIO, horarioAtencion)
    }



    private val seleccionarUbicacionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitud = data?.getDoubleExtra("latitud", 0.0) ?: 0.0
            val longitud = data?.getDoubleExtra("longitud", 0.0) ?: 0.0
            // Actualizar tus TextViews o lo que necesites
            this.latitudDouble =latitud
            this.longitudDouble = longitud
            this.latitud.text = latitud.toString()
            this.longitud.text = longitud.toString()
        }
    }

    private val seleccionarHorarioDeAtencionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            // Opción 1: Si usas API 33+ (Android 13+)
            val horario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("horarioAtencion", ArrayList::class.java) as ArrayList<HorarioAtencion>
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra("horarioAtencion") as ArrayList<HorarioAtencion>
            }

            this.horarioAtencion =  horario


            // Usar el horario
            horario.let {
                it.forEach { horarioAtencion ->
                    Log.d("Horario", "Dia: ${horarioAtencion.dia}, Inicio: ${horarioAtencion.horaInicio}, Cierre: ${horarioAtencion.horaFin}")
                }
            }
        }
    }


    fun guardarContacto(nombreHospital: TextView, chipEspecialidades :ChipGroup, referencia:com.google.firebase.database.DatabaseReference,
                        longitud: Double, latitud: Double, horarioAtencion: ArrayList<HorarioAtencion> ){

        var nombreHospital = nombreHospital.text.toString().trim()
        var id = UUID.randomUUID()
        var listaEspecialidades: ArrayList<String> = ArrayList<String>()
        for ( i in 0 until chipEspecialidades.childCount){
            var chip = chipEspecialidades.getChildAt(i) as Chip
            listaEspecialidades.add(chip.text.toString().trim())
        }

        var hospital = CentroMedico(id.toString(),nombreHospital,listaEspecialidades,horarioAtencion,latitud,longitud)
        uploadImage(mBitmap,hospital){ success, url ->
            if (success && url != null) {
                hospital.imagenUrl = url

                // Ahora sí, guarda el hospital CON la URL
                val controller = CentroMedicoController()
                controller.AddCentroMedico(referencia, hospital) { successDb ->
                    if (successDb) {
                        Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
        }



    }

    fun uploadImage(bitmap: Bitmap, centroMedicoActual: CentroMedico,
                    callback: (Boolean, String?) -> Unit) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val referenciaContactoFirebase = centroMedicoImagen.child("imagenes/${centroMedicoActual.id}.jpg")

        referenciaContactoFirebase.putBytes(byteArray)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    callback(true, uri.toString())
                }?.addOnFailureListener {
                    callback(false, null)
                }
            }
            .addOnFailureListener {
                callback(false, null)
            }
    }





}