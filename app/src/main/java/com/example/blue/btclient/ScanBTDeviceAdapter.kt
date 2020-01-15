package com.example.blue.btclient

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blue.R
import kotlinx.android.synthetic.main.cell_btclient.view.*

class ScanBTDeviceAdapter: RecyclerView.Adapter<ScanBTDeviceAdapter.ViewHolder>() {

    val myDevices: MutableList<BluetoothDevice> = mutableListOf()
    var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun bondedDevice(bonuedDev: BluetoothDevice)
        fun connectToDevice(connectDev: BluetoothDevice, position:Int)
    }

    fun setClickListener(listener: OnClickListener){
        this.onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_btclient, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myDevices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindviewholder(myDevices[position])
    }

    inner class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview){

        fun bindviewholder(btDevice:BluetoothDevice){
            val name = itemView.name
            val address = itemView.address
            val btBondState = btDevice.bondState
            val btn_paired = itemView.btn_paired
            val btn_connect = itemView.btn_connect
            name.text = btDevice.name ?: ""
            address.text = btDevice.address

            if (btBondState == 10){
                btn_paired.text = "可配對"
                btn_paired.isEnabled = true
            } else {
                btn_paired.text = "已配對"
                btn_paired.isEnabled = false
            }

            btn_paired.setOnClickListener {
                onClickListener?.bondedDevice(btDevice)
            }

            btn_connect.setOnClickListener {
                onClickListener?.connectToDevice(btDevice, adapterPosition)
            }

        }

    }

    fun addBondedBT(){
        myDevices.clear()
        myDevices.addAll(BluetoothAdapter.getDefaultAdapter().bondedDevices)
        notifyDataSetChanged()
    }

    fun addBT(bluetooth:BluetoothDevice){
        if (myDevices.contains(bluetooth)) return
        myDevices.add(bluetooth)
        notifyDataSetChanged()
    }

}