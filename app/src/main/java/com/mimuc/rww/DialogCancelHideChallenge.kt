package com.mimuc.rww
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mimuc.rww.databinding.DialogCancelHideChallengeBinding

class DialogCancelHideChallenge: DialogFragment() {
    private var binding: DialogCancelHideChallengeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = DialogCancelHideChallengeBinding.inflate(inflater, container, false)
        binding?.apply {

            var gotHidden = false

            quitChallengeButton.setOnClickListener {
                val alwaysHideChecked = checkAlwaysHide.isChecked
                if (alwaysHideChecked) {
                    gotHidden = true
                }

                val intent = Intent("send_hidden_challenge")
                intent.putExtra("got_hidden", gotHidden)
                context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }

                val nextDialogIntent = Intent("dismiss_cancel_hide_dialog")
                context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(nextDialogIntent) }

                dismiss()
            }
    }
        return binding?.root
    }
}