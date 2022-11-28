package com.mimuc.rww

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mimuc.rww.databinding.DialogUsageStatsBinding
import java.text.SimpleDateFormat
import java.util.*

class DialogUsageStats : DialogFragment() {

    private var binding: DialogUsageStatsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogUsageStatsBinding.inflate(inflater, container, false)
        binding?.apply {
            gotItButton.setOnClickListener {
                //val intent = Intent("fab")
                //context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                val timeNow = SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())
                FirebaseConfig.debugUnlocksRef?.push()?.setValue(timeNow + ": Dialog Usage Stats")
                dismiss()
            }
        }

        return binding?.root
    }
}