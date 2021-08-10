package com.example.pictoevents

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.navigation.fragment.findNavController
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.calendar.CalendarObject
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [reviewEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class reviewEventFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val dividerChar = "/"
    private val colonChar = ":"

    private fun manageDateDivider(working: String, position : Int, start: Int, before: Int) : String{
        if (working.length == position) {
            return if (before <= position && start < position)
                working + dividerChar
            else
                working.dropLast(1)
        }
        return working
    }

    private fun manageTimeDivider(working: String, position : Int, start: Int, before: Int) : String{
        if (working.length == position) {
            return if (before <= position && start < position)
                working + colonChar
            else
                working.dropLast(1)
        }
        return working
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<EditText>(R.id.review_editText_Title)
        val startDate = view.findViewById<EditText>(R.id.review_start_date)
        val startTime = view.findViewById<EditText>(R.id.review_start_time)
        val endDate = view.findViewById<EditText>(R.id.review_endEnd_date)
        val endTime = view.findViewById<EditText>(R.id.review_endEnd_time)
        val startAmPm = view.findViewById<Switch>(R.id.review_ampm_switch)
        val endAmPm = view.findViewById<Switch>(R.id.review_endDate_ampm_switch)

        //Date and time input listeners (TextWatcher)
        val textWatcherStart = object: TextWatcher {
            var edited = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (edited){
                    edited = false
                    return
                }

                var working = if (startDate.text.length >= 10) startDate.text.toString().substring(0,10)
                else startDate.text.toString()

                working = manageDateDivider(working, 2, start, before)
                working = manageDateDivider(working, 5, start, before)

                edited = true
                startDate.setText(working)
                startDate.setSelection(startDate.text.length)
            }

            override fun afterTextChanged(p0: Editable?) { }
        }

        startDate.addTextChangedListener(textWatcherStart);

        val textWatcherEnd = object: TextWatcher {
            var edited = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (edited){
                    edited = false
                    return
                }

                var working = if (endDate.text.length >= 10) endDate.text.toString().substring(0,10)
                else endDate.text.toString()

                working = manageDateDivider(working, 2, start, before)
                working = manageDateDivider(working, 5, start, before)

                edited = true
                endDate.setText(working)
                endDate.setSelection(endDate.text.length)
            }

            override fun afterTextChanged(p0: Editable?) { }
        }

        endDate.addTextChangedListener(textWatcherEnd);

        val textWatcherStartTime = object: TextWatcher {
            var edited = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (edited){
                    edited = false
                    return
                }

                var working = if (startTime.text.length >= 5) startTime.text.toString().substring(0,5)
                else startTime.text.toString()

                working = manageTimeDivider(working, 2, start, before)

                edited = true
                startTime.setText(working)
                startTime.setSelection(startTime.text.length)
            }

            override fun afterTextChanged(p0: Editable?) { }
        }

        startTime.addTextChangedListener(textWatcherStartTime);

        val textWatcherEndTime = object: TextWatcher {
            var edited = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                if (edited){
                    edited = false
                    return
                }

                var working = if (endTime.text.length >= 5) endTime.text.toString().substring(0,5)
                else endTime.text.toString()

                working = manageTimeDivider(working, 2, start, before)

                edited = true
                endTime.setText(working)
                endTime.setSelection(endTime.text.length)
            }

            override fun afterTextChanged(p0: Editable?) { }
        }

        endTime.addTextChangedListener(textWatcherEndTime);

        //Get text from repository
        val calObject = Repository.calendarObject
        val calendar = this.initializeACalendar((calObject))
        val sdfDate = SimpleDateFormat("MM/dd/yyyy")
        val sdfTime = SimpleDateFormat("hh:mm")
        val sdfAMPM = SimpleDateFormat("a")

        // set Text fields
        title.setText(calObject.title)
        startDate.setText(sdfDate.format(calendar.time))
        startTime.setText(sdfTime.format(calendar.time))
        val ampm =  sdfAMPM.format(calendar.time)
        if(ampm.equals("AM")) startAmPm.isChecked = false

        //Buttons
        view.findViewById<Button>(R.id.review_cancel_add_event).setOnClickListener {
            resetRepository()
            findNavController().navigate(R.id.action_reviewEventFragment_to_calendarFragment)
        }

        view.findViewById<Button>(R.id.review_ok_add_event).setOnClickListener {
            var ampmS = if(startAmPm.isChecked) startAmPm.textOn else startAmPm.textOff

            Repository.manualText = "${startDate.text}, ${startTime.text}, ${ampmS}"
            Repository.eventTitle = title.text.toString()
            Repository.manuallyCreatedEvent = true
            Repository.automaticallyCreatedEvent = false

            findNavController().navigate(R.id.action_reviewEventFragment_to_progressFragment)
        }
    }

    private fun initializeACalendar(calendarObj: CalendarObject): Calendar {

        val calendar = GregorianCalendar()

        calendar.timeZone = TimeZone.getDefault()
        if (calendarObj.year != 0){
            calendar.set(Calendar.YEAR, calendarObj.year)
        }

        if(calendarObj.month != 0){
            calendar.set(Calendar.MONTH, calendarObj.month - 1)
        }

        if(calendarObj.dayOfMonth != 0){
            calendar.set(Calendar.DAY_OF_MONTH, calendarObj.dayOfMonth)
        }

        if(calendarObj.hour != 0){
            calendar.set(Calendar.HOUR, calendarObj.hour)
        }

        if(calendarObj.second != 0){
            calendar.set(Calendar.SECOND, calendarObj.second)
        }

        if(calendarObj.AmPm == 0){
            calendar.set(Calendar.AM_PM, Calendar.AM)
        }
        else{
            calendar.set(Calendar.AM_PM, Calendar.PM)
        }
        calendar.set(Calendar.MINUTE, calendarObj.minute)

        return calendar
    }

    fun resetRepository()
    {
        Repository.eventTitle = ""
        Repository.text = ""
        Repository.eventCreationCompletedSuccessfully = false
        Repository.manuallyCreatedEvent = false
        Repository.automaticallyCreatedEvent = false
        Repository.manualText = ""
        Repository.shouldSetCreatedDate = false
        Repository.transitionalWorkDone = false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment reviewEventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            reviewEventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}