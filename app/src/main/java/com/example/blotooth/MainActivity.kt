package com.example.blotooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var lstvw: ListView
    private var aAdapter: ArrayAdapter<*>? = null
    private val bAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var button: Button

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                listPairedDevices()
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lstvw = findViewById(R.id.lstvw)
        button = findViewById(R.id.button)

        button.setOnClickListener {
            if (bAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        listPairedDevices()
                    } else {
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                } else {
                    listPairedDevices()
                }
            }
        }
    }

    private fun listPairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        try {
            val pairedDevices = bAdapter?.bondedDevices
            val list = ArrayList<String>()

            if (!pairedDevices.isNullOrEmpty()) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    list.add("Name: $deviceName\nMAC Address: $deviceHardwareAddress")
                }
                aAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
                lstvw.adapter = aAdapter
            } else {
                Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "SecurityException: Permission required", Toast.LENGTH_SHORT).show()
        }
    }

}
