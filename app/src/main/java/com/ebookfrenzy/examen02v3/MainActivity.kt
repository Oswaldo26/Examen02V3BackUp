package com.ebookfrenzy.examen02v3

import android.Manifest

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat


import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity(), BeaconReceiver.BeaconListener {

    private lateinit var bluetoothHelper: BluetoothHelper
    private lateinit var beaconEmitter: BeaconEmitter
    private lateinit var beaconReceiver: BeaconReceiver
    private lateinit var positionCalculator: PositionCalculator
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private val detectedBeacons = mutableStateListOf<Beacon>()
    private var currentPosition by mutableStateOf(Position(0.0, 0.0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothHelper = BluetoothHelper(this)
        beaconEmitter = BeaconEmitter(this)
        beaconReceiver = BeaconReceiver(this).apply {
            addListener(this@MainActivity)
        }
        positionCalculator = PositionCalculator()

        // Registrar el ActivityResultLauncher
        enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo habilitar Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            BluetoothApp(bluetoothHelper, beaconEmitter, beaconReceiver, detectedBeacons, currentPosition)
        }

        requestPermissions()
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

    fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableBtIntent)
    }

    override fun onBeaconDetected(beacon: Beacon) {
        Log.d("BeaconReceiver", beacon.toString())

        if (!detectedBeacons.contains(beacon)) {
            detectedBeacons.add(beacon)
            positionCalculator.addBeaconData(beacon)
            if (detectedBeacons.size >= 3) {
                currentPosition = positionCalculator.calculatePosition()
                Log.d("PositionCalculator", "Current Position: $currentPosition")
            }
        }
    }
}

@Composable
fun BluetoothApp(
    bluetoothHelper: BluetoothHelper,
    beaconEmitter: BeaconEmitter,
    beaconReceiver: BeaconReceiver,
    detectedBeacons: List<Beacon>,
    currentPosition: Position
) {
    var bluetoothEnabled by remember { mutableStateOf(bluetoothHelper.isBluetoothEnabled()) }
    var advertising by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }

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
                (bluetoothHelper.activity as MainActivity).enableBluetooth()
            }
            bluetoothEnabled = bluetoothHelper.isBluetoothEnabled()
        }) {
            Text(text = if (bluetoothEnabled) "Deshabilitar Bluetooth" else "Habilitar Bluetooth")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (advertising) {
                beaconEmitter.stopAdvertising()
                Log.d("BeaconTest", "Beacon advertising stopped")
            } else {
                beaconEmitter.startAdvertising()
                Log.d("BeaconTest", "Beacon advertising started")
            }
            advertising = !advertising
        }) {
            Text(text = if (advertising) "Detener Beacon" else "Iniciar Beacon")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (scanning) {
                beaconReceiver.stopScanning()
                Log.d("BeaconTest", "Beacon scanning stopped")
            } else {
                beaconReceiver.startScanning()
                Log.d("BeaconTest", "Beacon scanning started")
            }
            scanning = !scanning
        }) {
            Text(text = if (scanning) "Detener Escaneo" else "Iniciar Escaneo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Beacons detectados:")

        LazyColumn {
            items(detectedBeacons) { beacon ->
                Text(text = beacon.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Text(text = "Posición actual: (${currentPosition.x}, ${currentPosition.y})")
        CanvasView(currentPosition)
    }
}



