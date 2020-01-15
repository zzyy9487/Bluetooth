package com.example.blue

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_ENABLE_BT :Int = 0
    lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BluetoothAdapter.getDefaultAdapter() == null)
            Toast.makeText(this, "this device no BlueTooth inside...", Toast.LENGTH_SHORT).show()

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            Toast.makeText(this, "this device no BLE inside...", Toast.LENGTH_SHORT).show()

        permission()

        btn_TurnOn.setOnClickListener {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled){
                val enabledBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enabledBTIntent, REQUEST_ENABLE_BT)
            } else Toast.makeText(this, "BT turnon already", Toast.LENGTH_SHORT).show()
        }

        btn_BT_Client.setOnClickListener {
            startActivity(Intent(this, BTClientActivity::class.java))
        }
        btn_BT_Server.setOnClickListener {
            startActivity(Intent(this, BTServerActivity::class.java))
        }
        btn_BLE_Client.setOnClickListener {
            startActivity(Intent(this, BLEClientActivity::class.java))
        }
        btn_BLE_Server.setOnClickListener {
            startActivity(Intent(this, BLEServerActivity::class.java))
        }

        btn_TurnOff.setOnClickListener {
            BluetoothAdapter.getDefaultAdapter()?.disable()
            btn_TurnOn.isEnabled = true
            btn_BT_Client.isEnabled = false
            btn_BT_Server.isEnabled = false
            btn_BLE_Client.isEnabled = false
            btn_BLE_Server.isEnabled = false
            btn_TurnOff.isEnabled = false
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT){
            when (resultCode){
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "BT Turn on.", Toast.LENGTH_SHORT).show()
                    btn_BT_Client.isEnabled = true
                    btn_BT_Server.isEnabled = true
                    btn_BLE_Client.isEnabled = true
                    btn_BLE_Server.isEnabled = true
                    btn_TurnOff.isEnabled = true
                    btn_TurnOn.isEnabled = false
                }
                Activity.RESULT_CANCELED -> Toast.makeText(this, "WTF...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun permission() {
        val permissionList = arrayListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        var size = permissionList.size
        var i = 0
        while (i < size) {         //將三項存取權用迴圈一個一個判斷使用者是否同意存取
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionList[i]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.removeAt(i)
                i -= 1
                size -= 1
            }
            i += 1
        }
        val array = arrayOfNulls<String>(permissionList.size)
        if (permissionList.isNotEmpty()) ActivityCompat.requestPermissions(
            this,
            permissionList.toArray(array),
            0
        )
    }

    override fun onResume() {
        super.onResume()

        timer = Timer(true)
        val timerTask:TimerTask = object :TimerTask(){
            override fun run() {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled){
                    runOnUiThread{
                        btn_BT_Client.isEnabled = false
                        btn_BT_Server.isEnabled = false
                        btn_BLE_Client.isEnabled = false
                        btn_BLE_Server.isEnabled = false
                        btn_TurnOff.isEnabled = false
                        btn_TurnOn.isEnabled = true
                    }
                } else {
                    runOnUiThread{
                        btn_BT_Client.isEnabled = true
                        btn_BT_Server.isEnabled = true
                        btn_BLE_Client.isEnabled = true
                        btn_BLE_Server.isEnabled = true
                        btn_TurnOff.isEnabled = true
                        btn_TurnOn.isEnabled = false
                    }
                }
            }
        }
        timer.schedule(timerTask, 0, 1000)

    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


}
