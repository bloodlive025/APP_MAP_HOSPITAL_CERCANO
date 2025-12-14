package com.programmingtask.hospitalroutingappk

import java.io.Serializable

data class HorarioAtencion(
    val dia: Int = 0,
    val horaInicio: Int = 0,
    val horaFin: Int = 0
): Serializable{
    constructor() : this(0, 0, 0)

}