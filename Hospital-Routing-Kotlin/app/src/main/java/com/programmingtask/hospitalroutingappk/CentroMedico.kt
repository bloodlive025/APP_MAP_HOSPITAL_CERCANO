package com.programmingtask.hospitalroutingappk

data class CentroMedico(
    var id: String = "",
    var nombre: String = "",
    var especialidades: ArrayList<String> = ArrayList<String>(), // Lista de nombres de especialidades
    var horarioAtencion: ArrayList<HorarioAtencion> = ArrayList(), // Ej: "Lun-Vie: 8:00-20:00"
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var imagenUrl: String = "",
    var activo: Boolean = true
) {
    // Método para obtener especialidades como texto
    fun getEspecialidadesTexto(): String {
        return if (especialidades.isNotEmpty()) {
            especialidades.joinToString(", ")
        } else {
            "Sin especialidades"
        }
    }

    fun getHorarioDeAtencionTexto(): String{
        var horarioLunesViernes = horarioAtencion[0].horaInicio.toString() + " a " + horarioAtencion[0].horaFin.toString()
        var horarioSabado = horarioAtencion[5].horaInicio.toString()+ " a " + horarioAtencion[5].horaFin.toString()
        var horarioDomingo = horarioAtencion[6].horaInicio.toString()+ " a " + horarioAtencion[6].horaFin.toString()

        return if(horarioAtencion.isNotEmpty()){
            "L-V: "+horarioLunesViernes+" "+ "\n"+
            "S"+horarioSabado+"\n"+
            "D"+horarioDomingo+"\n"
        } else {
            "Sin horario de atencion"
        }
    }
}