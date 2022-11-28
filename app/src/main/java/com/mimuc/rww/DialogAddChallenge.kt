package com.mimuc.rww
import Category
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mimuc.rww.databinding.DialogAddChallengeBinding

class DialogAddChallenge: DialogFragment() {
    private var binding: DialogAddChallengeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = DialogAddChallengeBinding.inflate(inflater, container, false)
        binding?.apply {

            var category: Category = Category.RELAXING
            radiogroupCategories.setOnCheckedChangeListener { radiogroupCategories, checkedId ->
                category = when (checkedId) {
                    R.id.radio_cat_relaxing -> Category.RELAXING
                    R.id.radio_cat_mental -> Category.MENTAL
                    R.id.radio_cat_physical -> Category.PHYSICAL
                    R.id.radio_cat_social -> Category.SOCIAL
                    R.id.radio_cat_organizing -> Category.ORGANIZING
                    R.id.radio_cat_misc -> Category.MISC
                    else -> Category.RELAXING //default
                }
            }

            addChallengeButton.setOnClickListener {
                val gson = Gson()
                val sharedPrefs = activity?.getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE)
                val editor = sharedPrefs?.edit()

                val currentChallengesFromPref = getCurrentChallengeListFromPref(gson, sharedPrefs)

                val input:String? = edittextAddChallenge.text.toString()
                if (input?.trim()?.length!! > 0) {
                    val challenge = Challenge(input, category, true)

                    addToChallengesList(gson, editor, challenge, currentChallengesFromPref)
                    Toast.makeText(context, "Added " + input, Toast.LENGTH_SHORT).show()
                    edittextAddChallenge.setText("")

                    FirebaseConfig.addedChallengesRef?.push()?.setValue(challenge)

                    dismiss()

                } else {
                    Toast.makeText(context, "Please add a challenge", Toast.LENGTH_SHORT).show()
                }
            }
    }
        return binding?.root
    }

    private fun getCurrentChallengeListFromPref(gson:Gson, sharedPrefs:SharedPreferences?): ArrayList<Challenge>  {
        val savedList = sharedPrefs?.getString("jsonChallenge", "?")
        val myType = object : TypeToken<ArrayList<Challenge>>() {}.type
        val list = gson.fromJson<ArrayList<Challenge>>(savedList, myType)
        println("oldlist: "+list)
        return list
    }

    private fun addToChallengesList(gson:Gson, editor:SharedPreferences.Editor?, challenge:Challenge, challengeList:ArrayList<Challenge>) {
        challengeList.add(challenge)
        val json:String = gson.toJson(challengeList)
        editor?.putString("jsonChallenge", json)
        editor?.commit()
    }
}