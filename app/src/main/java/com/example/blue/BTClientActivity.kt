package com.example.blue

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blue.btclient.MsgBTClientAdapter
import com.example.blue.btclient.ScanBTDeviceAdapter
import kotlinx.android.synthetic.main.activity_btclient.*
import kotlinx.android.synthetic.main.cell_btclient.view.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class BTClientActivity : AppCompatActivity() {

    lateinit var timer: Timer
    var receiver: BroadcastReceiver? = null
    lateinit var scanAdapter:ScanBTDeviceAdapter
    lateinit var msgAdapter:MsgBTClientAdapter
    lateinit var threadRead:Thread
    lateinit var threadBonded: Thread
    lateinit var myBluetoothSocket:BluetoothSocket
    var threadReadBoolean = true
    var threadBondedBoolean = true

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
                    BluetoothDevice.ACTION_ACL_CONNECTED ->{
                        runOnUiThread{
                            textView_BTClient_status.text = dev.name + "(已連接)" + dev.address
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED ->{
                        runOnUiThread{
                            textView_BTClient_status.text =  "尚未連接"
                        }
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED ->{
                        runOnUiThread{
                            scanAdapter.notifyDataSetChanged()
                        }
                    }

                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(receiver, filter)

        btn_startscan.setOnClickListener {
            scanAdapter.addBondedBT()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        btn_stopscan.setOnClickListener {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
        }

        btn_rescan.setOnClickListener {
            scanAdapter.myDevices.clear()
            scanAdapter.addBondedBT()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        scanAdapter.setClickListener(object :ScanBTDeviceAdapter.OnClickListener{

            override fun bondorconnect(dev: BluetoothDevice, position:Int) {
                AlertDialog.Builder(this@BTClientActivity)
                    .setTitle(dev.name)
                    .setMessage(dev.address)
                    .setNeutralButton("取消") { dialog, which ->
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    }
                    .setNegativeButton("配對") { dialog, which ->
                        if (dev.bondState == 10){
                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                            dev.createBond()
                            threadBonded = Thread{
                                while (threadBondedBoolean){
                                    if (dev.bondState == 12){
                                        try {
                                            runOnUiThread {
                                                scanAdapter.notifyItemChanged(position)
                                            }
                                            threadBondedBoolean = false
                                            threadBonded.interrupt()
                                        } catch (e:Exception){

                                        }
                                    }
                                }
                            }
                            threadBonded.start()
                        } else if (dev.bondState == 12){
                            Toast.makeText(this@BTClientActivity, "this device paired...", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setPositiveButton("連線") { dialog, which ->
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                        val socket = dev.createRfcommSocketToServiceRecord(getUUID())
                        try {
                            socket.connect()
                        } catch (e:Exception){

                        }
                        myBluetoothSocket = socket
                        ImageBtn_BTClient_Send.setOnClickListener {
                            if (!socket.isConnected) return@setOnClickListener
                            val string :String = edit_BTClient_MsgInput.text.toString()
                            val dataOutput = DataOutputStream(socket.outputStream)
                            try {
                                dataOutput.writeUTF(string)
                                dataOutput.flush()
                            } catch (e:IOException){

                            }
                        }

                        threadRead = Thread{
                            while (threadReadBoolean){
                                if (!socket.isConnected) socket.connect()
                                val dataInput = DataInputStream(socket.inputStream)
                                try {
                                    val toastWord :String? = dataInput.readUTF().toString()
                                    runOnUiThread{
                                        if (!toastWord.isNullOrEmpty()) Toast.makeText(this@BTClientActivity, toastWord, Toast.LENGTH_SHORT).show()
                                        msgAdapter.addMSG(toastWord)
                                    }
                                } catch (e:IOException) {

                                }
                            }
                        }
                        threadRead.start()
                    }.show()
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
        threadReadBoolean = false
        myBluetoothSocket.inputStream.close()
        myBluetoothSocket.outputStream.close()
        myBluetoothSocket.close()
        timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}
