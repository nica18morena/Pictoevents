package com.example.pictoevents.UI.CalendarView

import android.R.attr
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.provider.CalendarContract.Events
import android.util.AttributeSet
import android.widget.CalendarView
import java.util.*


class PictoCalendarView(context: Context, attrs: AttributeSet) : CalendarView(context, attrs) {
    val FILL_LARGE_INDICATOR = 1
    val NO_FILL_LARGE_INDICATOR = 2
    val SMALL_INDICATOR = 3
    //TODO: Deal with these varialbes to not be null
    private val todayCalender: Calendar? = null
    private val currentCalender: Calendar? = null
    private val dayPaint = Paint()
    private val screenDensity = 1
    //scale small indicator by screen density
    private val smallIndicatorRadius = 2.5f * screenDensity
    private lateinit var calendarList : List<String>

    //makes easier to find radius
    //val bigCircleIndicatorRadius = getInterpolatedBigCircleIndicator();
    override fun onDraw(canvas: Canvas){

    }

    fun drawEvents(
        canvas: Canvas?,
        currentMonthToDrawCalender: Calendar,
        offset: Int,
        eventList: List<Events>
    ) {
        val currentMonth: Int = currentMonthToDrawCalender.get(Calendar.MONTH)
        val uniqEvents: List<Events> = eventList
     /*       eventsContainer.getEventsForMonthAndYear(
            currentMonth,
            currentMonthToDrawCalender.get(Calendar.YEAR)
        )*/
//        val shouldDrawCurrentDayCircle =
//            currentMonth == todayCalender.get(Calendar.MONTH)
//        val shouldDrawSelectedDayCircle =
//            currentMonth == currentCalender.get(Calendar.MONTH)
        val todayDayOfMonth: Int? = todayCalender?.get(Calendar.DAY_OF_MONTH)
        val currentYear: Int? = todayCalender?.get(Calendar.YEAR)
        val selectedDayOfMonth: Int? = currentCalender?.get(Calendar.DAY_OF_MONTH)
//        val indicatorOffset: Float = bigCircleIndicatorRadius / 2
        if (uniqEvents != null) {
            for (i in uniqEvents.indices) {
                val events = uniqEvents[i]
//                val timeMillis: Long = events.getTimeInMillis()
//                    .setTimeInMillis(timeMillis)
 //               var dayOfWeek: Int = getDayOfWeek(eventsCalendar)
//                if (isRtl) {
//                    dayOfWeek = 6 - dayOfWeek
//                }
//                val weekNumberForMonth: Int = eventsCalendar.get(Calendar.WEEK_OF_MONTH)
                val widthPerDay = this.width/ 7 //Days in a week
                val hightPerDay = this.height
                val accumulatedScrollOffset = PointF()
//                val xPosition: Float =
//                    widthPerDay * dayOfWeek + (widthPerDay/2) + attr.paddingLeft +  offset - attr.paddingRight
//                var yPosition: Float = weekNumberForMonth * heightPerDay + paddingHeight
//                if ((animationStatus === EXPOSE_CALENDAR_ANIMATION || animationStatus === ANIMATE_INDICATORS) && xPosition >= growFactor || yPosition >= growFactor) {
//                    // only draw small event indicators if enough of the calendar is exposed
//                    continue
//                } else if (animationStatus === EXPAND_COLLAPSE_CALENDAR && yPosition >= growFactor) {
//                    // expanding animation, just draw event indicators if enough of the calendar is visible
//                    continue
//                } else if (animationStatus === EXPOSE_CALENDAR_ANIMATION && (eventIndicatorStyle === FILL_LARGE_INDICATOR || eventIndicatorStyle === NO_FILL_LARGE_INDICATOR)) {
//                    // Don't draw large indicators during expose animation, until animation is done
//                    continue
//                }
//                val eventsList: List<Event> = events.getEvents()
//                val dayOfMonth: Int = eventsCalendar.get(Calendar.DAY_OF_MONTH)
//                val eventYear: Int = eventsCalendar.get(Calendar.YEAR)
//                val isSameDayAsCurrentDay =
//                    shouldDrawCurrentDayCircle && todayDayOfMonth == dayOfMonth && eventYear == currentYear
//                val isCurrentSelectedDay =
//                    shouldDrawSelectedDayCircle && selectedDayOfMonth == dayOfMonth
//                if (shouldDrawIndicatorsBelowSelectedDays || !shouldDrawIndicatorsBelowSelectedDays && !isSameDayAsCurrentDay && !isCurrentSelectedDay || animationStatus === EXPOSE_CALENDAR_ANIMATION) {
//                    if (eventIndicatorStyle === FILL_LARGE_INDICATOR || eventIndicatorStyle === NO_FILL_LARGE_INDICATOR) {
//                        if (!eventsList.isEmpty()) {
//                            val event: Event = eventsList[0]
//                            drawEventIndicatorCircle(canvas, xPosition, yPosition, event.getColor())
//                        }
//                    } else {
//                        yPosition += indicatorOffset
//                        // offset event indicators to draw below selected day indicators
//                        // this makes sure that they do no overlap
//                        if (shouldDrawIndicatorsBelowSelectedDays && (isSameDayAsCurrentDay || isCurrentSelectedDay)) {
//                            yPosition += indicatorOffset
//                        }
//                        if (eventsList.size >= 3) {
//                            drawEventsWithPlus(canvas, xPosition, yPosition, eventsList)
//                        } else if (eventsList.size == 2) {
//                            drawTwoEvents(canvas, xPosition, yPosition, eventsList)
//                        } else if (eventsList.size == 1) {
//                            drawSingleEvent(canvas, xPosition, yPosition, eventsList)
//                        }
//                    }
//                }
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
    //assume square around each day of width and height = heightPerDay and get diagonal line length
    //interpolate height and radius
    //https://en.wikipedia.org/wiki/Linear_interpolation
//    private fun getInterpolatedBigCircleIndicator(): Float {
//        val x0: Float = textSizeRect.height()
//        val x1: Float = heightPerDay // take into account indicator offset
//        val x: Float =
//            (x1 + textSizeRect.height()) / 2f // pick a point which is almost half way through heightPerDay and textSizeRect
//        val y1 = 0.5 * Math.sqrt(x1 * x1 + (x1 * x1).toDouble())
//        val y0 = 0.5 * Math.sqrt(x0 * x0 + (x0 * x0).toDouble())
//        return (y0 + (y1 - y0) * ((x - x0) / (x1 - x0))).toFloat()
//    }

    // zero based indexes used internally so instead of returning range of 1-7 like calendar class
    // it returns 0-6 where 0 is Sunday instead of 1
//    fun getDayOfWeek(calendar: Calendar): Int {
//        var dayOfWeek: Int = calendar[Calendar.DAY_OF_WEEK] - firstDayOfWeekToDraw
//        dayOfWeek = if (dayOfWeek < 0) 7 + dayOfWeek else dayOfWeek
//        return dayOfWeek
//    }

//    fun shouldDrawIndicatorsBelowSelectedDays(shouldDrawIndicatorsBelowSelectedDays: Boolean) {
//        compactCalendarController.shouldDrawIndicatorsBelowSelectedDays(
//            shouldDrawIndicatorsBelowSelectedDays
//        )
//    }
//
//    *
//     * Adds multiple events to the calendar and invalidates the view once all events are added.
//
//    fun addEvents(events: List<Event?>?) {
//        compactCalendarController.addEvents(events)
//        invalidate()
//    }
//
//    *
//     * Fetches the events for the date passed in
//     * @param date
//     * @return
//
//    fun getEvents(date: Date): List<Event?>? {
//        return compactCalendarController.getCalendarEventsFor(date.getTime())
//    }
//
//    *
//     * Fetches the events for the epochMillis passed in
//     * @param epochMillis
//     * @return
//
//    fun getEvents(epochMillis: Long): List<Event?>? {
//        return compactCalendarController.getCalendarEventsFor(epochMillis)
//    }
//
//    *
//     * Fetches the events for the month of the epochMillis passed in and returns a sorted list of events
//     * @param epochMillis
//     * @return
//
//    fun getEventsForMonth(epochMillis: Long): List<Event?>? {
//        return compactCalendarController.getCalendarEventsForMonth(epochMillis)
//    }
}
