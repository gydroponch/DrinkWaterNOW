package com.example.drinkwaternow

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import lecho.lib.hellocharts.formatter.AxisValueFormatter
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import kotlin.collections.ArrayList

class StatisticsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_activity)
        val goBackButton = findViewById<Button>(R.id.goBackButtonStats)
        val lineChartView = findViewById<LineChartView>(R.id.lineChart)
        val textViewError = findViewById<TextView>(R.id.textViewError)

        lineChartView.isInteractive = true
        lineChartView.isZoomEnabled = true
        lineChartView.zoomType = ZoomType.HORIZONTAL

        val values: MutableList<PointValue> = ArrayList()
        val axisValuesX: MutableList<AxisValue> = ArrayList()
        val axisValuesY: MutableList<AxisValue> = ArrayList()

        val intakesList = getIntakesByDayList()

        val calendar = Calendar.getInstance()

        if (intakesList.size > 0) {
            for (i in 0 until intakesList.size) {
                val intake = intakesList[i]
                calendar.timeInMillis=intake.DateTime
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = SimpleDateFormat("MMM").format(calendar.time)
                val labelString = "$day $month"
                println(labelString)
                values.add(PointValue(day.toFloat(), intake.Volume.toFloat()))
                axisValuesX.add(AxisValue(day.toFloat()).setLabel(labelString))
                axisValuesY.add(AxisValue(intake.Volume.toFloat()))
                println("i = $day: ${intake.Volume}   ${values[i]}")
            }
        } else textViewError.text = "Недостаточно данных для построения \nграфика! Пейте воду ещё пару дней!"


        val line = Line(values).setColor(ContextCompat.getColor(applicationContext, R.color.darkerBlueForStatusBar)).setHasPoints(true)
        val lines: MutableList<Line> = ArrayList()
        lines.add(line)

        val data = LineChartData()
        data.lines = lines

        val axisX = Axis()
        axisX.name = "Дата"
        axisX.textColor = ContextCompat.getColor(applicationContext, R.color.blueForStatusBar)
        axisX.textSize = 14
        axisX.values = axisValuesX

        val axisY = Axis()
        axisY.name = "Вода, мл."
        axisY.textColor = ContextCompat.getColor(applicationContext, R.color.blueForStatusBar)
        axisY.textSize = 14
        axisY.values = axisValuesY

        data.axisXBottom = axisX
        data.axisYLeft = axisY

        lineChartView.lineChartData = data

        goBackButton.setOnClickListener{
            finish()
        }
    }

    private fun getIntakesByDayList(): ArrayList<WaterModelClass> {
        //creating the instance of DatabaseHandler class
        val databaseHandler = WaterDatabaseHandler(this)
        //calling the viewEmployee method of DatabaseHandler class to read the records
        val intakeList: ArrayList<WaterModelClass> = databaseHandler.viewIntakesByDay()

        return intakeList
    }
}
