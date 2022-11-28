package com.mimuc.rww

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.mimuc.rww.FirebaseConfig.Companion.initFirebasePaths
import io.sentry.Sentry
import io.sentry.protocol.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var mAuth: FirebaseAuth
        lateinit var id: String
        const val SHARED_PREFS = "sharedPrefs"
        const val USER_ID = "user_id"
        const val USER_ID_ON_START = "user_id_on_start"
    }

    override fun onStart() {
        //println("login onstart")
        val user = mAuth.currentUser

        if (user != null) {
            /**val sentryUser = User().apply {
                email = mAuth.currentUser?.email
            }
            Sentry.setUser(sentryUser)**/

            val sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            //id = sharedPrefs.getString(USER_ID_ON_START, "${(0..6).random()}").toString()
            id = sharedPrefs.getString(USER_ID_ON_START, null).toString()
            initFirebasePaths(id)
            //FirebaseConfig.debugRef?.push()?.setValue("Hallo Firebase2??")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
            println("my user id: "+id.toString())
            FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Login onStart -> startActivity called")

            finish()

        }
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //println("login oncreate")
        //FirebaseConfig.debugRef?.push()?.setValue("Hallo Firebase??")


        //createDefaultChallenges()
        setContentView(R.layout.activity_log_in)
        mAuth = FirebaseAuth.getInstance()
        println("mauth: "+mAuth.toString())

        val signInButton = findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener {

            id = findViewById<EditText>(R.id.editTextNumber).text.toString()

            if (id.length < 4) {
                Toast.makeText(this, "Please use your StudyID (Aa-Zz, 0-9)", Toast.LENGTH_SHORT).show()
            }
            else if ("^[A-Za-z0-9_-]+$".toRegex().matches(id)) {
                createUserAccount(id)

                val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                val userIdStartsWith = sharedPreferences.getString(USER_ID, "")?.startsWith("1") == true
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
                } else {
                    //val currentChallengeFragment = CurrentChallengeFragment()
                    if (checkUsageStatsPermission()) {
                        //FirebaseConfig.debugRef?.push()?.setValue("Hallo Firebase1??")

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                        val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                        FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Login onCreate -> startActivity called")

                    } else {
                        //println("login in usagestats")

                        val usageStatsDialog = DialogUsageStats()
                        usageStatsDialog.isCancelable = false
                        usageStatsDialog.show(supportFragmentManager, "usageStatsDialog")
                    }
                }
            }
            else if(id == "") {
                Toast.makeText(this, "Enter StudyID", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Please do not use space or other special characters", Toast.LENGTH_SHORT).show()
            }






        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
        FirebaseConfig.debugDestroyRef?.push()?.setValue(timeNow + " onDestroy Login")
    }

    private fun createUserAccount(id: String) {
        //println("login in createuseraccount")

        createDefaultChallenges()

        //if (id.isNotEmpty()) {
            val email = "$id@realworldwind.com"
            val password = "userstudy"

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //userId = mAuth?.currentUser?.uid.toString()
                    initFirebasePaths(id)

                    val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(USER_ID, id)
                    editor.putString(USER_ID_ON_START, id)
                    editor.apply()
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun createDefaultChallenges() {
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        if (!sharedPrefs.contains("jsonChallenge")) {

            val challengeList: ArrayList<Challenge> = ChallengeList().getMyList()

            val json = Gson().toJson(challengeList)
            val editor = sharedPrefs?.edit()
            editor?.putString("jsonChallenge", json)
            editor?.commit() //or apply()?
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        var appOpsManager: AppOpsManager? = null
        var mode = 0
        appOpsManager = getSystemService(Context.APP_OPS_SERVICE)!! as AppOpsManager
        mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}