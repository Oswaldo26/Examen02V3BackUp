package com.ebookfrenzy.examen02v3


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat

class BeaconEmitter(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser

    fun startAdvertising(uuid: String, major: Int, minor: Int, txPower: Int) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BeaconEmitter", "Bluetooth no está habilitado o no disponible")
            return
        }

        if (!checkPermissions()) {
            Log.e("BeaconEmitter", "Permisos insuficientes para iniciar la publicidad")
            return
        }

        try {
            if (advertiser == null) {
                Log.e("BeaconEmitter", "Failed to create advertiser")
                return
            }

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(false)
                .build()

            val serviceUuid = ParcelUuid.fromString(EMITTED_SERVICE_UUID)

            val manufacturerData = byteArrayOf(
                0x02, 0x15 // iBeacon prefix
            ) + uuidToByteArray(uuid) + byteArrayOf(
                (major shr 8).toByte(), (major and 0xFF).toByte(), // Major
                (minor shr 8).toByte(), (minor and 0xFF).toByte(), // Minor
                txPower.toByte() // TxPower
            )

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false) // No incluir el nombre del dispositivo para ahorrar espacio
                .addServiceUuid(serviceUuid)
                .addManufacturerData(0x004C, manufacturerData) // Datos del fabricante
                .build()

            Log.i("BeaconEmitter", "Starting beacon advertising with UUID: $uuid, Major: $major, Minor: $minor, TxPower: $txPower")
            Log.i("BeaconEmitter", "Beacon data: ${data.toString()}")
            Log.i("BeaconEmitter", "Service UUIDs: [$EMITTED_SERVICE_UUID]")

            advertiser.startAdvertising(settings, data, advertiseCallback)
        } catch (e: SecurityException) {
            Log.e("BeaconEmitter", "SecurityException: ${e.message}")
        }
    }

    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(advertiseCallback)
            Log.i("BeaconEmitter", "Advertising stopped")
        } catch (e: SecurityException) {
            Log.e("BeaconEmitter", "SecurityException: ${e.message}")
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i("BeaconEmitter", "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BeaconEmitter", "Advertising failed with error code: $errorCode")
            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> Log.e("BeaconEmitter", "Advertising already started")
                ADVERTISE_FAILED_DATA_TOO_LARGE -> Log.e("BeaconEmitter", "Advertising data too large")
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> Log.e("BeaconEmitter", "Feature unsupported")
                ADVERTISE_FAILED_INTERNAL_ERROR -> Log.e("BeaconEmitter", "Internal error")
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> Log.e("BeaconEmitter", "Too many advertisers")
                else -> Log.e("BeaconEmitter", "Unknown error code: $errorCode")
            }
        }
    }

    private fun uuidToByteArray(uuid: String): ByteArray {
        val parsedUuid = java.util.UUID.fromString(uuid)
        val msb = parsedUuid.mostSignificantBits
        val lsb = parsedUuid.leastSignificantBits

        val byteArray = ByteArray(16)
        for (i in 0..7) {
            byteArray[i] = (msb shr 8 * (7 - i) and 0xFF).toByte()
            byteArray[i + 8] = (lsb shr 8 * (7 - i) and 0xFF).toByte()
        }
        return byteArray
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private const val EMITTED_SERVICE_UUID = "0000180D-0000-1000-8000-00805F9B34FB" // UUID del servicio emitido
        private const val ADVERTISE_FAILED_ALREADY_STARTED = 1
        private const val ADVERTISE_FAILED_DATA_TOO_LARGE = 2
        private const val ADVERTISE_FAILED_FEATURE_UNSUPPORTED = 3
        private const val ADVERTISE_FAILED_INTERNAL_ERROR = 4
        private const val ADVERTISE_FAILED_TOO_MANY_ADVERTISERS = 5
    }
}
