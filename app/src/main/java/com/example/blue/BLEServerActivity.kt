package com.example.blue

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blue.bleserver.ScanBLEDeviceAdapter
import kotlinx.android.synthetic.main.activity_bleserver.*


class BLEServerActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val REQUEST_ENABLE_BT:Int = 1
    val SCAN_PERIOD: Long = 5000

    var handler = Handler()
    var mScanning:Boolean = false

    lateinit var blescanAdapter:ScanBLEDeviceAdapter
    lateinit var bluttoothLeScanner :BluetoothLeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleserver)

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        blescanAdapter = ScanBLEDeviceAdapter()
        recyclerViewBLEScan.layoutManager = LinearLayoutManager(this)
        recyclerViewBLEScan.adapter = blescanAdapter

        val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            runOnUiThread {
                blescanAdapter.addBLE(device, rssi, scanRecord)
            }
        }

        fun scanLeDevice(enable: Boolean) {
            when (enable) {
                true -> {
                    // Stops scanning after a pre-defined scan period.
                    handler.postDelayed({
                        println("+++++++++++++++++++++++++++++++++++++++++++++++++")
                        mScanning = false
                        bluetoothAdapter?.stopLeScan(leScanCallback)
                    }, SCAN_PERIOD)
                    println("======================================================")
                    mScanning = true
                    bluetoothAdapter?.startLeScan(leScanCallback)
                }
                else -> {
                    mScanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallback)
                }
            }
        }







        btn_startscan.setOnClickListener {
//            scanLeDevice(true)
        }

        btn_stopscan.setOnClickListener {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQUEST_ENABLE_BT ->{
                when (resultCode) {
                    Activity.RESULT_OK ->{
                        Toast.makeText(this, "RESULT_OK", Toast.LENGTH_LONG).show()
                    }
                    Activity.RESULT_CANCELED ->{
                        Toast.makeText(this, "RESULT_CANCEL", Toast.LENGTH_LONG).show()
                    }

                }
            }

        }
    }
}
