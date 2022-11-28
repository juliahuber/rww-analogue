package com.mimuc.rww

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mimuc.rww.FirebaseConfig.Companion.openChallengesRef
import com.mimuc.rww.databinding.FragmentCurrentChallengeBinding
import kotlinx.android.synthetic.main.fragment_current_challenge.*
import java.text.SimpleDateFormat
import java.util.*

class CurrentChallengeFragment : Fragment() {

    var challenge: Challenge? = null
    var challenges = ArrayList<Challenge?>()
    private val UNKNOWN: String = "???"

    private var binding: FragmentCurrentChallengeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentChallengeBinding.inflate(inflater, container, false)

        binding?.apply {

            switchChallengeBtn.setOnClickListener {
                try {
                    (activity as MainActivity?)!!.switchChallenge()
                } catch (e: Exception) {
                    val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                    FirebaseConfig.debugRef?.push()?.setValue(timeNow+ ": Exception In CurrentChallengeFragment switchChallengeBtn.setOnClickListener: " + e)
                }
            }
        }
        //Warning:(40, 35) To get local formatting use `getDateInstance()`, `getDateTimeInstance()`, or `getTimeInstance()`, or use `new SimpleDateFormat(String template, Locale locale)` with for example `Locale.US` for ASCII dates.


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openChallengesRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                challenges.clear()
                for (postSnapshot in snapshot.children) {
                    challenge = postSnapshot.getValue(Challenge::class.java)
                    if(category_icon != null) {
                        challenge?.cat?.let { category_icon.setImageResource(it.iconLarge) }
                    } else {
                        println("is null?")
                    }

                    if(category_img != null) {
                        challenge?.cat?.let { category_img.setImageResource(it.img) }
                    } else {
                        println("is null?")
                    }

                    challenges.add(challenge)

                    val lastPos = challenges.size - 1
                    binding?.apply {
                        currentChallengeTitle.text = challenges[lastPos]?.title ?: UNKNOWN
                        date.text = challenges[lastPos]?.date ?: UNKNOWN
                        time.text = challenges[lastPos]?.time ?: UNKNOWN
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding?.fabSendAnswer?.setOnClickListener {
            if (binding?.answerEditText?.text?.isEmpty() == true){
                Toast.makeText(context, "Please give an answer", Toast.LENGTH_SHORT).show()
            } else {
                val answer: String = binding?.answerEditText?.text.toString()
                val intent2 = Intent("completed")
                intent2.putExtra("answer", answer)
                context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent2) }
            }
        }

        binding?.cancelChallenge?.setOnClickListener{
            val intent = Intent("cancel_challenge")
            intent.putExtra("challenge", challenge)
            context?.let {it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent)}
        }
    }
}