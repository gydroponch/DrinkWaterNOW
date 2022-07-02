package com.example.drinkwaternow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WaterDatabaseAdapter(val context: Context, val items: ArrayList<WaterModelClass>) :
RecyclerView.Adapter<WaterDatabaseAdapter.ViewHolder>() {

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each item to
        private val constraintLayoutMain: ConstraintLayout
        val dateTimeTextView: TextView
        val volumeTextView: TextView
        val intakeImageView: ImageView
        var calendar = Calendar.getInstance()
        init {
            intakeImageView = view.findViewById(R.id.intakeImageView)
            constraintLayoutMain = view.findViewById(R.id.llMain)
            dateTimeTextView = view.findViewById(R.id.tvDateTime)
            volumeTextView = view.findViewById(R.id.tvVolume)
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
        holder.calendar.timeInMillis=item.DateTime
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.dateTimeTextView.text = df.format(holder.calendar.time)
        holder.volumeTextView.text = (item.Volume.toString()) + " мл"

        when (item.Volume) {
            200 -> holder.intakeImageView.setImageResource(R.drawable.ic_glass_200_gradient)
            250 -> holder.intakeImageView.setImageResource(R.drawable.ic_glass_250_gradient)
            300 -> holder.intakeImageView.setImageResource(R.drawable.ic_cup_300_gradient)
            500 -> holder.intakeImageView.setImageResource(R.drawable.ic_bottle_500_gradient)
            1000 -> holder.intakeImageView.setImageResource(R.drawable.ic_bottle_1000_gradient)
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return items.size
    }
}