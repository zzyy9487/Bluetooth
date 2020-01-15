package com.example.blue.btclient

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blue.R
import kotlinx.android.synthetic.main.cell_msg.view.*

class MsgBTClientAdapter: RecyclerView.Adapter<MsgBTClientAdapter.ViewHolder>() {

    val myMSG = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_msg, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myMSG.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindviewholder(myMSG[position])
    }

    inner class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview){

        fun bindviewholder(string: String){
            val msg = itemView.text_MSG
            msg.text = string
        }

    }

    fun addMSG(string: String?){
        if (string == null || string == "") return
        myMSG.add(string)
        notifyDataSetChanged()
    }

}