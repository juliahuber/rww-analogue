package com.mimuc.rww

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseConfig {

    companion object {
        var myRootRef: DatabaseReference ? = null
        var addedChallengesRef: DatabaseReference? = null
        var chosenCategoriesRef: DatabaseReference? = null
        var completedChallengesRef: DatabaseReference? = null
        var openChallengesRef: DatabaseReference? = null
        var swappedChallengesRef: DatabaseReference? = null
        var cancelledChallengesRef: DatabaseReference? = null
        var deletedChallengesRef: DatabaseReference? = null
        var snoozeRef: DatabaseReference? = null
        var logsRef: DatabaseReference? = null
        var debugRef: DatabaseReference? = null
        var debugUnlocksRef: DatabaseReference? = null
        var debugDestroyRef: DatabaseReference? = null

        fun initFirebasePaths(userId: String?) {
            val database = FirebaseDatabase.getInstance("https://rww-analogue-default-rtdb.europe-west1.firebasedatabase.app/")
            println("database: "+database.toString())
            println("database.getreference: "+database.getReference().toString())
            println("database app: "+database.app.toString())
            if (userId != null){
                myRootRef = database.getReference("users").child(userId)
                addedChallengesRef = database.getReference("users").child(userId).child("added")
                chosenCategoriesRef = database.getReference("users").child(userId).child("chosenCat")
                completedChallengesRef = database.getReference("users").child(userId).child("completed")
                //completedChallengesRef = database.getReference("users").child(userId).child("completed_fn")
                openChallengesRef = database.getReference("users").child(userId).child("open")
                swappedChallengesRef = database.getReference("users").child(userId).child("exchanged")
                cancelledChallengesRef = database.getReference("users").child(userId).child("cancelled")
                deletedChallengesRef = database.getReference("users").child(userId).child("deleted")
                snoozeRef = database.getReference("users").child(userId).child("snooze")
                logsRef = database.getReference("users").child(userId).child("logs")
                debugRef = database.getReference("users").child(userId).child("debug")
                debugUnlocksRef = database.getReference("users").child(userId).child("debug-unlocks")
                debugDestroyRef = database.getReference("users").child(userId).child("debug-onDestroy")
            }
        }
    }
}

