package com.mimuc.rww

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mimuc.rww.R
import com.mimuc.rww.databinding.DialogBeforeChallengeEsBinding

class DialogBeforeChallenge : DialogFragment() {
    private var binding: DialogBeforeChallengeEsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogBeforeChallengeEsBinding.inflate(inflater, container, false)
        binding?.apply {
            sendReasonButton.setOnClickListener {
                var balanced = ""
                val balancedId = balanceId.checkedRadioButtonId
                val balanceButton = view?.findViewById<RadioButton>(balancedId)

                var howRecognized = ""

                if (balanceButton == null || recognizeOverload.text.isEmpty()){
                    Toast.makeText(context, "Please give an answer", Toast.LENGTH_SHORT).show()
                } else {
                    when (balanceButton.id){
                        R.id.very_balanced -> balanced = "very balanced"
                        R.id.balanced -> balanced = "balanced"
                        R.id.unbalanced -> balanced = "unbalanced"
                        R.id.very_unbalanced -> balanced = "very unbalanced"
                        R.id.dont_know -> balanced = "I don't know"
                    }

                    howRecognized = recognizeOverload.text.toString()

                    //val intent = Intent("fab")
                    val intent = Intent("fab")
                    intent.putExtra("balanced", balanced)
                    intent.putExtra("howRecognized", howRecognized)

                    context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                    dismiss()
                }
            }
        }

        return binding?.root
    }
}