package com.example.pictoevents.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.pictoevents.Processor.TextProcessor

import com.example.pictoevents.R
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.UI.AddEvent.DialogDatePickerFragment
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddEventFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEventFragment : Fragment() {

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
        //Buttons
        view.findViewById<Button>(R.id.cancel_add_event).setOnClickListener {
            findNavController().navigate(R.id.action_addEventFragment_to_calendarFragment)
        }
        view.findViewById<Button>(R.id.ok_add_event).setOnClickListener {
            //Need to gather title, start date, start time, ampm, end date, end time and ampm

            //set Repository.Text to string
            Repository.manualText = "${startDate.text}, ${startTime.text}, ${startAmPm.text}"
            Repository.eventTitle = title.text.toString()
            //update manually added boolean to true
            Repository.manuallyCreatedEvent = true
            //call to textprocessor?- should call to text processor be in progress frag?

            findNavController().navigate(R.id.action_addEventFragment_to_progressFragment)
        }

    }

    fun showDatePickerDialog(v: View) {
        var datePickerFragment: DialogDatePickerFragment = DialogDatePickerFragment()
        DialogDatePickerFragment().show(childFragmentManager, "datePicker")
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
         * @return A new instance of fragment AddEventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}
