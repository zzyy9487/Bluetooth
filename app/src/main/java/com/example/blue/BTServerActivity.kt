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
import com.example.blue.btserver.MsgBTServerAdapter
import kotlinx.android.synthetic.main.activity_btclient.*
import kotlinx.android.synthetic.main.activity_btserver.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class BTServerActivity : AppCompatActivity() {

    lateinit var msgAdapter: MsgBTServerAdapter
    var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btserver)

        msgAdapter = MsgBTServerAdapter()
        recyclerViewMsg_BTServer.layoutManager = LinearLayoutManager(this)
        recyclerViewMsg_BTServer.adapter = msgAdapter

        val uuid = getUUID()

        receiver = object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action ?: return
                val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                when(action){
                    BluetoothDevice.ACTION_ACL_CONNECTED ->{
                        runOnUiThread{
                            textView_BTServer_status.setText(dev.name + "(已連接)" + dev.address)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED ->{
                        runOnUiThread{
                            textView_BTServer_status.text =  "尚未連接"
                        }
                    }

                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(receiver, filter)

        Thread{
            try {
                while (true){
                    var mySocketOk = true

                    while (mySocketOk){
                        val mySocket = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("XDDD", uuid)
                        val socket = mySocket.accept()
                        mySocket.close()

                        ImageBtn_BTServer_Send.setOnClickListener {
                            if (!socket.isConnected) return@setOnClickListener
                            val string :String = edit_BTServer_MsgInput.text.toString()
                            val dataOutput = DataOutputStream(socket.outputStream)
                            try {
                                dataOutput.writeInt(0)
                                dataOutput.writeUTF(string)
                                dataOutput.flush()
                            } catch (e: IOException){

                            }
                        }

                        val connecting = true

                        Thread{
                            while (connecting){
                                if (!socket.isConnected) socket.connect()
                                val dataInput = DataInputStream(socket.inputStream)
                                try {
                                    val toastWord :String? = dataInput.readUTF().toString()
                                    runOnUiThread{
                                        if (!toastWord.isNullOrEmpty()) Toast.makeText(this@BTServerActivity, toastWord, Toast.LENGTH_SHORT).show()
                                        msgAdapter.addMSG(toastWord)
                                        msgAdapter.notifyDataSetChanged()
                                    }
                                } catch (e:Throwable) {

                                }
                            }
                        }.start()

                    }
                }

            } catch (e:Exception){

            }

        }.start()
    }

    fun getUUID(): UUID {
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}
