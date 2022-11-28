package com.mimuc.rww

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.Sentry
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

const val MAX_SCREENTIME:Int = 720   //720s =  12mins
const val MAX_NUMBER_OF_UNLOCKS:Int = 6    //6
const val TIME_RESET_FOR_UNLOCKS:Int = 1800   //1800s = 30mins

class ScreenReceiver : BroadcastReceiver() {
    var minute = 0
    var hour = 0
    var day = 0
    var month = 0
    var year = 0

    val timestamps : MutableList<Long> = mutableListOf()

    val gson: Gson = Gson()

    override fun onReceive(context: Context, intent: Intent?) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE)
        val challenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, ScreenNotificationReceiver::class.java)

        println("ScreenTimeService onReceive")

        if (intent?.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            println("SCREEN ON")
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": SCREEN ON")
            try {
                createNotificationChannel(context)
                showNotification(context, "screentime", alarmManager, notificationIntent)
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver 'intent?.getAction().equals(Intent.ACTION_SCREEN_ON) showNotification': " + e)
            }
        }
        if (intent?.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            println("SCREEN OFF")
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": SCREEN OFF")
            try {
                stopTimer(context, alarmManager, notificationIntent)
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver 'intent?.getAction().equals(Intent.ACTION_SCREEN_OFF)': " + e)
            }
        }
        if (intent?.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            println("UNLOCK USER PRESENT")
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": UNLOCK USER PRESENT")
            val nowUnix = System.currentTimeMillis() / 1000L
            timestamps.add(nowUnix)
            println(timestamps.toString())
            var index = 0
            var timestampRemoved = false

            try {
                for ((i, timestamp) in timestamps.asReversed().withIndex()) {
                    if (isTimeExceeded(timestamp, nowUnix, TIME_RESET_FOR_UNLOCKS)) {
                        index = timestamps.size - i - 1
                        timestampRemoved = true
                        break
                    } else {
                        timestampRemoved = false
                    }
                }
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver 'intent?.getAction().equals(Intent.ACTION_USER_PRESENT) for-loop timestamps': " + e)
            }

            try {
                if (timestampRemoved) {
                    for (x in 0..index) {
                        timestamps.removeAt(0)
                    }
                }
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver 'intent?.getAction().equals(Intent.ACTION_USER_PRESENT) if timestampRemoved': " + e)
            }

            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": timestamps.size: "+timestamps.size.toString())
            if (timestamps.size == MAX_NUMBER_OF_UNLOCKS) {
                println("found potential smartphone overload by too many unlocks per time")
                Sentry.captureMessage(timeNow + ": ScreenReceiver: Found potential smartphone overload by too many unlocks per time")
                timestamps.clear()
                try {
                    val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                    FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Found potential smartphone overload by too many unlocks per time")
                    showNotification(
                        context,
                        "number of unlocks",
                        alarmManager,
                        notificationIntent
                    )
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver 'intent?.getAction().equals(Intent.ACTION_USER_PRESENT) showNotification': " + e)
                }
            }
        }
    }

    private fun showNotification(context:Context, reason: String, alarmManager:AlarmManager, notificationIntent:Intent) {
            val title = "Potential smarthone overload"
            val message = "caused by " + reason

            notificationIntent.putExtra(notificationID.toString(), 1)
            notificationIntent.putExtra(titleExtra, title)
            notificationIntent.putExtra(messageExtra, message)
            notificationIntent.putExtra("reason", reason)
            notificationIntent.putExtra(
                "toastMessage",
                "Potential smartphone overload " + message + " detected"
            )

            var time: Long = 0
            if (reason == "screentime") {
                time = setTimer(MAX_SCREENTIME)
                //showAlert(context, time, title, message)
            } else if (reason == "number of unlocks") {
                time = setCalender().timeInMillis
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationID,
                notificationIntent,
                FLAG_UPDATE_CURRENT
                //PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT

            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, time, pendingIntent
            )
        //   notificationManager.notify(1, notification)
    }


    private fun showAlert(context: Context?, time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormate = android.text.format.DateFormat.getLongDateFormat(context)
        val timeFormate = android.text.format.DateFormat.getTimeFormat(context)

        AlertDialog.Builder(context)
            .setTitle("Smartphone Overload")
            .setMessage(
                title + "\n" + message + "\nAt: " + dateFormate.format(date) + " " + timeFormate.format(date))
            .setPositiveButton("Okay"){_,_->}
            .show()
    }

    private fun setCalender(): Calendar {
        val timestamp = LocalDateTime.now().toString() //2017-08-02T11:25:44.973
        minute = timestamp.substring(14, 16).toInt()
        hour = timestamp.substring(11, 13).toInt()
        day = timestamp.substring(8, 10).toInt()
        month = timestamp.substring(5, 7).toInt()-1
        year = timestamp.substring(0, 4).toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar
    }

    private fun setTimer(screentime:Int): Long {
        val calendar = setCalender()
        calendar.add(GregorianCalendar.SECOND, screentime)
        return calendar.timeInMillis
    }

    private fun stopTimer(context:Context, alarmManager: AlarmManager, notificationIntent:Intent): Long {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationID,
            notificationIntent,
            FLAG_UPDATE_CURRENT
                    //PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)

        minute = 0
        hour = 0
        day = 0
        month = 0
        year = 0

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }

    //return true if time difference > 30min: then timestamp will be dropped from list
    private fun isTimeExceeded(timestamp: Long, now:Long, duration:Int):Boolean {
        return timestamp + duration < now
    }

    private fun createNotificationChannel(context: Context?) {
        val name = "Notif Channel"
        val desc = "This is our notification channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc

        println("create notification channel")

        try {
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            println("created notification channel")

        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In ScreenReceiver createNotificationChannel() method: " + e)
        }
    }

    //TODO: diese Funktion auslagern!!
    private fun getCurrentChallengeListFromPref(gson: Gson, sharedPrefs: SharedPreferences?): ArrayList<Challenge>  {
        val savedList = sharedPrefs?.getString("jsonChallenge", "?")
        val myType = object : TypeToken<ArrayList<Challenge>>() {}.type
        val list = gson.fromJson<ArrayList<Challenge>>(savedList, myType)
        return list
    }
}