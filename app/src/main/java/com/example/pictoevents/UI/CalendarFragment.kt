package com.example.pictoevents.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.example.pictoevents.Calendar.PictoCalendar
import com.example.pictoevents.Pattern.RegExPatterns
import com.example.pictoevents.R
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CalendarFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

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

        calendarView.setDate(calendar)
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
