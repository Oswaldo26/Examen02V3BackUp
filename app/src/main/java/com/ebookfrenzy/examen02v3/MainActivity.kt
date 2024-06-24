package com.ebookfrenzy.examen02v3




import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat


class MainActivity : ComponentActivity() {

    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var beaconEmitter: BeaconEmitter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothHelper = BluetoothHelper(this)
        beaconEmitter = BeaconEmitter(BluetoothAdapter.getDefaultAdapter())

        setContent {
            BluetoothApp(bluetoothHelper,beaconEmitter)
        }

        requestPermissions()
        testBeaconDistance()
    }

    private fun requestPermissions() {
        if (!bluetoothHelper.checkPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo habilitar Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Función de prueba para Beacon
    private fun testBeaconDistance() {
        val beacon = Beacon(uuid = "12345678-1234-1234-1234-123456789012", major = 1, minor = 1, rssi = -70)
        val distance = beacon.calculateDistance()
        println("Distancia estimada: $distance metros")
        Log.d("BeaconTest", beacon.toString())

    }
}

@Composable
fun BluetoothApp(bluetoothHelper: BluetoothHelper, beaconEmitter: BeaconEmitter) {
    var bluetoothEnabled by remember { mutableStateOf(bluetoothHelper.isBluetoothEnabled()) }
    var advertising by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (bluetoothEnabled) "Bluetooth está habilitado" else "Bluetooth no está habilitado")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (bluetoothEnabled) {
                bluetoothHelper.disableBluetooth()
            } else {
                bluetoothHelper.enableBluetooth()
            }
            bluetoothEnabled = bluetoothHelper.isBluetoothEnabled()
        }) {
            Text(text = if (bluetoothEnabled) "Deshabilitar Bluetooth" else "Habilitar Bluetooth")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (advertising) {
                beaconEmitter.stopAdvertising()
                Log.d("BeaconEmitterTest", "Beacon advertising stopped")
            } else {
                beaconEmitter.startAdvertising(
                    uuid = "12345678-1234-1234-1234-123456789012",
                    major = 1,
                    minor = 1,
                    txPower = -59
                )
                Log.d("BeaconEmitterTest", "Beacon advertising started")
            }
            advertising = !advertising
        }) {
            Text(text = if (advertising) "Detener Beacon" else "Iniciar Beacon")
        }
    }
}

