package com.mimuc.rww

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

const val reminderID = 9999
const val reminderChannelID = "reminderChannel"
const val reminderTitleExtra = "reminderTitleExtra"
const val reminderMessageExtra = "reminderMessageExtra"

class DailyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        println("in onReceive DailyReminder")

        val reminderIntent = Intent(context, MainActivity::class.java)
        reminderIntent.putExtra("firstKeyName", "alarmintent")
        val pendingIntent = PendingIntent.getActivity(context, 0, reminderIntent, FLAG_UPDATE_CURRENT)

        try {
            val reminder: Notification? =
                context?.let {
                    NotificationCompat.Builder(it, reminderChannelID)
                        .setSmallIcon(R.drawable.ic_reminder_notification)
                        .setContentTitle(intent.getStringExtra(reminderTitleExtra))
                        .setContentText(intent.getStringExtra(reminderMessageExtra))
                        .setContentIntent(pendingIntent)
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(intent.getStringExtra(reminderMessageExtra)))
                        .setAutoCancel(true)
                        .build()
                }

            if (reminder != null) {
                reminder.flags = NotificationCompat.FLAG_NO_CLEAR
            }

            val manager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify("reminderId",reminderID, reminder)

        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In DailyReceiver 'notification = ': " + e)
        }
    }

}