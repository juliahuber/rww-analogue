package com.mimuc.rww

import Category
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mimuc.rww.databinding.DialogChoseCategoryBinding
import kotlin.collections.ArrayList

class DialogChoseCategory: DialogFragment() {

    val gson: Gson = Gson()

        private var binding: DialogChoseCategoryBinding? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

        ): View? {
            binding = DialogChoseCategoryBinding.inflate(inflater, container, false)
            binding?.apply {

                val sharedPrefs = activity?.getSharedPreferences(LoginActivity.SHARED_PREFS, Context.MODE_PRIVATE)
                println("LOGIN?sharedprefs")
                var category: Category = Category.MENTAL
                println("category")


                choseCategoriesButton.setOnClickListener {

                    val relaxingChecked = checkRelaxing.isChecked
                    val mentalChecked = checkMental.isChecked
                    val physicalChecked = checkPhysical.isChecked
                    val socialChecked = checkSocial.isChecked
                    val organizingChecked = checkOrganizing.isChecked
                    val miscChecked = checkMisc.isChecked

                    val personalizedChecked = checkPersonalized.isChecked
                    //val randomChecked = checkRandom.isChecked

                    var catList: ArrayList<Category> = ArrayList<Category>()
                    if(relaxingChecked) {catList.add(Category.RELAXING)}
                    if(mentalChecked) {catList.add(Category.MENTAL)}
                    if(physicalChecked) {catList.add(Category.PHYSICAL)}
                    if(socialChecked) {catList.add(Category.SOCIAL)}
                    if(organizingChecked) {catList.add(Category.ORGANIZING)}
                    if(miscChecked) {catList.add(Category.MISC)}
                    if(personalizedChecked) {catList.add(Category.PERSONALIZED)}

                    var list: ArrayList<Challenge?>
                    list = getChosenChallenges(sharedPrefs, relaxingChecked, mentalChecked, physicalChecked, socialChecked, organizingChecked, miscChecked, personalizedChecked)

                    if (!list.isEmpty()) {
                        val intent = Intent("cat_chosen")

                        intent.putExtra("relaxingChecked", relaxingChecked)
                        intent.putExtra("mentalChecked", mentalChecked)
                        intent.putExtra("physicalChecked", physicalChecked)
                        intent.putExtra("socialChecked", socialChecked)
                        intent.putExtra("organizingChecked", organizingChecked)
                        intent.putExtra("miscChecked", miscChecked)
                        intent.putExtra("personalizedChecked", personalizedChecked)
                        intent.putExtra("randomChecked", false)

                        context?.let { it1 ->
                            LocalBroadcastManager.getInstance(it1).sendBroadcast(intent)
                        }

                        FirebaseConfig.chosenCategoriesRef?.push()?.setValue(catList)

                        dismiss()

                    } else {
                        Toast.makeText(context, "Your chosen categories are empty", Toast.LENGTH_SHORT).show()
                    }
                }
                randomCategoriesButton.setOnClickListener{
                    var list: ArrayList<Challenge?>
                    list = getRandomChallenge(sharedPrefs)

                    if (!list.isEmpty()) {
                        val intent = Intent("cat_chosen")

                        intent.putExtra("relaxingChecked", false)
                        intent.putExtra("mentalChecked", false)
                        intent.putExtra("physicalChecked", false)
                        intent.putExtra("socialChecked", false)
                        intent.putExtra("organizingChecked", false)
                        intent.putExtra("miscChecked", false)
                        intent.putExtra("randomChecked", true)

                        var catList: ArrayList<Category> = ArrayList<Category>()
                        catList.add(Category.RANDOM)


                        context?.let { it1 ->
                            LocalBroadcastManager.getInstance(it1).sendBroadcast(intent)
                        }

                        FirebaseConfig.chosenCategoriesRef?.push()?.setValue(catList)

                        dismiss()

                    } else {
                        Toast.makeText(context, "There are no challenges for your profile. Please add your owns.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return binding?.root
        }



    private fun getCurrentChallengeListFromPref(gson: Gson, sharedPrefs: SharedPreferences?): ArrayList<Challenge?> {
        val savedList = sharedPrefs?.getString("jsonChallenge", "?")
        val myType = object : TypeToken<ArrayList<Challenge>>() {}.type
        val list = gson.fromJson<ArrayList<Challenge?>>(savedList, myType)
        println("oldlist: "+list)
        return list
    }

    private fun getChosenChallenges(sharedPrefs: SharedPreferences?, relaxingChecked: Boolean?, mentalChecked: Boolean?, physicalChecked: Boolean?, socialChecked: Boolean?, organizingChecked: Boolean?, miscChecked: Boolean?, personalizedChecked: Boolean?): ArrayList<Challenge?> {
        val existingChallenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
        var chosenChallenges: ArrayList<Challenge?> = ArrayList()
        val personalizedChallenges: ArrayList<Challenge?> = ArrayList()
        if (personalizedChecked == true) {
            for (item in existingChallenges) {
                if (item?.personalized == true) {
                    personalizedChallenges.add(item)
                }
            }
            for (item in personalizedChallenges) {
                if (item?.cat?.equals(Category.RELAXING) == true && relaxingChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.MENTAL) == true && mentalChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.PHYSICAL) == true && physicalChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.SOCIAL) == true && socialChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.ORGANIZING) == true && organizingChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.MISC) == true && miscChecked == true) {
                    chosenChallenges.add(item)
                }
                if ((relaxingChecked == false)&&
                    (mentalChecked == false)&&
                    (physicalChecked == false)&&
                    (socialChecked == false)&&
                    (miscChecked == false)&&
                    (organizingChecked == false)) {
                    chosenChallenges = personalizedChallenges
                }
            }
        } else {
            for (item in existingChallenges) {
                if (item?.cat?.equals(Category.RELAXING) == true && relaxingChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.MENTAL) == true && mentalChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.PHYSICAL) == true && physicalChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.SOCIAL) == true && socialChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.ORGANIZING) == true && organizingChecked == true) {
                    chosenChallenges.add(item)
                }
                if (item?.cat?.equals(Category.MISC) == true && miscChecked == true) {
                    chosenChallenges.add(item)
                }
            }
        }
        return chosenChallenges
    }

    private fun getRandomChallenge(sharedPrefs: SharedPreferences?): ArrayList<Challenge?> {
        val existingChallenges = getCurrentChallengeListFromPref(gson, sharedPrefs)
        return existingChallenges
    }
}
