package com.mimuc.rww

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mimuc.rww.R
import com.mimuc.rww.databinding.DialogEvaluateChallengeBinding
import kotlinx.android.synthetic.main.dialog_evaluate_challenge.*

class DialogEvaluateChallenge : DialogFragment() {
    private var binding: DialogEvaluateChallengeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogEvaluateChallengeBinding.inflate(inflater, container, false)
        binding?.apply {
            sendReasonButton.setOnClickListener {
                /**
                var balanced = ""
                val balancedId = balanceId.checkedRadioButtonId
                val balanceButton = view?.findViewById<RadioButton>(balancedId)

                var sadHappy = ""
                val sadHappyId = sadHappyId.checkedRadioButtonId
                val sadHappyButton = view?.findViewById<RadioButton>(sadHappyId)

                var boredExcited = ""
                val boredExcitedId = boredExcitedId.checkedRadioButtonId
                val boredExcitedButton = view?.findViewById<RadioButton>(boredExcitedId)

                var annoyedPleased = ""
                val annoyedPleasedId = annoyedPleasedId.checkedRadioButtonId
                val annoyedPleasedButton = view?.findViewById<RadioButton>(annoyedPleasedId)
                **/

                //var agreeHelped = ""
                //val agreeHelpedId = agreeHelpedId.checkedRadioButtonId
                //val agreeHelpedButton = view?.findViewById<RadioButton>(agreeHelpedId)

                var agreeAwareness = ""
                val agreeAwarenessId = agreeAwarenessId.checkedRadioButtonId
                val agreeAwarenessButton = view?.findViewById<RadioButton>(agreeAwarenessId)

                var agreeEnjoyed = ""
                val agreeEnjoyedId = agreeEnjoyedId.checkedRadioButtonId
                val agreeEnjoyedButton = view?.findViewById<RadioButton>(agreeEnjoyedId)

                var agreeBored = ""
                val agreeBoredId = agreeBoredId.checkedRadioButtonId
                val agreeBoredButton = view?.findViewById<RadioButton>(agreeBoredId)

                var agreeHappy = ""
                val agreeHappyId = agreeHappyId.checkedRadioButtonId
                val agreeHappyButton = view?.findViewById<RadioButton>(agreeHappyId)

                var agreeAnnoyed = ""
                val agreeAnnoyedId = agreeAnnoyedId.checkedRadioButtonId
                val agreeAnnoyedButton = view?.findViewById<RadioButton>(agreeAnnoyedId)

                //var agreeSame = ""
                //val agreeSameId = agreeSameId.checkedRadioButtonId
                //val agreeSameButton = view?.findViewById<RadioButton>(agreeSameId)

                var agreeWellbeing = ""
                val agreeWellbeingId = agreeWellbeingId.checkedRadioButtonId
                val agreeWellbeingButton = view?.findViewById<RadioButton>(agreeWellbeingId)

                var agreeBalance = ""
                val agreeBalanceId = agreeBalanceId.checkedRadioButtonId
                val agreeBalanceButton = view?.findViewById<RadioButton>(agreeBalanceId)

                var whichContext = ""
                val contextId = contextId.checkedRadioButtonId
                val contextButton = view?.findViewById<RadioButton>(contextId)


                if (
                    //agreeHelpedButton == null ||
                    agreeAwarenessButton == null ||
                    agreeEnjoyedButton == null ||
                    agreeBoredButton == null ||
                    agreeHappyButton == null ||
                    agreeAnnoyedButton == null ||
                    agreeWellbeingButton == null ||
                    agreeBalanceButton == null ||
                    //agreeSameButton == null ||
                    contextButton == null
                ){
                    Toast.makeText(context, "Please fill out the questions", Toast.LENGTH_SHORT).show()
                } else {
                    /**when (balanceButton.id){
                        R.id.very_balanced -> balanced = "very balanced"
                        R.id.balanced -> balanced = "balanced"
                        R.id.unbalanced -> balanced = "unbalanced"
                        R.id.very_unbalanced -> balanced = "very unbalanced"
                        R.id.dont_know -> balanced = "I don't know"
                    }
                    when (sadHappyButton.id){
                        R.id.sad -> sadHappy = "sad"
                        R.id.slightly_sad -> sadHappy = "slightly sad"
                        R.id.neutral -> sadHappy = "neutral"
                        R.id.slightly_happy -> sadHappy = "slightly happy"
                        R.id.happy -> sadHappy = "happy"
                    }
                    when (boredExcitedButton.id){
                        R.id.bored -> boredExcited = "bored"
                        R.id.slightly_bored -> boredExcited = "slightly bored"
                        R.id.neutral1 -> boredExcited = "neutral"
                        R.id.slightly_excited -> boredExcited = "slightly excited"
                        R.id.excited -> boredExcited = "excited"
                    }
                    when (annoyedPleasedButton.id){
                        R.id.annoyed -> annoyedPleased = "annoyed"
                        R.id.slightly_annoyed -> annoyedPleased = "slightly annoyed"
                        R.id.neutral2 -> annoyedPleased = "neutral"
                        R.id.slightly_pleased -> annoyedPleased = "slightly pleased"
                        R.id.pleased -> annoyedPleased = "pleased"
                    }

                    when (agreeHelpedButton.id){
                        R.id.strongly_disagree_helped -> agreeHelped = "strongly disagree"
                        R.id.disagree_helped -> agreeHelped = "disagree"
                        R.id.agree_helped -> agreeHelped = "agree"
                        R.id.strongly_agree_helped -> agreeHelped = "strongly agree"
                        R.id.dont_know_helped -> agreeHelped = "I don't know"
                    }**/
                    when (agreeAwarenessButton.id){
                        R.id.strongly_disagree_awareness -> agreeAwareness = "strongly disagree"
                        R.id.disagree_awareness -> agreeAwareness = "disagree"
                        R.id.agree_awareness -> agreeAwareness = "agree"
                        R.id.strongly_agree_awareness -> agreeAwareness = "strongly agree"
                        R.id.dont_know_awareness -> agreeAwareness = "I don't know"
                    }
                    when (agreeEnjoyedButton.id){
                        R.id.strongly_disagree_enjoyed -> agreeEnjoyed = "strongly disagree"
                        R.id.disagree_enjoyed -> agreeEnjoyed = "disagree"
                        R.id.agree_enjoyed -> agreeEnjoyed = "agree"
                        R.id.strongly_agree_enjoyed -> agreeEnjoyed = "strongly agree"
                        R.id.dont_know_enjoyed -> agreeEnjoyed = "I don't know"
                    }
                    when (agreeBoredButton.id){
                        R.id.strongly_disagree_bored -> agreeBored = "strongly disagree"
                        R.id.disagree_bored -> agreeBored = "disagree"
                        R.id.agree_bored -> agreeBored = "agree"
                        R.id.strongly_agree_bored -> agreeBored = "strongly agree"
                        R.id.dont_know_bored -> agreeBored = "I don't know"
                    }
                    when (agreeHappyButton.id){
                        R.id.strongly_disagree_happy -> agreeHappy = "strongly disagree"
                        R.id.disagree_happy -> agreeHappy = "disagree"
                        R.id.agree_happy -> agreeHappy = "agree"
                        R.id.strongly_agree_happy -> agreeHappy = "strongly agree"
                        R.id.dont_know_happy -> agreeHappy = "I don't know"
                    }
                    when (agreeAnnoyedButton.id){
                        R.id.strongly_disagree_annoyed -> agreeAnnoyed = "strongly disagree"
                        R.id.disagree_annoyed -> agreeAnnoyed = "disagree"
                        R.id.agree_annoyed -> agreeAnnoyed = "agree"
                        R.id.strongly_agree_annoyed -> agreeAnnoyed = "strongly agree"
                        R.id.dont_know_annoyed -> agreeAnnoyed = "I don't know"
                    }
                    /**when (agreeSameButton.id){
                        R.id.strongly_disagree_same -> agreeSame = "strongly disagree"
                        R.id.disagree_same -> agreeSame = "disagree"
                        R.id.agree_same -> agreeSame = "agree"
                        R.id.strongly_agree_same -> agreeSame = "strongly agree"
                        R.id.dont_know_same -> agreeSame = "I don't know"
                    }**/
                    when (agreeWellbeingButton.id){
                        R.id.strongly_disagree_wellbeing -> agreeWellbeing = "strongly disagree"
                        R.id.disagree_wellbeing -> agreeWellbeing = "disagree"
                        R.id.agree_wellbeing -> agreeWellbeing = "agree"
                        R.id.strongly_agree_wellbeing -> agreeWellbeing = "strongly agree"
                        R.id.dont_know_wellbeing -> agreeWellbeing = "I don't know"
                    }
                    when (agreeBalanceButton.id){
                        R.id.strongly_disagree_balance -> agreeBalance = "strongly disagree"
                        R.id.disagree_balance -> agreeBalance = "disagree"
                        R.id.agree_balance -> agreeBalance = "agree"
                        R.id.strongly_agree_balance -> agreeBalance = "strongly agree"
                        R.id.dont_know_balance -> agreeBalance = "I don't know"
                    }
                    when (contextButton.id){
                        R.id.radio_home -> whichContext = "Home"
                        R.id.radio_outside -> whichContext = "Outside"
                        R.id.radio_work -> whichContext = "Work"
                        R.id.radio_other -> whichContext = "Other: " + binding?.radioEdit?.text.toString()
                    }


                    val intent = Intent("evaluate_challenge")
                    /**
                    intent.putExtra("balanced", balanced)
                    intent.putExtra("sadHappy", sadHappy)
                    intent.putExtra("boredExcited", boredExcited)
                    intent.putExtra("annoyedPleased", annoyedPleased)
                    **/

                    //intent.putExtra("agreeHelped", agreeHelped)
                    intent.putExtra("agreeAwareness", agreeAwareness)
                    intent.putExtra("agreeEnjoyed", agreeEnjoyed)
                    intent.putExtra("agreeBored", agreeBored)
                    intent.putExtra("agreeHappy", agreeHappy)
                    intent.putExtra("agreeAnnoyed", agreeAnnoyed)
                    //intent.putExtra("agreeSame", agreeSame)
                    intent.putExtra("agreeWellbeing", agreeWellbeing)
                    intent.putExtra("agreeBalance", agreeBalance)

                    intent.putExtra("whichContext", whichContext)

                    context?.let { it1 -> LocalBroadcastManager.getInstance(it1).sendBroadcast(intent) }
                    dismiss()
                    Toast.makeText(context, "Challenge completed", Toast.LENGTH_SHORT).show()
                }
            }

            val textwatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    contextId.check(R.id.radio_other)
                }
            }
            radioEdit.addTextChangedListener(textwatcher)
        }
        return binding?.root
    }


}