package com.example.blue

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blue.btclient.MsgBTClientAdapter
import com.example.blue.btclient.ScanBTDeviceAdapter
import kotlinx.android.synthetic.main.activity_btclient.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class BTClientActivity : AppCompatActivity() {

    lateinit var timer: Timer
    var receiver: BroadcastReceiver? = null
    lateinit var scanAdapter:ScanBTDeviceAdapter
    lateinit var msgAdapter:MsgBTClientAdapter
//    val MSG:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btclient)

        scanAdapter = ScanBTDeviceAdapter()
        recyclerViewScanDevice.layoutManager = LinearLayoutManager(this)
        recyclerViewScanDevice.adapter = scanAdapter

        msgAdapter = MsgBTClientAdapter()
        recyclerViewMsg_BTClient.layoutManager = LinearLayoutManager(this)
        recyclerViewMsg_BTClient.adapter = msgAdapter

        receiver = object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action ?: return
                val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                when(action){
                    BluetoothDevice.ACTION_FOUND ->{
                        scanAdapter.addBT(dev)
                    }
                }

            }
        }

        btn_startscan.setOnClickListener {
            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            scanAdapter.addBondedBT()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        btn_stopscan.setOnClickListener {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
        }

        btn_rescan.setOnClickListener {
            scanAdapter.myDevices.clear()
            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            scanAdapter.addBondedBT()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        scanAdapter.setClickListener(object :ScanBTDeviceAdapter.OnClickListener{
            override fun bondedDevice(bonuedDev: BluetoothDevice) {

                try {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    var bonding = true
                    Thread{
                        while (bonding){
                            if (bonuedDev.bondState == 12){
                                runOnUiThread{
                                    scanAdapter.notifyDataSetChanged()
                                }
                                bonding = false
                            }
                        }
                    }.start()
                    bonuedDev.createBond()
                } catch (e:IOException){

                }

            }

            override fun connectToDevice(connectDev: BluetoothDevice, position:Int) {

                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                try {
                    val socket = connectDev.createRfcommSocketToServiceRecord(getUUID())
                    socket.connect()
                    val connecting = true
                    Thread{
                        while (connecting){

                        }
                    }

                    ImageBtn_BTClient_Send.setOnClickListener {
                        if (!socket.isConnected) return@setOnClickListener
                        val string :String = edit_BTClient_MsgInput.text.toString()
                        val dataOutput =DataOutputStream(socket.outputStream)
                        try {
//                            dataOutput.writeInt(MSG)
                            dataOutput.writeUTF(string)
                            dataOutput.flush()
                        } catch (e:IOException){

                        }
                    }

                    Thread{
                        while (connecting){
                            if (!socket.isConnected) socket.connect()
                            val dataInput = DataInputStream(socket.inputStream)
                            val toastWord :String? = dataInput.readUTF().toString()
                            runOnUiThread{
                                if (!toastWord.isNullOrEmpty()) Toast.makeText(this@BTClientActivity, toastWord, Toast.LENGTH_SHORT).show()
                                    msgAdapter.addMSG(toastWord)
                                    msgAdapter.notifyDataSetChanged()
                            }
                        }
                    }.start()
                } catch (e:IOException){

                }

            }
        })

    }

    fun getUUID():UUID{
        var uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        try {
            val getUuidsMethod = BluetoothAdapter::class.java.getDeclaredMethod("getUuids")
            val uuids = getUuidsMethod.invoke(BluetoothAdapter.getDefaultAdapter(), null) as Array<ParcelUuid>
//            for (uuid in uuids) {
//                LogUtils.e(uuid.uuid)
//            }
            uuid = uuids[0].uuid
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uuid
    }

    override fun onResume() {
        super.onResume()
        timer = Timer(true)
        val timerTask :TimerTask = object :TimerTask(){
            override fun run() {
                try {
                    if (BluetoothAdapter.getDefaultAdapter() == null){
                        btn_startscan.isEnabled = false
                        btn_stopscan.isEnabled = false
                        ImageBtn_BTClient_Send.isEnabled = false
                    } else {
                        btn_startscan.isEnabled = true
                        btn_stopscan.isEnabled = true
                        ImageBtn_BTClient_Send.isEnabled = true
                    }
                } catch (e:Exception){

                }

            }
        }
        timer.schedule(timerTask, 0, 1000)
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}
