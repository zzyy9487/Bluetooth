package com.example.blue.bleserver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blue.R
import kotlinx.android.synthetic.main.cell_bleserver.view.*

class ScanBLEDeviceAdapter: RecyclerView.Adapter<ScanBLEDeviceAdapter.ViewHolder>() {

    val bleDevices: MutableList<BLEItem> = mutableListOf()
    val bleList:MutableList<BluetoothDevice> = mutableListOf()
//    var onClickListener: OnClickListener? = null

//    interface OnClickListener {
//        fun bondorconnect(dev: BluetoothDevice, position:Int)
//    }
//
//    fun setClickListener(listener: OnClickListener){
//        this.onClickListener = listener
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_bleserver, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bleDevices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindviewholder(bleDevices[position])
    }

    inner class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview){

        fun bindviewholder(bleDevice:BLEItem){
            val name = itemView.nameBLE
            val address = itemView.addressBLE
            val rssi = itemView.rssiBLE
            val scanRecord = itemView.scanRecordBLE
            val btBondState = bleDevice.bluetoothDevice.bondState
            name.text = bleDevice.bluetoothDevice.name ?: "Null"
            address.text = String.format("%s (%s)", bleDevice.bluetoothDevice.address, if (btBondState == 10) "未配对" else "已配对")
            rssi.text = bleDevice.rssi.toString()
            scanRecord.text = bleDevice.scanRecord.toString()
//            itemView.setOnClickListener {
//                onClickListener?.bondorconnect(btDevice, adapterPosition)
//            }

        }

    }

//    fun addBondedBT(){
//        bleDevices.clear()
//        bleDevices.addAll(BluetoothAdapter.getDefaultAdapter().bondedDevices)
//        notifyDataSetChanged()
//    }

    fun addBLE(bleDevice:BluetoothDevice, rssi:Int, scanRecord:ByteArray){
        if (bleList.contains(bleDevice) ) return
        bleList.add(bleDevice)
        bleDevices.add(BLEItem(bleDevice, rssi, scanRecord))
        notifyDataSetChanged()
    }

}