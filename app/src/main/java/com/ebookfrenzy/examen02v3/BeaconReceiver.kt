package com.ebookfrenzy.examen02v3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log


class BeaconReceiver(context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val listeners = mutableListOf<BeaconListener>()

    fun startScanning() {
        try {
            if (scanner == null) {
                Log.e("BeaconReceiver", "Failed to create scanner")
                return
            }

            scanner.startScan(scanCallback)
            Log.i("BeaconReceiver", "Started scanning for beacons")
        } catch (e: SecurityException) {
            Log.e("BeaconReceiver", "SecurityException: ${e.message}")
        }
    }

    fun stopScanning() {
        try {
            scanner?.stopScan(scanCallback)
            Log.i("BeaconReceiver", "Stopped scanning for beacons")
        } catch (e: SecurityException) {
            Log.e("BeaconReceiver", "SecurityException: ${e.message}")
        }
    }

    fun addListener(listener: BeaconListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BeaconListener) {
        listeners.remove(listener)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                val rssi = it.rssi
                val scanRecord = it.scanRecord
                val serviceUuids = scanRecord?.serviceUuids?.map { it.uuid.toString() }

                Log.i("BeaconReceiver", "Device: ${device.address}, RSSI: $rssi, Service UUIDs: $serviceUuids")

                listeners.forEach { listener ->
                    listener.onBeaconDetected(Beacon(serviceUuids?.firstOrNull() ?: "", major = 0, minor = 0, rssi))
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BeaconReceiver", "Scan failed with error code: $errorCode")
        }
    }

    companion object {
        private const val TAG = "BeaconReceiver"
    }

    interface BeaconListener {
        fun onBeaconDetected(beacon: Beacon)
    }
}
