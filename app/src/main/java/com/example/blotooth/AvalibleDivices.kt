package com.example.blotooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AvalibleDivices : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var listView: ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val deviceList = ArrayList<String>()

    private val BLUETOOTH_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val REQUEST_CODE_BLUETOOTH = 100

    private val reciever = object: BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent){
            when(intent.action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let{
                        val deviceName = device.name ?: "Unknown Device"
                        val deviceAddress = device.address
                        deviceList.add("$deviceName\n$deviceAddress")
                        arrayAdapter.notifyDataSetChanged()
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context,"Discovery Finished", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avalible_divices)
        listView = findViewById(R.id.available_devices_list)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = arrayAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            Toast.makeText(this,"Bluetooth ius not supported",Toast.LENGTH_SHORT).show()
            return
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(checkPermissions()){
                discoverBluetoothDevices()
            }
        }
        else{
            discoverBluetoothDevices()
        }

    }

    private fun checkPermissions():Boolean{
        val permissionNeeded = BLUETOOTH_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this,it) != PackageManager.PERMISSION_GRANTED
        }
        if(permissionNeeded.isNotEmpty())
        {
            if(permissionNeeded.any{ ActivityCompat.shouldShowRequestPermissionRationale(this,it) }){
                Toast.makeText(this,"Bluetooth Permission Required to scan for devices",Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this,permissionNeeded.toTypedArray(),REQUEST_CODE_BLUETOOTH)
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun discoverBluetoothDevices(){
        if(bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(reciever,filter)
        val filterFinished = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(reciever,filterFinished)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(reciever)
        bluetoothAdapter.cancelDiscovery()
    }

}
