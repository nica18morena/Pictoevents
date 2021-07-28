package com.example.pictoevents.UI

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

import com.example.pictoevents.R
import com.example.pictoevents.Repository.Repository

class AddEventFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<EditText>(R.id.editText_Title)
        val startDate = view.findViewById<EditText>(R.id.start_date)
        val startTime = view.findViewById<EditText>(R.id.start_time)
        val startAmPm = view.findViewById<Switch>(R.id.ampm_switch)
        val endDate = view.findViewById<EditText>(R.id.endEnd_date)
        val endTime = view.findViewById<EditText>(R.id.endEnd_time)
        val endAmPm = view.findViewById<Switch>(R.id.endDate_ampm_switch)

        //Date and time input listeners (TextWatcher)
        val textWatcherStart = object: TextWatcher{
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

        val textWatcherEnd = object: TextWatcher{
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

        val textWatcherStartTime = object: TextWatcher{
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

        val textWatcherEndTime = object: TextWatcher{
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

        //Buttons
        view.findViewById<Button>(R.id.cancel_add_event).setOnClickListener {
            findNavController().navigate(R.id.action_addEventFragment_to_calendarFragment)
        }

        view.findViewById<Button>(R.id.ok_add_event).setOnClickListener {
            var ampmS = if(startAmPm.isChecked) startAmPm.textOn else startAmPm.textOff

            Repository.manualText = "${startDate.text}, ${startTime.text}, ${ampmS}"
            Repository.eventTitle = title.text.toString()
            //update manually added boolean to true
            Repository.manuallyCreatedEvent = true

            findNavController().navigate(R.id.action_addEventFragment_to_progressFragment)
        }
    }
}
