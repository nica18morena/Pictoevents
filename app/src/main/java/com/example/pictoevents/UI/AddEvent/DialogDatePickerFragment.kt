package com.example.pictoevents.UI.AddEvent


import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

import com.example.pictoevents.R
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DialogDatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    //var listener: OnDateRecievedListener? = null
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_dialog_date_picker, container, false)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = context as? OnDateRecievedListener
//        if(listener == null){
//            throw ClassCastException("$context must implement OnDateRecievedListener")
//        }
//    }
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        //listener?.onDateRecieved(year, month, day)
    }

//    interface OnDateRecievedListener{
//        fun onDateRecieved(year: Int, month: Int, day: Int)
//    }
}
