package com.example.drinkwaternow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChangeCupAdapter(private val dataSet: List<Int>, private val listener: OnCupClickListener) :
    RecyclerView.Adapter<ChangeCupAdapter.ViewHolder>() {

    interface OnCupClickListener {
        fun onCupClick(input: String)
    }

    inner class ViewHolder(view: View, viewGroup: ViewGroup) : RecyclerView.ViewHolder(view) {
        val imageButton: ImageButton
        val textView: TextView
        var cupSize: Int = 0
        init {
            // Define click listener for the ViewHolder's View.
            imageButton = view.findViewById(R.id.imageButtonRVItem)
            textView = view.findViewById(R.id.ImageButtonText)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.change_cup_row_item, viewGroup, false)
        println("viewHolder created")
        return ViewHolder(view,viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.imageButton.setOnClickListener { listener.onCupClick(dataSet[position].toString()) }

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].toString()+ "мл"
        viewHolder.cupSize = dataSet[position]
        when (dataSet[position]){
            200 -> viewHolder.imageButton.setImageResource(R.drawable.ic_glass_200_gradient)
            250 -> viewHolder.imageButton.setImageResource(R.drawable.ic_glass_250_gradient)
            300 -> viewHolder.imageButton.setImageResource(R.drawable.ic_cup_300_gradient)
            500 -> viewHolder.imageButton.setImageResource(R.drawable.ic_bottle_500_gradient)
            1000 -> viewHolder.imageButton.setImageResource(R.drawable.ic_bottle_1000_gradient)
            //TODO добавление нового сосуда
//            0 -> {
//                viewHolder.imageButton.setImageResource(R.drawable.water_glass_250ml)
//                viewHolder.textView.text = "Новая\nчашка"
//            }
        }
        println("dataSet")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
