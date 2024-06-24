package com.ebookfrenzy.examen02v3

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log

class BeaconEmitter(private val bluetoothAdapter: BluetoothAdapter) {

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null

    init {
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(uuid: String, major: Int, minor: Int, txPower: Int) {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BluetoothLeAdvertiser is null")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val beaconUuid = ParcelUuid.fromString(uuid)
        val beaconData = AdvertiseData.Builder()
            .addServiceUuid(beaconUuid)
            .setIncludeDeviceName(false)
            .addManufacturerData(
                MANUFACTURER_ID,
                createBeaconData(beaconUuid, major, minor, txPower)
            )
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                Log.i(TAG, "Beacon advertising started successfully with UUID: $uuid, Major: $major, Minor: $minor, TxPower: $txPower")

            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                Log.e(TAG, "Beacon advertising failed with error code: $errorCode")
            }
        }
        Log.i(TAG, "Starting beacon advertising with UUID: $uuid, Major: $major, Minor: $minor, TxPower: $txPower")
        bluetoothLeAdvertiser?.startAdvertising(settings, beaconData, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (bluetoothLeAdvertiser != null && advertiseCallback != null) {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            advertiseCallback = null
            Log.i(TAG, "Beacon advertising stopped")
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
        private const val MANUFACTURER_ID = 0x0000// ID de Apple, usado para iBeacons
    }
}