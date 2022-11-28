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
import com.mimuc.rww.databinding.DialogSkipChallengeBinding

class DialogSkipChallenge : DialogFragment() {

    private var binding: DialogSkipChallengeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSkipChallengeBinding.inflate(inflater, container, false)
        binding?.apply {
            sendReasonButton.setOnClickListener {
                var reason = ""
                val radioId = radioGroup.checkedRadioButtonId
                val radioButton = view?.findViewById<RadioButton>(radioId)
                if(radioButton == null && reasonEdittext.text.isEmpty()){
                    Toast.makeText(context, "Please give a reason", Toast.LENGTH_SHORT).show()
                } else {
                    if (radioButton != null){
                        reason = radioButton.text.toString() + ". " + explainCircumstances.text.toString() + " " + reasonEdittext.text.toString()
                    } else {
                        reason = reasonEdittext.text.toString()
                    }
                    val intent = Intent("send_reason")
                    intent.putExtra("reason", reason)
                    context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                    dismiss()
                }
            }
        }

        return binding?.root
    }
}