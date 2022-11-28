package com.mimuc.rww

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.Sentry
import java.text.SimpleDateFormat
import java.util.*


const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class ScreenNotificationReceiver : BroadcastReceiver() {
    var challenge: Challenge = Challenge()
    val gson: Gson = Gson()
    var notificationObj = Notification()


    @SuppressLint("ResourceAsColor")
    override fun onReceive(context: Context, intent: Intent) {
        println("received")
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
        FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": on Overload Receive, active notification size: "+notificationManager.activeNotifications.size.toString())

        if(notificationManager.activeNotifications.size <= 1) {
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Overload Received in ScreenNotificationReceiver: notification in the making")
            Sentry.captureMessage("$timeNow: Overload Received in ScreenNotificationReceiver: notification in the making")

            val message: String? = intent.getStringExtra("toastMessage")

            val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
            val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
            var date = simpleDateFormatDate.format(Date())
            var time = simpleDateFormatTime.format(Date())
            val triggerTime = System.currentTimeMillis()

            notificationObj.triggerTime = convertTime(triggerTime)
            notificationObj.trigger = message


            val sharedPrefs: SharedPreferences =
                context.getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE)
            val challenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
            challenge = challenges.shuffled()[0]
            challenge.viaNotification = true
            challenge.notification = notificationObj

            challenge.date = date.toString()
            challenge.time = time.toString()
            println("shuffled challenge is: " + challenge.title)
            FirebaseConfig.openChallengesRef?.push()?.setValue(challenge)

            val activityIntent = Intent(context, MainActivity::class.java)
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, activityIntent, FLAG_IMMUTABLE)


            val cancelIntent = Intent(context, MainActivity::class.java)
            cancelIntent.action = "challenge_received"
            cancelIntent.putExtra("firstKeyName", "send_cancel_fn")
            cancelIntent.putExtra("randomChallengeIntent", challenge)
            val cancelPendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 1, cancelIntent, FLAG_UPDATE_CURRENT)


            val exchangeIntent = Intent(context, MainActivity::class.java)
            exchangeIntent.action = "challenge_exchanged"
            exchangeIntent.putExtra("firstKeyName", "send_exchange_fn")
            exchangeIntent.putExtra("exchangedChallengeIntent", challenge)
            val exchangePendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 1, exchangeIntent, FLAG_UPDATE_CURRENT)

            //val img: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test)
            challenge.cat?.let { println(it.iconSmall.toString()) }
            val img: Bitmap? =
                challenge.cat?.let { BitmapFactory.decodeResource(context.resources, it.iconSmall) }


            val remoteInput: androidx.core.app.RemoteInput? =
                androidx.core.app.RemoteInput.Builder("key_challenge_reply")
                    .setLabel("Your answer...")
                    .build()

            val replyIntent = Intent(context, MainActivity::class.java)
            replyIntent.action = "challenge_replied"
            replyIntent.putExtra("notification_challenge", challenge)
            replyIntent.putExtra("start_time", System.currentTimeMillis())

            val replyPendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 1, replyIntent, FLAG_UPDATE_CURRENT)

            val replyAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_reply,
                "Reply",
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()


            try {
                val notification = NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(R.drawable.ic_wind)
                    .setLargeIcon(img)
                    .setContentTitle(intent.getStringExtra(titleExtra))
                    .setContentText(intent.getStringExtra(messageExtra))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle("Take a challenge!")
                            .bigText(challenge?.title)
                            .setSummaryText("Potential Smartphone Overload")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setColor(R.color.primary)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)//tab notification: will dismiss
                    .setOnlyAlertOnce(false)
                    .addAction(R.drawable.ic_cancel, "Cancel", cancelPendingIntent)
                    .addAction(R.drawable.ic_arrows, "Exchange", exchangePendingIntent)
                    .addAction(replyAction)
                    .build()

                notification.flags = NotificationCompat.FLAG_NO_CLEAR

                notificationManager.notify(notificationID, notification)

                for(notification in notificationManager.activeNotifications)
                    FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Existing Notifications are: NOTIFICATION: " + notification.toString())

            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenNotificationReceiver 'notification = ': " + e)
            }
        }
    }

    //TODO: diese Funktion auslagern!!
    private fun getCurrentChallengeListFromPref(gson: Gson, sharedPrefs: SharedPreferences?): ArrayList<Challenge> {
        val savedList = sharedPrefs?.getString("jsonChallenge", "?")
        val myType = object : TypeToken<ArrayList<Challenge>>() {}.type
        val list = gson.fromJson<ArrayList<Challenge>>(savedList, myType)
        return list
    }

    private fun convertTime(lastTimeUsed: Long): String {
        val date = Date(lastTimeUsed * 1L)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.GERMAN)
        return format.format(date)
    }
}