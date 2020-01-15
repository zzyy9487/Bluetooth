package com.example.blue

import android.bluetooth.BluetoothAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btserver)

        msgAdapter = MsgBTServerAdapter()
        recyclerViewMsg_BTServer.layoutManager = LinearLayoutManager(this)
        recyclerViewMsg_BTServer.adapter = msgAdapter

        val uuid = getUUID()

        Thread{
            try {
                while (true){
                    var mySocketOk = true

                    while (mySocketOk){
                        val mySocket = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("XDDD", uuid)
                        val socket = mySocket.accept()
                        mySocket.close()
                        var socketisconnecting = true

                        runOnUiThread{
                            textView_BTServer_status.text = "${socket.remoteDevice.name}(已連接)(${socket.remoteDevice.address})"
                        }

                        ImageBtn_BTServer_Send.setOnClickListener {
                            if (!socket.isConnected) return@setOnClickListener
                            val string :String = edit_BTServer_MsgInput.text.toString()
                            val dataOutput = DataOutputStream(socket.outputStream)
                            try {
                                dataOutput.writeUTF(string)
                                dataOutput.flush()
                            } catch (e: IOException){

                            }
                        }

                        while (socketisconnecting){
                            val dataInput = DataInputStream(socket.inputStream)
                            val toastWord :String? = dataInput.readUTF().toString()

                            if (!toastWord.isNullOrEmpty()){
                                runOnUiThread{
                                    Toast.makeText(this@BTServerActivity, toastWord, Toast.LENGTH_SHORT).show()
                                    msgAdapter.addMSG(toastWord)
                                    msgAdapter.notifyDataSetChanged()
                                }
                            }

                            if (!socket.isConnected) {
                                runOnUiThread{
                                    textView_BTServer_status.text = "斷線等待連線ing..."
                                }
                                socketisconnecting = false
                                mySocketOk = false
                            }

                        }
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
}
