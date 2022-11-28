package com.mimuc.rww

import Category
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mimuc.rww.FirebaseConfig.Companion.completedChallengesRef
import com.mimuc.rww.FirebaseConfig.Companion.logsRef
import com.mimuc.rww.FirebaseConfig.Companion.myRootRef
import com.mimuc.rww.LoginActivity.Companion.SHARED_PREFS
import com.mimuc.rww.LoginActivity.Companion.USER_ID
import com.mimuc.rww.LoginActivity.Companion.id
import io.sentry.Sentry
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    var challenges = ArrayList<Challenge?>()
    val recyclerViewAdapter = RecyclerViewAdapter(challenges)
    var currentChallengeActive: Boolean = false
    var challenge: Challenge? = null
    val screentimeReceiver = ScreenReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {

        println("main in oncreate1")

        /**
        Firebase.messaging.subscribeToTopic("test-notification")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                println(msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
**/
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState)

        println("main in oncreate2")


        FirebaseConfig.debugUnlocksRef?.push()?.setValue("in Main onCreate")

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate setContentView(): " + e)
        }
        //startService(Intent(this, MyService::class.java))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val serviceIntent = Intent(this, MyService::class.java)
                this.startForegroundService(serviceIntent)
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate startForegroundService(): " + e)
            }
        } else {
            try {
                val serviceIntent = Intent(this, MyService::class.java)
                this.startService(serviceIntent)
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()
                    ?.setValue(timeNow + ": Exception In MainActivity onCreate startService(): " + e)
            }
        }


        val remoteInputBundle: Bundle? = RemoteInput.getResultsFromIntent(intent)
        if (remoteInputBundle != null) {
            val replyText: CharSequence? = remoteInputBundle?.getCharSequence("key_challenge_reply")
            challenge = (intent.getSerializableExtra("notification_challenge") as Challenge?)

            val newIntent = Intent("completed_fn")

            val answer: String = replyText.toString()
            newIntent.putExtra("answer", answer)
            newIntent.putExtra("is_from_nf", true)
            newIntent.putExtra("notification_challenge", challenge)

            let { LocalBroadcastManager.getInstance(this).sendBroadcast(newIntent) }
        }

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    this.startForegroundService(Intent(this, MyService::class.java))
        //} else {
        //    this.startService(Intent(this, MyService::class.java))
        //}

        val receivedIntent = intent // gets the previously created intent
        val firstKeyName = receivedIntent.getStringExtra("firstKeyName")

        if (firstKeyName == "send_cancel_fn") {
            challenge = (receivedIntent.getSerializableExtra("randomChallengeIntent") as Challenge)

            val intentCancelInMain = Intent("cancel_challenge_fn")
            intentCancelInMain.putExtra("challengeIntentForService", challenge)
            intentCancelInMain.putExtra("validateStringForService", "validate_service")
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, IntentFilter("cancel_challenge_fn"))

            this.let { LocalBroadcastManager.getInstance(this).sendBroadcast(intentCancelInMain) }
        }
        if (intent.getStringExtra("firstKeyName") == "send_exchange_fn") {

            challenge =
                (receivedIntent.getSerializableExtra("exchangedChallengeIntent") as Challenge)

            val intentExchangedInMain = Intent("switch")
            intentExchangedInMain.putExtra("challengeIntentForService", challenge)
            intentExchangedInMain.putExtra("validateStringForService", "validate_service")
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, IntentFilter("switch"))

            this.let {
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentExchangedInMain)
            }
        }



        try {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            registerReceiver(screentimeReceiver, filter)
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()
                ?.setValue(timeNow + ": Exception In MainActivity onCreate filter.addAction: " + e)
        }

        //val simpleDateFormat= SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.GERMAN)
        val customDate = Calendar.getInstance()
        customDate.set(Calendar.YEAR, 2022)
        customDate.set(Calendar.MONTH, 0)
        customDate.set(Calendar.DAY_OF_MONTH, 6)
        customDate.set(Calendar.HOUR_OF_DAY, 15)
        customDate.set(Calendar.MINUTE, 2)
        customDate.set(Calendar.SECOND, 0)
        customDate.set(Calendar.MILLISECOND, 0)
        //val date = simpleDateFormat.format(customDate.timeInMillis)
        val currentTime = Calendar.getInstance()

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val userIdStartsWith = sharedPreferences.getString(USER_ID, "")?.startsWith("1") == true

        val firstStart = sharedPreferences.getBoolean("firstStart", true)
        if (firstStart) {
            try {
                println("main in firststart")

                val firstStartTime = Calendar.getInstance()
                val editor = sharedPreferences.edit()
                editor.putLong("firstStartTime", firstStartTime.timeInMillis)
                editor.putBoolean("firstStart", false)
                editor.apply()
                //Toast.makeText(this, "First start: ${simpleDateFormat.format(firstStartTime.timeInMillis)}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate firstStart: " + e)
            }
        }

        if (!userIdStartsWith) {
            try {
                setFragment()
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate !if userIdStartsWith setFragment(): " + e)
            }
        }

        val compareTime = Calendar.getInstance()
        compareTime.timeInMillis = sharedPreferences.getLong("firstStartTime", 0)


        if (currentTime.get(Calendar.MONTH) >= compareTime.get(Calendar.MONTH) &&
            currentTime.get(Calendar.DAY_OF_MONTH) >= (compareTime.get(Calendar.DAY_OF_MONTH) + 7) &&
            currentTime.get(Calendar.HOUR_OF_DAY) >= compareTime.get(Calendar.HOUR_OF_DAY) &&
            currentTime.get(Calendar.MINUTE) >= compareTime.get(Calendar.MINUTE) &&
            currentTime.get(Calendar.SECOND) >= compareTime.get(Calendar.SECOND)
            ||
            currentTime.get(Calendar.MONTH) >= (compareTime.get(Calendar.MONTH) + 1) &&
            currentTime.get(Calendar.DAY_OF_MONTH) >= compareTime.get(Calendar.DAY_OF_MONTH) &&
            currentTime.get(Calendar.HOUR_OF_DAY) >= compareTime.get(Calendar.HOUR_OF_DAY) &&
            currentTime.get(Calendar.MINUTE) >= compareTime.get(Calendar.MINUTE) &&
            currentTime.get(Calendar.SECOND) >= compareTime.get(Calendar.SECOND)
        ) {
            val appSwitch = sharedPreferences.getBoolean("appSwitch", true)
            if (appSwitch) {
                try {
                    //Toast.makeText(this, "App version changed, please restart", Toast.LENGTH_SHORT).show()
                    Snackbar.make(
                        findViewById(R.id.constraint_layout),
                        "App version changed, please restart.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    val editor = sharedPreferences.edit()
                    when (id.startsWith("1")) {
                        true -> {
                            editor.putString(USER_ID, "0$id")
                            editor.apply()
                        }
                        false -> {
                            editor.putString(USER_ID, "1$id")
                            editor.apply()
                        }
                    }
                    editor.putBoolean("appSwitch", false)
                    editor.apply()
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate if appSwitch: " + e)
                }
            }
        }


        /*
        if (userIdStartsWith){
            val textView = findViewById<TextView>(R.id.completed_challenges)
            textView.text = getString(R.string.your_logs)
        }

        if (id.endsWith("1")){
            val textView = findViewById<TextView>(R.id.completed_challenges)
            textView.text = getString(R.string.your_logs)
        }

         */

        
        if (userIdStartsWith) {
            if (logsRef != null) {
                logsRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            challenges.clear()

                            for (postSnapshot in snapshot.children) {
                                val challenge = postSnapshot.getValue(Challenge::class.java)
                                challenges.add(challenge)
                            }

                            if (challenges.isNotEmpty()) {
                                challenges.reverse()
                            }
                            //TODO
                            val challengerino = Challenge("Log Header", Category.MENTAL, false)
                            challenges.add(0, challengerino)

                            //val challengesAmount = findViewById<TextView>(R.id.challenges_amount)
                            //challengesAmount.text = getString(R.string.challenges_amount, challenges.size.toString())

                            recyclerViewAdapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate if userIdStartsWith onDataChange: " + e)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        } else {
            if (completedChallengesRef != null) {
                completedChallengesRef?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            challenges.clear()
                            for (postSnapshot in snapshot.children) {
                                val challenge = postSnapshot.getValue(Challenge::class.java)
                                challenges.add(challenge)
                            }
                            if (challenges.isNotEmpty()) {
                                challenges.reverse()
                            }
                            //TODO
                            val challengerino =
                                Challenge("Challenge Header", Category.MENTAL, false)
                            challenges.add(0, challengerino)

                            //val challengesAmount = findViewById<TextView>(R.id.challenges_amount)
                            //challengesAmount.text = getString(R.string.challenges_amount, challenges.size.toString())

                            recyclerViewAdapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate if completedChallengesRef onDataChange: " + e)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.floatingActionButtonAdd)
        fabAdd.setOnClickListener {
            val addDialog = DialogAddChallenge()
            try {
                addDialog.show(supportFragmentManager, "dialogAddChallenge")
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate fabAdd.setOnClickListener: " + e)
            }
        }

        /**val fabTest = findViewById<FloatingActionButton>(R.id.floatingActionButtonTest)
        fabTest.setOnClickListener {
            println("halloooo"+(5.toInt().div(0.toInt())).toString())
        }**/

        val fabSnooze = findViewById<FloatingActionButton>(R.id.floatingActionButtonSnooze)
        fabSnooze.setOnClickListener{
            val snoozeDialog = DialogSnooze()
            try {
                snoozeDialog.show(supportFragmentManager, "dialogSnooze")
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate fabSnooze.setOnClickListener: " + e)
            }
        }

        /**val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        fab.setOnClickListener {
        if(userIdStartsWith){
        if(checkUsageStatsPermission()){
        val logESDialog = DialogLogExperienceSampling()
        logESDialog.show(supportFragmentManager, "log_ES")
        //val intent = Intent("new_log")
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else {
        val usageStatsDialog = DialogUsageStats()
        usageStatsDialog.isCancelable = false
        usageStatsDialog.show(supportFragmentManager, "usageStatsDialog")
        }
        } else {
        //val currentChallengeFragment = CurrentChallengeFragment()
        if (currentChallengeActive){
        val dialog = DialogSkipChallenge()
        dialog.show(supportFragmentManager, "costumDialog")

        } else {
        if(checkUsageStatsPermission()){
        //val intent = Intent("fab")
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent)


        val beforeChallengeDialog = DialogBeforeChallenge()
        beforeChallengeDialog.show(supportFragmentManager, "beforeChallenge")

        //val TESTDialog = DialogAddChallenge()
        //TESTDialog.show(supportFragmentManager, "addChallenge")

        //supportFragmentManager.beginTransaction().replace(R.id.frame_layout, currentChallengeFragment).commit()
        //currentChallengeActive = true
        //fab.setImageResource(R.drawable.switch_challenge)
        } else {
        val usageStatsDialog = DialogUsageStats()
        usageStatsDialog.isCancelable = false
        usageStatsDialog.show(supportFragmentManager, "usageStatsDialog")
        }
        }
        }}**/


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("completed"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("completed_challenge_fn"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("upload_challenge_from_notification"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("switch"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("send_reason"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("send_cancel_reason"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("intentKey"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("evaluate_challenge"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("cancel_challenge"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("dismiss_cancel_hide_dialog"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("send_hidden_challenge"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("log_from_tile"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("fab"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mBroadcastReceiver, IntentFilter("cat_chosen"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mNotificationReceiver, IntentFilter("challenge_received"))


        /**if (userIdStartsWith) {
            val logPlaceHolderFragment = LogPlaceholderFragment()
            try {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, logPlaceHolderFragment).commit()
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                println(timeNow + ": In MainActivity onCreate if userIdStartsWith logPlaceHolderFragment: " + e)
                FirebaseConfig.debugRef?.push()
                    ?.setValue(timeNow + ": In MainActivity onCreate if userIdStartsWith logPlaceHolderFragment: " + e)
            }
        } else {**/
            val placeholderFragment = PlaceHolderFragment()
            try {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, placeholderFragment).commit()
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate if userIdStartsWith placeHolderFragment: " + e)
            }
        //}

        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
        Sentry.captureMessage(timeNow + ": Test Main onCreate")

        println("main in oncreate3")


    }

    override fun onStart() {
        super.onStart()

        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
        //FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": DEBORAH is in onStart")

        createReminderChannel()
        //FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": DEBORAH createdReminderChannel")

        scheduleReminder()
        //FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": DEBORAH scheduledRminder")

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            unregisterReceiver(screentimeReceiver)}
        catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception unregisterReceiver not possible")
            Sentry.captureMessage(timeNow + ": unregisterReceiver not possible")
        }

        val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + " onDestroy Main")
        FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + " onDestroy Main activeNotificationSize: " +  manager.activeNotifications.toString())
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val receivedIntent = intent // gets the previously created intent
        val firstKeyName = receivedIntent?.getStringExtra("firstKeyName")

        val remoteInputBundle: Bundle? = RemoteInput.getResultsFromIntent(intent)
        if (remoteInputBundle != null) {
            val replyText: CharSequence? = remoteInputBundle?.getCharSequence("key_challenge_reply")
            challenge = (intent?.getSerializableExtra("notification_challenge") as Challenge?)

            val newIntent = Intent("completed_fn")

            val answer: String = replyText.toString()
            newIntent.putExtra("answer", answer)
            newIntent.putExtra("is_from_nf", true)
            newIntent.putExtra("notification_challenge", challenge)

            try {
                let { LocalBroadcastManager.getInstance(this).sendBroadcast(newIntent) }
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception in Main onNewIntent sendBroadcast")

            }
        }


        if (firstKeyName == "send_cancel_fn") {
            challenge = (receivedIntent.getSerializableExtra("randomChallengeIntent") as Challenge)

            val intentCancelInMain = Intent("cancel_challenge_fn")
            intentCancelInMain.putExtra("challengeIntentForService", challenge)
            intentCancelInMain.putExtra("validateStringForService", "validate_service")
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, IntentFilter("cancel_challenge_fn"))

            this.let { LocalBroadcastManager.getInstance(this).sendBroadcast(intentCancelInMain) }
        }
        if (intent?.getStringExtra("firstKeyName") == "send_exchange_fn") {
            challenge =
                (receivedIntent?.getSerializableExtra("exchangedChallengeIntent") as Challenge)

            val intentExchangedInMain = Intent("switch")
            intentExchangedInMain.putExtra("challengeIntentForService", challenge)
            intentExchangedInMain.putExtra("validateStringForService", "validate_service")
            //LocalBroadcastManager.getInstance(this)
            //  .registerReceiver(mBroadcastReceiver, IntentFilter("switch"))

            this.let {
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentExchangedInMain)
            }
        }
        if (intent?.getStringExtra("firstKeyName") == "alarmintent") {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(reminderID)
            manager.cancelAll()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        //val userIdStartsWith = sharedPreferences.getString(USER_ID, "")?.startsWith("1") == true

        try{
                if (completedChallengesRef != null) {
                    completedChallengesRef?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                challenges.clear()
                                for (postSnapshot in snapshot.children) {
                                    val challenge = postSnapshot.getValue(Challenge::class.java)
                                    challenges.add(challenge)
                                }
                                if (challenges.isNotEmpty()) {
                                    challenges.reverse()
                                }
                                //TODO
                                val challengerino =
                                    Challenge("Challenge Header", Category.MENTAL, false)
                                challenges.add(0, challengerino)

                                //val challengesAmount = findViewById<TextView>(R.id.challenges_amount)
                                //challengesAmount.text = getString(R.string.challenges_amount, challenges.size.toString())

                                recyclerViewAdapter.notifyDataSetChanged()
                            } catch (e: Exception) {
                                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onCreate if completedChallengesRef onDataChange: " + e)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            } catch(e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onResume recyclerview: " + e)
        }


        //if (!userIdStartsWith) {
            try {
                setFragment()
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity onResume setFragment: " + e)
            }
        //}
    }

    private fun setFragment() {
        myRootRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentChallengeFragment = CurrentChallengeFragment()
                if (snapshot.hasChild("open") && snapshot.child("open") != null) {
                    println("snapshot: " + snapshot.child("open"))
                    if (!supportFragmentManager.isDestroyed()) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, currentChallengeFragment).commit()
                        currentChallengeActive = true
                    }
                    //val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
                    //fab.setImageResource(R.drawable.switch_challenge)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                val dialog = DialogEvaluateChallenge()
                try {
                    dialog.show(supportFragmentManager, "dialog_evaluate_challenge")
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mMessageReceiver isAtLeast(Lifecycle.State.RESUMED): " + e)
                }
                /*
                    val placeholderFragment = PlaceHolderFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.frame_layout, placeholderFragment).commit()
                    currentChallengeActive = false
                     */
            }
            //myRootRef?.child("open")?.removeValue()

        }
    }

    val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
            when (intent.action) {
                "switch" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val dialog = DialogSkipChallenge()
                        try {
                            dialog.show(supportFragmentManager, "costumDialog")
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->switch: " + e)
                        }
                    }
                }

                "send_reason" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val currentChallengeFragment = CurrentChallengeFragment()
                        try {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, currentChallengeFragment).commit()
                            currentChallengeActive = true
                            val fabIntent = Intent("fab")
                            LocalBroadcastManager.getInstance(context).sendBroadcast(fabIntent)
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->send_reason: " + e)
                        }
                    }
                }
                "send_cancel_reason" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val placeholderFragment = PlaceHolderFragment()
                        try {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, placeholderFragment).commit()
                            currentChallengeActive = false
                            //fab.setImageResource(R.drawable.challenge_flag)
                            myRootRef?.child("open")?.removeValue()
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->send_cancel_reason: " + e)
                        }
                    }
                }

                "intentKey" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val currentChallengeFragment = CurrentChallengeFragment()
                        try {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, currentChallengeFragment).commit()
                            currentChallengeActive = true
                            //fab.setImageResource(R.drawable.switch_challenge)
                            val myIntent = Intent("fab")
                            LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent)
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->intentKey: " + e)
                        }
                    }
                }
                "evaluate_challenge" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val placeholderFragment = PlaceHolderFragment()
                        try {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, placeholderFragment).commit()
                            currentChallengeActive = false
                            //fab.setImageResource(R.drawable.challenge_flag)
                            myRootRef?.child("open")?.removeValue()
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->evaluate_challenge: " + e)
                        }
                    }
                }
                "cancel_challenge" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        if (challenge != null) {
                            val intentCancelChallenge = Intent("send_challenge_fn")
                            intentCancelChallenge.putExtra("challengeFromNotification", challenge)
                            context?.let { it1 ->
                                LocalBroadcastManager.getInstance(it1)
                                    .sendBroadcast(intentCancelChallenge)
                            }
                        }
                        val cancelHideFragmentDialog = DialogCancelHideChallenge()
                        try {
                            println("show cancelhide")
                            cancelHideFragmentDialog.show(
                                supportFragmentManager,
                                "dialog_cancel_hide_challenge"
                            )
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->cancel_challenge: " + e)
                        }
                    }
                }
                "cancel_challenge_fn" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        if (challenge != null) {
                            println("show challenge ungleich null")
                            val intentCancelChallenge = Intent("send_challenge_fn")
                            intentCancelChallenge.putExtra("challengeFromNotification", challenge)
                            context?.let { it1 ->
                                LocalBroadcastManager.getInstance(it1)
                                    .sendBroadcast(intentCancelChallenge)
                            }
                        }
                        val cancelHideFragmentDialog = DialogCancelHideChallenge()
                        try {
                            println("show cancelfn")

                            cancelHideFragmentDialog.show(
                                supportFragmentManager,
                                "dialog_cancel_hide_challenge"
                            )
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->cancel_challenge_fn: " + e)
                        }
                    }
                }
                "dismiss_cancel_hide_dialog" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val cancelFragmentDialog = DialogCancelChallenge()
                        try {
                            cancelFragmentDialog.show(
                                supportFragmentManager,
                                "dialog_cancel_challenge"
                            )
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->dismiss_cancel_hide_dialog: " + e)
                        }
                    }
                }
                "fab" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {

                        /**val currentChallengeFragment = CurrentChallengeFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, currentChallengeFragment).commit()
                        currentChallengeActive = true
                        fab.setImageResource(R.drawable.switch_challenge)**/

                        val choseCatDialog = DialogChoseCategory()
                        try {
                            choseCatDialog.show(supportFragmentManager, "dialogChoseCategory")
                            currentChallengeActive = true
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->fab: " + e)
                        }
                        //fab.setImageResource(R.drawable.switch_challenge)
                    }
                }
                "cat_chosen" -> {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        val currentChallengeFragment = CurrentChallengeFragment()
                        try {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.frame_layout, currentChallengeFragment).commit()
                            currentChallengeActive = true
                            //fab.setImageResource(R.drawable.switch_challenge)
                        } catch (e: Exception) {
                            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity mBroadcastReceiver->cat_chosen: " + e)
                        }
                    }
                }
            }
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        var appOpsManager: AppOpsManager? = null
        var mode = 0
        appOpsManager = getSystemService(Context.APP_OPS_SERVICE)!! as AppOpsManager
        mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationInfo.uid,
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    val mNotificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
            when (intent.action) {
                "challenge_received" -> {
                    val firstKeyName = intent.getStringExtra("firstKeyName")

                    if (firstKeyName == "send_cancel_fn") {
                        challenge = (intent.getSerializableExtra("randomChallengeIntent") as Challenge)

                        val intentCancelInMain = Intent("cancel_challenge_fn")
                        intentCancelInMain.putExtra("challengeIntentForService", challenge)
                        intentCancelInMain.putExtra("validateStringForService", "validate_service")
                        LocalBroadcastManager.getInstance(context)
                            .registerReceiver(mBroadcastReceiver, IntentFilter("cancel_challenge_fn"))

                        this.let { LocalBroadcastManager.getInstance(context).sendBroadcast(intentCancelInMain) }
                    }
                    if (intent.getStringExtra("firstKeyName") == "send_exchange_fn") {
                        challenge =
                            (intent.getSerializableExtra("exchangedChallengeIntent") as Challenge)
                        val intentExchangedInMain = Intent("switch")
                        intentExchangedInMain.putExtra("challengeIntentForService", challenge)
                        intentExchangedInMain.putExtra("validateStringForService", "validate_service")
                        LocalBroadcastManager.getInstance(context)
                            .registerReceiver(mBroadcastReceiver, IntentFilter("switch"))

                        this.let {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intentExchangedInMain)
                        }
                }

        }}}}

    public fun giveChallenge() {
        try {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            /**val userIdStartsWith = sharedPreferences.getString(USER_ID, "")?.startsWith("1") == true
            if (userIdStartsWith) {
                if (checkUsageStatsPermission()) {
                    val logESDialog = DialogLogExperienceSampling()
                    logESDialog.show(supportFragmentManager, "log_ES")
                    //val intent = Intent("new_log")
                    //LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                } else {
                    val usageStatsDialog = DialogUsageStats()
                    usageStatsDialog.isCancelable = false
                    usageStatsDialog.show(supportFragmentManager, "usageStatsDialog")
                }
            } else {**/
                //val currentChallengeFragment = CurrentChallengeFragment()
                if (checkUsageStatsPermission()) {

                    val giveChallengeIntent = Intent("cat_chosen")

                    giveChallengeIntent.putExtra("relaxingChecked", false)
                    giveChallengeIntent.putExtra("mentalChecked", false)
                    giveChallengeIntent.putExtra("physicalChecked", false)
                    giveChallengeIntent.putExtra("socialChecked", false)
                    giveChallengeIntent.putExtra("organizingChecked", false)
                    giveChallengeIntent.putExtra("miscChecked", false)
                    giveChallengeIntent.putExtra("randomChecked", true)

                    this.let { it1 ->
                        LocalBroadcastManager.getInstance(it1).sendBroadcast(giveChallengeIntent)
                    }

                } else {
                    val usageStatsDialog = DialogUsageStats()
                    usageStatsDialog.isCancelable = false
                    usageStatsDialog.show(supportFragmentManager, "usageStatsDialog")
                }
            //}
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity giveChallenge() method: " + e)
        }
    }

    public fun switchChallenge() {
        val dialog = DialogSkipChallenge()
        try {
            dialog.show(supportFragmentManager, "costumDialog")
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In MainActivity switchChallenge() method: " + e)
        }
    }

    public fun snooze(minutes: Long) {
        if(minutes != null) {
            val timeNow = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
            FirebaseConfig.snoozeRef?.push()?.setValue(timeNow + ": snoozed for " + minutes.toString() + "minutes")

            try {
                val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val notificationIntent = Intent(this, ScreenNotificationReceiver::class.java)
                stopTimer(this, alarmManager, notificationIntent)

                unregisterReceiver(screentimeReceiver)
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main snooze() method 1: " + e)
            }

            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_USER_PRESENT)

            try {
                Timer("SnoozeTimer", false).schedule(minutes * 60 * 1000) {
                    registerReceiver(screentimeReceiver, filter)
                }
            } catch (e: Exception) {
                val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
                FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main snooze() method 2: " + e)
            }
        }
    }

    /**public fun getSnoozeBtn(): FloatingActionButton {
        val fabSnooze = findViewById<FloatingActionButton>(R.id.floatingActionButtonSnooze)
        return fabSnooze
    }**/

    private fun stopTimer(context:Context, alarmManager: AlarmManager, notificationIntent:Intent) {
        try {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
//                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main stopTimer() method: " + e)
        }
    }

    private fun scheduleReminder() {
        //FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN scheduleReminder START")
        val intent = Intent(applicationContext, DailyReceiver::class.java)
        val title = "Click me!"
        val message = "Please make sure, that your challenges are displayed. Otherwise restart the app!"
        intent.putExtra(reminderTitleExtra, title)
        intent.putExtra(reminderMessageExtra, message)

        val reminderPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            reminderID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
                    //PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getReminderTime()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                reminderPendingIntent
            )

            /**alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            getReminderTime(),
            AlarmManager.INTERVAL_DAY,
            reminderPendingIntent
            )**/
            //FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN scheduleReminder ENDE")


        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main scheduleReminder() method: " + e)
        }
    }

    private fun getReminderTime(): Long {
        //FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN getReminderTime START")
        /**val minute = LocalDateTime.now().toString().substring(14, 16).toInt() +1
        val hour = LocalDateTime.now().toString().substring(11, 13).toInt()
        val day = 13
        val month = 11
        val year = 2022

        val timestamp = LocalDateTime.now().toString() //2017-08-02T11:25:44.973
        val minute2 = timestamp.substring(14, 16).toInt()+1
        val hour2 = timestamp.substring(11, 13).toInt()
        val day2 = timestamp.substring(8, 10).toInt()
        val month2 = timestamp.substring(5, 7).toInt()-1
        val year2 = timestamp.substring(0, 4).toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year2, month2, day2, hour2, minute2)

        println("hour-"+hour2.toString()+ "minute-"+minute2.toString()+ "day-"+day2.toString()+ "month-"+month2.toString()+ "year-"+year2.toString() )

        val reminderCalendar = Calendar.getInstance()
        reminderCalendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis**/
        val calendar: Calendar = Calendar.getInstance()

        try {
            val cur = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, 5)
            if (calendar.timeInMillis <= cur) {
                println("in if: calendar.timeinmillis: " + calendar.timeInMillis.toString() + " + current time in millis: " + cur.toString())
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            } else {
                println("now")
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    LocalDateTime.now().toString().substring(8, 10).toInt()
                )

            }
           // FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN getReminderTime ENDE")

        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main getReminderTime() method: " + e)
        }

        return calendar.timeInMillis
    }

    private fun createReminderChannel() {
        try {
            //FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN createReminderChannel START")
            val name = "Reminder Channe"
            val desc = "A Desc of the Reminder Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(reminderChannelID, name, importance)
            channel.description = desc
            val reminderManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            reminderManager.createNotificationChannel(channel)
            //FirebaseConfig.debugRef?.push()?.setValue("DEBORAH is IN createReminderChannel ENDE")
        } catch (e: Exception) {
            val timeNow = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())
            FirebaseConfig.debugRef?.push()?.setValue(timeNow + ": Exception In Main createReminderChannel() method: " + e)
        }
    }

}