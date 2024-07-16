package com.appdev.printitout.utils

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.google.gson.Gson

class Utils(private val context: Context) {

    private val PREFS_NAME = "snooze_timers"

    private val API_KEY = "01100001"
    private val CODICE_REPARTO = "01000011"
    private val PRINTER = "100110011"

    val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveKey(key: String) {
        val editor = sharedPref.edit()
        editor.putString(API_KEY, key)
        editor.apply()
    }

    fun getKey(): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theKey = sharedPref.getString(API_KEY, null)
        return if (theKey.isNullOrEmpty()) "d05c0260748065e454cc7a" else theKey
    }

    fun saveCodice(codice: String) {
        val editor = sharedPref.edit()
        editor.putString(CODICE_REPARTO, codice)
        editor.apply()
    }

    fun getCodice(): String {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = sharedPref.getString(CODICE_REPARTO, null)
        return if (key.isNullOrEmpty()) "652-404" else key
    }

    fun savePrinter(key: BluetoothDevice) {
        val editor = sharedPref.edit()
        val json = Gson().toJson(key)
        editor.putString(PRINTER, json)
        editor.apply()
    }

    fun getPrinter(): BluetoothDevice? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val thePrinterString = sharedPref.getString(PRINTER, null)
        return if(thePrinterString.isNullOrEmpty()) null else Gson().fromJson(thePrinterString, BluetoothDevice::class.java)
    }

}
