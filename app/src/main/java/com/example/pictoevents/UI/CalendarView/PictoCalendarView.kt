package com.example.pictoevents.UI.CalendarView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.widget.CalendarView
import java.util.*


class PictoCalendarView(context: Context, attrs: AttributeSet) : CalendarView(context, attrs) {

    private val todayCalender: Calendar? = null
    private val currentCalender: Calendar? = null
    private val dayPaint = Paint()
    private val screenDensity = 1
    //scale small indicator by screen density
    private val smallIndicatorRadius = 2.5f * screenDensity
    private lateinit var calendarList : List<String>
    private lateinit var dates : LongArray

    //makes easier to find radius
    //val bigCircleIndicatorRadius = getInterpolatedBigCircleIndicator();
    override fun onDraw(canvas: Canvas){
        this.drawEvents(canvas)
        super.onDraw(canvas)
    }

    fun drawEvents(
        canvas: Canvas?
    ) {
        val uniqEvents: List<String> = this.calendarList

        val todayDayOfMonth: Int? = todayCalender?.get(Calendar.DAY_OF_MONTH)
        val currentYear: Int? = todayCalender?.get(Calendar.YEAR)
        val selectedDayOfMonth: Int? = currentCalender?.get(Calendar.DAY_OF_MONTH)

        if (uniqEvents != null) {
            for (i in uniqEvents.indices) {
                val events = uniqEvents[i]
                val widthPerDay = this.width/ 7 //Days in a week
                val hightPerDay = this.height
                val accumulatedScrollOffset = PointF()
            }
        }
    }

    private fun drawEventIndicatorCircle(
        canvas: Canvas,
        x: Float,
        y: Float,
        color: Int
    ) {
        dayPaint.setColor(color)
            dayPaint.setStyle(Paint.Style.FILL)
            drawCircle(canvas, smallIndicatorRadius, x, y)
    }

    private fun drawCircle(
        canvas: Canvas,
        radius: Float,
        x: Float,
        y: Float
    ) {
        canvas.drawCircle(x, y, radius, dayPaint)
    }

    fun setCalendarList(list : List<String>){
        this.calendarList = list
    }
}
