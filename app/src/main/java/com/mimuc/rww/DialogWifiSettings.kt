package com.mimuc.rww

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mimuc.rww.databinding.DialogWifiSettingsBinding

class DialogWifiSettings : DialogFragment(){

    private var binding: DialogWifiSettingsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        binding = DialogWifiSettingsBinding.inflate(inflater, container, false)
        binding?.apply {

            sendReasonButton.setOnClickListener {
                val radioId = wifiGroup.checkedRadioButtonId
                val radioButton = view?.findViewById<RadioButton>(radioId)

                /**val cancelReasonIntent = Intent("send_cancel_reason")
                cancelReasonIntent.putExtra("cancel_reason", reason)
                context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(cancelReasonIntent) }
                 **/
                dismiss()

            }
        }
        return binding?.root
    }
}