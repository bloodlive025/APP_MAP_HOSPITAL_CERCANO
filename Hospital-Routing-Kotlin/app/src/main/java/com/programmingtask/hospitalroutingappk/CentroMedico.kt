package com.programmingtask.hospitalroutingappk

data class CentroMedico(
    var id: String = "",
    var nombre: String = "",
    var especialidades: ArrayList<String> = ArrayList<String>(), // Lista de nombres de especialidades
    var horarioAtencion: ArrayList<String> = ArrayList<String>(), // Ej: "Lun-Vie: 8:00-20:00"
    var latitud: String = "",
    var longitud: String = "",
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
        return if(horarioAtencion.isNotEmpty()){
            "L"+horarioAtencion[0]+"\n"+
            "M"+horarioAtencion[1]+"\n"+
            "Mi"+horarioAtencion[2]+"\n"+
            "J"+horarioAtencion[3]+"\n"+
            "V"+horarioAtencion[4]+"\n"+
            "S"+horarioAtencion[5]+"\n"+
            "D"+horarioAtencion[6]+"\n"
        } else {
            "Sin horario de atencion"
        }
    }
}