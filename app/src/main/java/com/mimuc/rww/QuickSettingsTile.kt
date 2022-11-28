package com.mimuc.rww

import androidx.annotation.RequiresApi
import android.os.Build
import android.service.quicksettings.TileService
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.drawable.Icon
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mimuc.rww.R
import com.mimuc.rww.LoginActivity.Companion.SHARED_PREFS

@RequiresApi(api = Build.VERSION_CODES.N)
class QuickSettingsTile : TileService() {
    override fun onTileAdded() {
        super.onTileAdded()
    }

    override fun onStartListening() {
        super.onStartListening()
        val icon = Icon.createWithResource(applicationContext, R.drawable.challenge_flag)
        qsTile.icon = icon
        qsTile.updateTile()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onClick() {
        super.onClick()
        sendMessageToService()
    }

    private fun sendMessageToService() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val userIdStartsWith = sharedPreferences.getString(LoginActivity.USER_ID, "")?.startsWith("1") == true
        if (userIdStartsWith){
            //val intent = Intent("new_log")
            //LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.addFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent2)
        } else {
            val intent = Intent("intentKey")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

}