/**@file:Suppress("DEPRECATION")

package com.mimuc.rww

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi


class WifiReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(context)
                .setMessage("gps network not enabled")
                .setPositiveButton("open location settings",
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        context.startActivity(
                            Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton("cancel", null)
                .show()
        }

        println("wifireceiver here")
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            scanSuccess(wifiManager)

            getCurrentSSID(context, wifiManager)


        } else {
            scanFailure(wifiManager)
        }
    }


    private fun scanSuccess(wifiManager: WifiManager) {
        val scanResults: List<ScanResult> = wifiManager.scanResults
        println("here come the scanresults!!")
        for(item in scanResults) {
            println(item.toString())
        }
        println("here comes the scanresults size!!: "+scanResults.size.toString())

    }

    private fun scanFailure(wifiManager: WifiManager) {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        // potentially use older scan results
    }


    fun getCurrentSSID(context: Context, wifiManager: WifiManager): String? {
        try {
            if (wifiManager != null) {
                //if version <28
                val wifiInfo = wifiManager.connectionInfo
                println("wifiinfo: "+wifiInfo.toString()+" wifiSSID: "+wifiInfo.getSSID().toString())
            }
        } catch (e: Exception) {
            println("Get Wifi Manager" + "Something went wrong while fetching current SSID:")
            e.printStackTrace()
        }
        return null
    }
}
 **/