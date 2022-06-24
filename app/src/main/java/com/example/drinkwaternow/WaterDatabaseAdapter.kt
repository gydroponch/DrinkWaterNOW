package com.example.drinkwaternow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class WaterDatabaseAdapter(val context: Context, val items: ArrayList<WaterModelClass>) :
RecyclerView.Adapter<WaterDatabaseAdapter.ViewHolder>() {

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each item to
        val llMain: ConstraintLayout
        val tvDateTime : TextView
        val tvVolume : TextView
        val intakeImageView: ImageView
        init {
            intakeImageView = view.findViewById(R.id.intakeImageView)
            llMain = view.findViewById(R.id.llMain)
            tvDateTime = view.findViewById(R.id.tvDateTime)
            tvVolume = view.findViewById(R.id.tvVolume)

        }
    }

    /**
     * Inflates the item views which is designed in the XML layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.intake_row_item,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items.get(position)

        holder.tvDateTime.text = item.DateTime
        holder.tvVolume.text = (item.Volume.toString())+" мл"
        holder.intakeImageView.setImageResource(R.drawable.water_glass_250ml)

        // Updating the background color according to the odd/even positions in list.
        /*if (position % 2 == 0) {
            holder.llMain.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.progressBarBackground
                )
            )
        } else {
            holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }
        */
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return items.size
    }
}