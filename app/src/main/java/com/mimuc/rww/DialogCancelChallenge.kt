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
import com.mimuc.rww.databinding.DialogCancelChallengeBinding
import kotlinx.android.synthetic.main.dialog_cancel_challenge.*

class DialogCancelChallenge : DialogFragment(){

    private var binding: DialogCancelChallengeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = DialogCancelChallengeBinding.inflate(inflater, container, false)
        binding?.apply {

            sendReasonButton.setOnClickListener {
                val feltOverload = !check_feel_overload.isChecked
                var reason: String
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
                    val cancelReasonIntent = Intent("send_cancel_reason")
                    cancelReasonIntent.putExtra("cancel_reason", reason)
                    cancelReasonIntent.putExtra("check_feel_overload", feltOverload)
                    context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(cancelReasonIntent) }
                    dismiss()

                    Toast.makeText(context, "Challenge cancelled", Toast.LENGTH_SHORT).show()

                }
            }
        }
        return binding?.root
    }
}