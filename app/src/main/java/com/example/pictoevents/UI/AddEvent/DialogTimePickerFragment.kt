package com.example.pictoevents.UI.AddEvent


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment

import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DialogTimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_calendar_dialog_picker, container, false)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))

        //return super.onCreateDialog(savedInstanceState)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            //TODO: Save the time input into a calendar object
    }
}
