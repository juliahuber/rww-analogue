package com.mimuc.rww

import Category
import android.app.*
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.*
import android.content.Intent.*
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mimuc.rww.FirebaseConfig.Companion.cancelledChallengesRef
import com.mimuc.rww.FirebaseConfig.Companion.completedChallengesRef
import com.mimuc.rww.FirebaseConfig.Companion.deletedChallengesRef
import com.mimuc.rww.FirebaseConfig.Companion.logsRef
import com.mimuc.rww.FirebaseConfig.Companion.openChallengesRef
import com.mimuc.rww.FirebaseConfig.Companion.swappedChallengesRef
import io.sentry.Sentry
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MyService : Service() {

    val gson: Gson = Gson()
    var openChallenges = ArrayList<Challenge?>()
    //var chosenChallenges = ArrayList<Challenge?>()
    var challenges = mutableListOf<String?>()
    private lateinit var challengeName: String
    private var challenge: Challenge? = null
    var hidden = false

    //lateinit var balanceBefore: String
    //lateinit var recognized: String

    //TODO
    private lateinit var challengeCategory: Category
    private var challengePersonalized: Boolean = false
    private var currentDate: String = ""
    private var currentTime: String = ""
    private var answer = ""
    private var reason = ""
    private var lastUsedApp = ""

    var start: Long = 0
    var stop: Long = 0
    var result: Long = -1
    var challengeNotification: Notification? = null
    var challengeViaNotification: Boolean = false

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val CHANNEL_ID_ONE = "channel_id_one"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            Sentry.captureMessage(timeNow + "Service intent not null!")
            FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + ":  Service intent not null!")


            val sharedPrefs =  this.getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE)

        try {
            NotificationHelper.createServiceNotificationChannel(this)
            createServiceNotification()
        }
        catch(i: IllegalStateException) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService onStartCommand: " + i)
        }
        catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService onStartCommand: " + e)
        }


        //val inputStream: InputStream = assets.open("challenges.txt")
        //inputStream.bufferedReader().forEachLine { challenges.add(it) }

        if(!getCurrentChallengeListFromPref(gson, sharedPrefs).equals("")) {
            openChallenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
        }
        val list = openChallenges
        if(list.isNotEmpty()) {
            for (item in list) {
                if (item != null) {
                    challenges.add(item.title)
                }
            }
        }

        openChallengesRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(openChallenges.isNotEmpty()) {
                    openChallenges.clear()
                }
                try {
                    for (postSnapshot in snapshot.children) {
                        val challenge = postSnapshot.getValue(Challenge::class.java)
                        openChallenges.add(challenge)
                    }
                }
                catch(e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService 'openChallengesRef?.addValueEventListener': " + e)
                }
                if (openChallenges.isNotEmpty()){
                    try {
                        val lastPos = openChallenges.size - 1
                        val firstPos = openChallenges.size - openChallenges.size
                        if (challenge == null) {
                            challenge = Challenge(
                                //TODO
                                title = openChallenges[lastPos]?.title,
                                cat = openChallenges[lastPos]?.cat,
                                personalized = false,
                                time = openChallenges[lastPos]?.time,
                                date = openChallenges[lastPos]?.date,
                                notification = openChallenges[lastPos]?.notification,
                                viaNotification = openChallenges[lastPos]?.viaNotification,
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                                //balanceBefore = openChallenges[firstPos]?.balanceBefore,
                                //recognized = openChallenges[firstPos]?.recognized,

                                //balanceBefore = openChallenges[firstPos]?.balanceBefore,
                                //recognized = openChallenges[firstPos]?.recognized,
                            )
                        }
                    } catch (e: Exception) {
                        val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                        FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService 'if (openChallenges.isNotEmpty())': " + e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        } else {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            Sentry.captureMessage(timeNow + "Service intent null!")
            FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + ":  Service intent null!")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Toast.makeText(this, "Please restart the app", Toast.LENGTH_LONG).show();

        val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + ": onDestroy Service")
        FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + ": onDestroy Service activeNotificationSize: " +  manager.activeNotifications.toString())
    }

    private fun getCurrentChallengeListFromPref(gson: Gson, sharedPrefs: SharedPreferences?): ArrayList<Challenge?>  {
        val savedList = sharedPrefs?.getString("jsonChallenge", "?")
        val myType = object : TypeToken<ArrayList<Challenge?>>() {}.type
        val list = gson.fromJson<ArrayList<Challenge?>>(savedList, myType)
        return list
    }

    // this method doesn't go into NotificationHelper because startForeground only works in services (I think)
    private fun createServiceNotification() {
        try {
            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_ONE)
            val notification = notificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.challenge_flag)
                .setContentTitle("Real-World Wind is running in background.")
                .setContentText("Press here to log a new smartphone overload.")
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build()
            startForeground(2, notification)
        }
        catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService createServiceNotification() method: " + e)
        }
    }

    private fun uploadChallenge(){
        try {
            if (getLastUsedApp(challenge)) {
                setEndTime(challenge)
                challenge?.reason = null
                completedChallengesRef?.push()?.setValue(challenge)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            manager.cancel(notificationID)
            manager.cancelAll();
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Active notification size in upload nach cancel: "+manager.activeNotifications.size.toString())
            //NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
            challenge = null
        }
        catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService uploadChallenge() method: " + e)
        }
    }

    private fun uploadCancelledChallenge(){
        if (getLastUsedApp(challenge)){
            //challenge?.agreeHelped = null
            challenge?.agreeAwareness = null
            challenge?.agreeEnjoyed = null
            challenge?.agreeBored = null
            challenge?.agreeHappy = null
            challenge?.agreeAnnoyed = null
            //challenge?.agreeSame = null
            challenge?.agreeWellbeing = null
            challenge?.agreeBalance = null
            //challenge?.annoyedPleased = null
            //challenge?.sadHappy = null
            //challenge?.boredExcited = null
            challenge?.whichContext = null
            challenge?.answer = null
            //challenge?.balanceAfter = null
            stop = System.currentTimeMillis()
            result = stop - start
            val timeSinceUnlock: String = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(result),
                TimeUnit.MILLISECONDS.toSeconds(result) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
            )
            challenge?.timeSinceUnlock = timeSinceUnlock
            setEndTime(challenge)

            if(hidden) {
                deletedChallengesRef?.push()?.setValue(challenge)

            } else {
                cancelledChallengesRef?.push()?.setValue(challenge)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationID)
            manager.cancelAll();
            //NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
            challenge = null
        }
    }

    private fun uploadLog(recognized: String, howToCombat: String){
        try {
            val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
            val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
            currentDate = simpleDateFormatDate.format(Date())
            currentTime = simpleDateFormatTime.format(Date())
            challengeName = "Overload log"
            stop = System.currentTimeMillis()
            result = stop - start
            val timeSinceUnlock: String = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(result),
                TimeUnit.MILLISECONDS.toSeconds(result) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
            )
            challenge = Challenge(
                //TODO
                title = challengeName,
                cat = Category.MENTAL,
                personalized = false,
                time = currentTime,
                date = currentDate,
                answer = howToCombat,
                timeSinceUnlock = timeSinceUnlock,
                lastUsedApps = lastUsedApp,
                //recognized = recognized
            )
        }
        catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService uploadLog() method: " + e)
        }

        if(getLastUsedApp(challenge)){
            logsRef?.push()?.setValue(challenge)
        }
        //val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //manager.cancel(notificationID)
        //NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        challenge = null
    }

    private fun uploadSwappedChallenge(){
        try {
            //challenge?.agreeHelped = null
            challenge?.agreeAwareness = null
            challenge?.agreeEnjoyed = null
            challenge?.agreeBored = null
            challenge?.agreeHappy = null
            challenge?.agreeAnnoyed = null
            //challenge?.agreeSame = null
            challenge?.agreeWellbeing = null
            challenge?.agreeBalance = null
            //challenge?.annoyedPleased = null
            //challenge?.sadHappy = null
            //challenge?.boredExcited = null
            challenge?.whichContext = null
            challenge?.answer = null
            //challenge?.balanceAfter = null
            stop = System.currentTimeMillis()
            result = stop - start
            val timeSinceUnlock: String = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(result),
                TimeUnit.MILLISECONDS.toSeconds(result) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
            )
            challenge?.timeSinceUnlock = timeSinceUnlock
            setEndTime(challenge)

            swappedChallengesRef?.push()?.setValue(challenge)
        }
        catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService uploadSwappedChallenge() method: " + e)
        }
        //NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationID)
        manager.cancelAll();
        challenge = null
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sharedPrefs: SharedPreferences = context.getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE)
            when (intent.action){
                "cat_chosen" -> {
                    val relaxingChecked = intent.getExtras()?.getBoolean("relaxingChecked")
                    val mentalChecked = intent.getExtras()?.getBoolean("mentalChecked")
                    val physicalChecked = intent.getExtras()?.getBoolean("physicalChecked")
                    val socialChecked = intent.getExtras()?.getBoolean("socialChecked")
                    val organizingChecked = intent.getExtras()?.getBoolean("organizingChecked")
                    val miscChecked = intent.getExtras()?.getBoolean("miscChecked")
                    val personalizedChecked = intent.getExtras()?.getBoolean("personalizedChecked")
                    val randomChecked = intent.getExtras()?.getBoolean("randomChecked")
                    openChallenges = getChosenChallenges(sharedPrefs, relaxingChecked, mentalChecked, physicalChecked, socialChecked, organizingChecked, miscChecked, personalizedChecked, randomChecked)
                    challenges.clear()
                    if(openChallenges != null) {
                        for (item in openChallenges) {
                            if (item != null) {
                                challenges.add(item.title)
                            }
                        }
                    }

                    val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
                    val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
                    currentDate = simpleDateFormatDate.format(Date())
                    currentTime = simpleDateFormatTime.format(Date())

                    if(openChallenges != null) {
                        var randomChallenge = openChallenges.random()
                        challengeName = randomChallenge?.title.toString()
                        challengeCategory = randomChallenge?.cat!!
                        var viaNotification = randomChallenge?.viaNotification
                        var notification = randomChallenge?.notification

                        challenge = Challenge(
                            title = challengeName,
                            cat = challengeCategory,
                            personalized = false,
                            time = currentTime,
                            date = currentDate,
                            notification = notification,
                            viaNotification = viaNotification,
                            answer = answer,
                            reason = reason,
                            lastUsedApps = lastUsedApp,
                            //recognized = recognized,
                            //balanceBefore = balanceBefore
                        )

                        openChallengesRef?.push()?.setValue(challenge)
                    }

                    //NotificationHelper.createChallengeNotificationChannel(context)
                    //NotificationHelper.sendChallengeNotification(context, challengeName)



                }
                "fab" -> {
                        //balanceBefore = intent.getStringExtra("balanced").toString()
                        //recognized = intent.getStringExtra("howRecognized").toString()
                }
                "intentKey" -> {
                    if(checkUsageStatsPermission()){
                        val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
                        val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
                        currentDate = simpleDateFormatDate.format(Date())
                        currentTime = simpleDateFormatTime.format(Date())
                        challengeName = challenges.random().toString()
                        val challengeNotification = null
                        val challengeViaNotification = false
                        //TODO
                        challengeCategory = Category.MENTAL
                        challengePersonalized = false
                        challenge = Challenge(challengeName, challengeCategory, challengePersonalized, currentTime, currentDate, challengeNotification, challengeViaNotification, answer, reason, lastUsedApp)
                        openChallengesRef?.push()?.setValue(challenge)
                        //NotificationHelper.createChallengeNotificationChannel(context)
                        //NotificationHelper.sendChallengeNotification(context, challengeName)
                    }
                    else{
                        try {
                            startActivity(
                                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(
                                    FLAG_ACTIVITY_NEW_TASK
                                )
                            )
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())

                            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": startActivity in MyService intentKey")

                        }
                        catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService mMessageReceiver->intentKey 'else { startActiviy() }': " + e)
                        }
                    }
                }
            }
        }
    }

    private val mCompletedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sharedPrefs: SharedPreferences = context.getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE)
            when (intent.action){
                "completed" -> {
                    try {
                        val challenge = challenge.let { it } ?: return
                    } catch(e: Exception) {
                        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                        FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService mCompletedReceiver->completed 'challenge.let { it } ?: return': " + e)
                    }

                    challenge?.answer = intent.getStringExtra("answer")

                    stop = System.currentTimeMillis()
                    result = stop - start
                    val timeSinceUnlock: String = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(result),
                        TimeUnit.MILLISECONDS.toSeconds(result) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
                    )
                    challenge?.timeSinceUnlock = timeSinceUnlock
                }
                "completed_fn" -> {
                    var isFromNotification: Boolean? = intent.getExtras()?.getBoolean("is_from_nf")

                    challenge = null
                    if (isFromNotification == true) {
                        val notificationChallenge = intent.getSerializableExtra("notification_challenge") as Challenge
                        challenge = notificationChallenge
                    }
                    challenge?.answer = intent.getStringExtra("answer")

                    stop = System.currentTimeMillis()
                    result = stop - start
                    val timeSinceUnlock: String = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(result),
                        TimeUnit.MILLISECONDS.toSeconds(result) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
                    )
                    challenge?.timeSinceUnlock = timeSinceUnlock

                    val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
                    val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
                    currentDate = simpleDateFormatDate.format(Date())
                    currentTime = simpleDateFormatTime.format(Date())
                    challenge?.date = currentDate
                    challenge?.time = currentTime

                    //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    //notificationManager.cancel(notificationID)
                    val intent = Intent("completed_challenge_fn")
                    context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                }
                "upload_challenge_from_notification" -> {
                    try {
                        val challenge = challenge.let { it } ?: return
                    }
                    catch(e: Exception) {
                        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                        FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": : Exception In MyService mCompletedReceiver->upload_challenge_from_notification 'challenge.let { it } ?: return': " + e)
                    }
                    challenge?.answer = intent.getStringExtra("answer")

                    stop = System.currentTimeMillis()
                    result = stop - start
                    val timeSinceUnlock: String = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(result),
                        TimeUnit.MILLISECONDS.toSeconds(result) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(result))
                    )
                    challenge?.timeSinceUnlock = timeSinceUnlock
                    uploadChallenge()
                }
                "evaluate_challenge" -> {
                    try {
                        val challenge = challenge.let { it } ?: return
                    }
                    catch(e: Exception) {
                        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                        FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService mCompletedReceiver->evaluate_challenge 'challenge.let { it } ?: return': " + e)
                    }
/**
                    challenge.balanceAfter = intent.getStringExtra("balanced")
                    challenge.sadHappy = intent.getStringExtra("sadHappy")
                    challenge.boredExcited = intent.getStringExtra("boredExcited")
                    challenge.annoyedPleased = intent.getStringExtra("annoyedPleased")
**/
                    //challenge?.agreeHelped = intent.getStringExtra("agreeHelped")
                    challenge?.agreeAwareness = intent.getStringExtra("agreeAwareness")
                    challenge?.agreeEnjoyed = intent.getStringExtra("agreeEnjoyed")
                    challenge?.agreeBored = intent.getStringExtra("agreeBored")
                    challenge?.agreeHappy = intent.getStringExtra("agreeHappy")
                    challenge?.agreeAnnoyed = intent.getStringExtra("agreeAnnoyed")
                    //challenge?.agreeSame = intent.getStringExtra("agreeSame")
                    challenge?.agreeWellbeing = intent.getStringExtra("agreeWellbeing")
                    challenge?.agreeBalance = intent.getStringExtra("agreeBalance")

                    challenge?.whichContext = intent.getStringExtra("whichContext")
                    uploadChallenge()
                }
                "send_reason" -> {
                    try {
                        val challenge = challenge.let { it } ?: return
                    }
                    catch(e: Exception) {
                        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                        FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService mCompletedReceiver->send_reason 'challenge.let { it } ?: return': " + e)
                    }
                    challenge?.reason = intent.getStringExtra("reason")
                    uploadSwappedChallenge()
                }
                "send_challenge_fn" -> {
                    if(!challengeInitialized()) {
                        challenge = intent.getSerializableExtra("challengeFromNotification") as Challenge
                    }
                }
                "send_hidden_challenge"-> {
                    val gotHidden = intent.getExtras()?.getBoolean("got_hidden")
                    val unlovedChallenge = challenge
                    val editor = sharedPrefs.edit()
                    if (gotHidden == true) {
                        alwaysHide(gson, sharedPrefs, editor, challenge)
                        hidden = true
                    } else {
                        hidden = false
                    }
                    challenge = unlovedChallenge
                }
                "send_cancel_reason" -> {
                    challenge?.reason = intent.getStringExtra("cancel_reason")
                    challenge?.feltOverload = intent.getExtras()?.getBoolean("check_feel_overload")
                    uploadCancelledChallenge()
                    challenge = null

                }
                "new_log" -> {
                    val recognized = intent.getStringExtra("howRecognized")
                    val howToCombat = intent.getStringExtra("howToCombat")
                    if (recognized != null && howToCombat != null) {
                        uploadLog(recognized, howToCombat)
                    }
                }
            }
        }
    }

    private fun challengeInitialized(): Boolean {
        return challenge != null
    }

    private fun getLastUsedApp(challenge: Challenge?): Boolean {
        var success = false
            if (checkUsageStatsPermission()) {
                val usageStatsManager: UsageStatsManager =
                    getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val cal: Calendar = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_MONTH, -1)
                val queryUsageStats: List<UsageStats> = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    cal.timeInMillis,
                    System.currentTimeMillis()
                )
                val statsData = ArrayList<String>()

                // nur diejenigen, die länger als 1 sekunde im foreground
                try {
                    for (i in 0..queryUsageStats.size - 1) {
                        if (
                            convertTime(queryUsageStats[i].lastTimeUsed) != "01/01/1970 01:00:00" &&
                            !(queryUsageStats[i].packageName.contains("com.google")) &&
                            !(queryUsageStats[i].packageName.contains("com.android")) &&
                            !(queryUsageStats[i].packageName.contains("com.oneplus")) &&
                            !(queryUsageStats[i].packageName.contains("net.oneplus"))
                        ) {
                            statsData.add(
                                "Package Name: " + queryUsageStats[i].packageName + " --- " +
                                        "Last Time Used: " + convertTime(queryUsageStats[i].lastTimeUsed) + "<br>"
                            )
                        }
                    }
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService getLastUsedApps() method for-loop: " + e)
                }
                challenge?.lastUsedApps = statsData.toString()
                success = true

            } else {
                try {
                    startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(
                            FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService getLastUsedApps 'else startActivity()': " + e)
                }
                success = false
            }
        return success
    }

    private fun convertTime(lastTimeUsed: Long): String {
        val date = Date(lastTimeUsed * 1L)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.GERMAN)
        return format.format(date)
    }


    private fun checkUsageStatsPermission(): Boolean {
        var appOpsManager: AppOpsManager? = null
        var mode = 0
        appOpsManager = getSystemService(Context.APP_OPS_SERVICE)!! as AppOpsManager
        mode = appOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, applicationInfo.uid, packageName)
        return mode == MODE_ALLOWED
    }

    private fun phoneUnlockReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)
        val screenOnOffReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val strAction = intent.action
                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (strAction == Intent.ACTION_USER_PRESENT && keyguardManager.isKeyguardSecure) {
                    Log.d("Smartphone Unlock", "UNLOCKED")
                    start = System.currentTimeMillis()
                } else {
                    Log.d("Smartphone Unlock", "LOCKED")
                }
            }
        }
        applicationContext.registerReceiver(screenOnOffReceiver, intentFilter)
    }

    private fun getChosenChallenges(sharedPrefs: SharedPreferences?, relaxingChecked: Boolean?, mentalChecked: Boolean?, physicalChecked: Boolean?, socialChecked: Boolean?, organizingChecked: Boolean?, miscChecked: Boolean?, personalizedChecked: Boolean?, randomChecked: Boolean?): ArrayList<Challenge?> {
        val existingChallenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
        var chosenChallenges: ArrayList<Challenge?> = ArrayList()
        val personalizedChallenges: ArrayList<Challenge?> = ArrayList()

        if (randomChecked == true) {
            return existingChallenges
        } else {
            if (personalizedChecked == true) {
                for (item in existingChallenges) {
                    if (item?.personalized == true) {
                        personalizedChallenges.add(item)
                    }
                }
                for (item in personalizedChallenges) {
                    if (item?.cat?.equals(Category.RELAXING) == true && relaxingChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.MENTAL) == true && mentalChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.PHYSICAL) == true && physicalChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.SOCIAL) == true && socialChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.ORGANIZING) == true && organizingChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.MISC) == true && miscChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if ((relaxingChecked == false)&&
                        (mentalChecked == false)&&
                        (physicalChecked == false)&&
                        (socialChecked == false)&&
                        (miscChecked == false)&&
                        (organizingChecked == false)) {
                        chosenChallenges = personalizedChallenges
                    }
                }
            } else {
                for (item in existingChallenges) {
                    if (item?.cat?.equals(Category.RELAXING) == true && relaxingChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.MENTAL) == true && mentalChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.PHYSICAL) == true && physicalChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.SOCIAL) == true && socialChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.ORGANIZING) == true && organizingChecked == true) {
                        chosenChallenges.add(item)
                    }
                    if (item?.cat?.equals(Category.MISC) == true && miscChecked == true) {
                        chosenChallenges.add(item)
                    }
                }
            }
            return chosenChallenges
        }
    }

    private fun removeChallengeFromPrefs(gson:Gson, sharedPrefs:SharedPreferences?, editor:SharedPreferences.Editor?, unlovedChallenge: Challenge?): ArrayList<Challenge?> {
        val list = getCurrentChallengeListFromPref(gson, sharedPrefs)
        var key = -1
        for (item in list) {
            key = key + 1
            if (item?.title == unlovedChallenge?.title) {
                break
            }
        }

        list.removeAt(key)

        val json:String = gson.toJson(list)
        editor?.putString("jsonChallenge", json)
        editor?.commit()
        return list
    }

    private fun alwaysHide(gson:Gson, sharedPrefs:SharedPreferences?, editor:SharedPreferences.Editor?, unlovedChallenge:Challenge?) {
        removeChallengeFromPrefs(gson, sharedPrefs, editor, unlovedChallenge)
    }

    private fun setEndTime(finishedChallenge: Challenge?) {
        val simpleDateFormatDate = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
        val simpleDateFormatTime = SimpleDateFormat("HH:mm:ss", Locale.GERMAN)
        currentDate = simpleDateFormatDate.format(Date())
        currentTime = simpleDateFormatTime.format(Date())
        finishedChallenge?.date = currentDate
        finishedChallenge?.endTime = currentTime
    }

    override fun onCreate() {
        super.onCreate()

        try {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ":  Service onCreate")
            NotificationHelper.createServiceNotificationChannel(this)
            createServiceNotification()
        }
        catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MyService onCreate createServiceNotification(): " + e)
        }
        //phoneUnlockReceiver()

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("intentKey"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("fab"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("cat_chosen"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("completed"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("completed_fn"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("upload_challenge_from_notification"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("send_reason"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("send_cancel_reason"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("send_hidden_challenge"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("send_challenge_fn"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("evaluate_challenge"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("new_log"))
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReceiver, IntentFilter("quick_tile_log"))
        }

}






