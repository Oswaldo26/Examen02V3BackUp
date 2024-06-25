package com.ebookfrenzy.examen02v3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log

class BeaconEmitter(context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser

    fun startAdvertising(uuid: String = "12345678-1234-1234-1234-123456789012", major: Int = 1, minor: Int = 1, txPower: Int = -59) {
        try {
            if (advertiser == null) {
                Log.e(TAG, "Failed to create advertiser")
                return
            }

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            val beaconUuid = ParcelUuid.fromString(uuid)
            val beaconData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(beaconUuid)
                .addManufacturerData(
                    MANUFACTURER_ID,
                    createBeaconData(beaconUuid, major, minor, txPower)
                )
                .build()

            advertiser.startAdvertising(settings, beaconData, advertiseCallback)
            Log.i(TAG, "Started advertising beacon with UUID: $uuid, Major: $major, Minor: $minor, TxPower: $txPower")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(advertiseCallback)
            Log.i(TAG, "Stopped advertising beacon")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i(TAG, "Beacon advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e(TAG, "Beacon advertising failed with error code: $errorCode")
        }
    }

    private fun createBeaconData(uuid: ParcelUuid, major: Int, minor: Int, txPower: Int): ByteArray {
        val manufacturerData = ByteArray(23)
        val uuidBytes = convertUuidToByteArray(uuid.uuid)

        System.arraycopy(uuidBytes, 0, manufacturerData, 2, 16)
        manufacturerData[18] = (major shr 8).toByte()
        manufacturerData[19] = (major and 0xFF).toByte()
        manufacturerData[20] = (minor shr 8).toByte()
        manufacturerData[21] = (minor and 0xFF).toByte()
        manufacturerData[22] = txPower.toByte()

        return manufacturerData
    }

    private fun convertUuidToByteArray(uuid: java.util.UUID): ByteArray {
        val uuidBytes = ByteArray(16)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits

        for (i in 0..7) {
            uuidBytes[i] = (msb shr 8 * (7 - i) and 0xFF).toByte()
            uuidBytes[i + 8] = (lsb shr 8 * (7 - i) and 0xFF).toByte()
        }
        return uuidBytes
    }

    companion object {
        private const val TAG = "BeaconEmitter"
        private const val MANUFACTURER_ID = 0x004C // ID de Apple, usado para iBeacons
    }
}