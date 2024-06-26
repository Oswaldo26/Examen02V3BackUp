package com.ebookfrenzy.examen02v3



import kotlin.math.pow
import kotlin.math.sqrt

class PositionCalculator {

    private val beaconDataList = mutableListOf<Beacon>()

    fun addBeaconData(beacon: Beacon) {
        beaconDataList.add(beacon)
    }

    fun calculatePosition(): Position {
        if (beaconDataList.size < 3) {
            throw IllegalArgumentException("At least 3 beacons are required to calculate position.")
        }

        // Usar los tres primeros beacons para la trilateración (para simplificación)se
        val beacon1 = beaconDataList[0]
        val beacon2 = beaconDataList[1]
        val beacon3 = beaconDataList[2]

        // Asumimos que conocemos las posiciones de los beacons
        val beaconPositions = mapOf(
            beacon1.uuid to Position(0.0, 0.0),
            beacon2.uuid to Position(5.0, 0.0),
            beacon3.uuid to Position(2.5, 5.0)
        )

        val pos1 = beaconPositions[beacon1.uuid]!!
        val pos2 = beaconPositions[beacon2.uuid]!!
        val pos3 = beaconPositions[beacon3.uuid]!!

        val dist1 = rssiToDistance(beacon1.rssi)
        val dist2 = rssiToDistance(beacon2.rssi)
        val dist3 = rssiToDistance(beacon3.rssi)

        val A = 2 * (pos2.x - pos1.x)
        val B = 2 * (pos2.y - pos1.y)
        val C = dist1.pow(2) - dist2.pow(2) - pos1.x.pow(2) + pos2.x.pow(2) - pos1.y.pow(2) + pos2.y.pow(2)
        val D = 2 * (pos3.x - pos2.x)
        val E = 2 * (pos3.y - pos2.y)
        val F = dist2.pow(2) - dist3.pow(2) - pos2.x.pow(2) + pos3.x.pow(2) - pos2.y.pow(2) + pos3.y.pow(2)

        val x = (C * E - F * B) / (E * A - B * D)
        val y = (C * D - A * F) / (B * D - A * E)

        return Position(x, y)
    }

    private fun rssiToDistance(rssi: Int): Double {
        val txPower = -59 // Valor RSSI a 1 metro de distancia (valor típico, ajusta según tus beacons)
        return 10.0.pow((txPower - rssi) / (10 * 2))
    }
}
