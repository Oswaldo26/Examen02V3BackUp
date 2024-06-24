package com.ebookfrenzy.examen02v3



import kotlin.math.pow

data class Beacon(
    val uuid: String,
    val major: Int,
    val minor: Int,
    val rssi: Int
) {
    /**
     * Calcula la distancia aproximada al beacon usando el valor de RSSI.
     * @return La distancia estimada en metros.
     */
    fun calculateDistance(): Double {
        val txPower = -59 // Valor de referencia para la potencia de transmisión en 1 metro
        return if (rssi == 0) {
            -1.0 // Si RSSI es 0, no se puede calcular la distancia
        } else {
            val ratio = rssi.toDouble() / txPower
            if (ratio < 1.0) {
                ratio.pow(10.0)
            } else {
                0.89976 * ratio.pow(7.7095) + 0.111
            }
        }
    }

    override fun toString():String{
        return "Beacon(uuid = '$uuid' , major= $major, minor= $minor, rssi=$rssi, distance=${calculateDistance()} meters)"
    }
}
