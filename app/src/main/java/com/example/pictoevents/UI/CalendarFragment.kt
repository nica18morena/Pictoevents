package com.example.pictoevents.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.example.pictoevents.Calendar.PictoCalendar
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
        //val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        //val events: MutableList<EventDay> = ArrayList()

        val calendar: Calendar = Calendar.getInstance()
        //events.add(EventDay(calendar, R.drawable.ic_arrow_right))

        //Add dummy event
        //val cal2 = Calendar.getInstance()
        //val info = Calendar.MONTH
        //cal2.setTime(Date(pictoCalendarEvents[0]))
        //cal2.add(Calendar.MONTH, 5)
        //events.add(EventDay(cal2, R.drawable.ic_camera))

        val events = this.setCalendarEvents(pictoCalendarEvents)
        val calendarView = view.findViewById<com.applandeo.materialcalendarview.CalendarView>(R.id.calendarView)
        calendarView.setEvents(events)

        calendarView.setDate(calendar)
        //val selectedDate = calendarView.dates
        //calendarView.setCalendarList(pictoCalendarEvents)
        /*calendarView.setOnDateChangeListener{view, year, month, dayOfMonth ->
            val msg = "Date changed"
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }*/
    }

    fun setCalendarEvents(list: MutableList<String>): MutableList<EventDay> {
        val events: MutableList<EventDay> = ArrayList()

        if (list.size > 0 ){
            list.forEach {
                var calendar: Calendar = Calendar.getInstance()
                var tokens = it.split(",", " ")
                var date = tokens[1].toString().replaceAfter(" ","")
                calendar.time = Date(date.toLong())

                events.add(EventDay(calendar, R.drawable.ic_face_24px))
            }
        }
        return events
    }
    // TODO: Rename method, update argument and hook method into UI event
    /*fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }*/

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }*/

    /*override fun onDetach() {
        super.onDetach()
        listener = null
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    /*interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }*/

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}
