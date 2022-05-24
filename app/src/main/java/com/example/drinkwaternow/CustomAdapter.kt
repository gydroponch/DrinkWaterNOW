package com.example.drinkwaternow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val dataSet: List<Int>, private val listener: OnCupClickListener) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

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
//            imageButton.setOnClickListener{
//                Toast.makeText(viewGroup.context,"Выбрана чашка (${textView.text})",Toast.LENGTH_SHORT).show()
//            }
            println("viewHolder init")
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.imagebutton_row_item, viewGroup, false)
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
            200 -> viewHolder.imageButton.setImageResource(R.drawable.water_glass_200ml)
            250 -> viewHolder.imageButton.setImageResource(R.drawable.water_glass_250ml)
            500 -> viewHolder.imageButton.setImageResource(R.drawable.water_bottle_500ml)
            1000 -> viewHolder.imageButton.setImageResource(R.drawable.water_bottle_1000ml)
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
