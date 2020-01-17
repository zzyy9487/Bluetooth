package com.example.blue.bleserver

import android.bluetooth.BluetoothDevice

data class BLEItem(var bluetoothDevice: BluetoothDevice, var rssi:Int, var scanRecord:ByteArray ) {
}