package com.example.talk

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi


open class InternetConnectivity {
    @RequiresApi(Build.VERSION_CODES.M)
    fun isConnected(applicationContext: Context): Boolean {
        var connected = false
        try {
            val cm =
                applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val nInfo = cm.activeNetworkInfo
            connected = nInfo != null && nInfo.isAvailable && nInfo.isConnected
            return connected
        } catch (e: Exception) {
            Log.e("Loggg", e.message!!)
//            Toast.makeText(this,"Not working",Toast.LENGTH_LONG).show()
        }
        return connected
    }
}