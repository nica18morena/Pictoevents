package com.example.pictoevents.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.example.pictoevents.calendar.PictoCalendar
import com.example.pictoevents.Pattern.RegExPatterns
import com.example.pictoevents.R
import com.example.pictoevents.Repository.Repository
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pictoCalendar = PictoCalendar(this.requireContext())
        pictoCalendar.checkCalendars()
        val pictoCalendarEvents = pictoCalendar.getAllCalendarEvents()

        val calendar: Calendar = Calendar.getInstance()

        val events = this.setCalendarEvents(pictoCalendarEvents)
        val calendarView = view.findViewById<com.applandeo.materialcalendarview.CalendarView>(R.id.calendarView)
        calendarView.setEvents(events)

        if(Repository.isNavigationFromProgressFrag){
            calendarView.setDate(Repository.calendar)
            Repository.isNavigationFromProgressFrag = false
        }
        else calendarView.setDate(calendar)
    }

    fun setCalendarEvents(list: MutableList<String>): MutableList<EventDay> {
        val events: MutableList<EventDay> = ArrayList()

        if (list.size > 0 ){
            list.forEach {
                var calendar: Calendar = Calendar.getInstance()
                var tokens = it.split(",")
                //iterate through
                for(token in tokens){
                    if(!token.isNullOrEmpty() && !token.contains(RegExPatterns.WORD.toRegex())){
                        var date = token.replaceAfter(" ","")
                        calendar.time = Date(date.toLong())

                        events.add(EventDay(calendar, R.drawable.ic_face_24px))
                        break
                    }
                }
            }
        }
        return events
    }
}
