package com.mimuc.rww

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.RemoteInput
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent)
        val replyText = remoteInput.getCharSequence("notification_reply")
        //val challengeName = intent.getStringExtra("challengeName")
        Toast.makeText(context, replyText, Toast.LENGTH_SHORT).show()

        NotificationHelper.sendChallengeNotification(context, replyText.toString())

        // Broadcast to MyService for the challenge to upload
        val uploadIntent = Intent("upload_challenge_from_notification")
        uploadIntent.putExtra("answer", replyText.toString())
        LocalBroadcastManager.getInstance(context).sendBroadcast(uploadIntent)


    }
}