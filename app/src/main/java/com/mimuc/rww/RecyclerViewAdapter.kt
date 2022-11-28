package com.mimuc.rww

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.mimuc.rww.databinding.HeaderChallengeBinding
import com.mimuc.rww.databinding.HeaderLogBinding
import com.mimuc.rww.databinding.SingleItemBinding
import kotlinx.android.synthetic.main.fragment_current_challenge.*
import java.util.ArrayList

class RecyclerViewAdapter(
    var challenges: ArrayList<Challenge?>
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val UNKNOWN: String = "???"

    override fun getItemViewType(position: Int): Int {
        if(challenges[position]?.title.equals("Challenge Header")) {
            return 2
        } else if (challenges[position]?.title.equals("Log Header")){
            return 1
        }
        return 0
    }

    class MyViewHolder(val binding: SingleItemBinding) : RecyclerView.ViewHolder(binding.root)

    class MyViewHolder2(val binding: HeaderChallengeBinding): RecyclerView.ViewHolder(binding.root)

    class MyViewHolder3(val binding: HeaderLogBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0){
            val binding = SingleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyViewHolder(binding)
        } else if (viewType == 2){
            val binding = HeaderChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyViewHolder2(binding)
        } else{
            val binding = HeaderLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyViewHolder3(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(challenges[position]?.title.equals("Challenge Header")){
            holder as MyViewHolder2
            holder.binding.textView3.text = "(${challenges.size-1})"
        } else if(challenges.get(position)?.title.equals("Log Header")){
            holder as MyViewHolder3
            holder.binding.textView8.text = "(${challenges.size-1})"
        } else {
            holder as MyViewHolder
            holder.binding.apply {
                challengeTitle.text = challenges[position]?.title ?: UNKNOWN
                //challengeDesc.text = challenges[position]?.desc ?: UNKNOWN
                challengeDate.text = challenges[position]?.date ?: UNKNOWN
                challengeTime.text = challenges[position]?.time ?: UNKNOWN
                answerTv.text = challenges[position]?.answer ?: UNKNOWN
                number.text = (position).toString()
                /**when (challenges[position]?.cat){
                    Category.RELAXING -> singleCategoryIcon.setImageResource(R.drawable.ic_small_relax)
                    Category.MENTAL-> singleCategoryIcon.setImageResource(R.drawable.ic_small_mental)
                    Category.PHYSICAL -> singleCategoryIcon.setImageResource(R.drawable.ic_small_physical)
                    Category.SOCIAL -> singleCategoryIcon.setImageResource(R.drawable.ic_small_social)
                    Category.ORGANIZING -> singleCategoryIcon.setImageResource(R.drawable.ic_small_organizing)
                }**/
                challenges[position]?.cat?.let { singleCategoryIcon.setImageResource(it.iconSmall) }

            }
        }
    }

    override fun getItemCount(): Int {
        return challenges.size
    }


}