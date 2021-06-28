package com.example.pictoevents

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pictoevents.Processor.TextProcessor
import com.example.pictoevents.Processor.TextProcessor.TextProcessorListener
import com.example.pictoevents.Repository.Repository
import com.google.android.material.snackbar.Snackbar
import java.lang.ClassCastException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [progressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class progressFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //private lateinit var spinner: ProgressBar
    private lateinit var container: FrameLayout
    //private lateinit var listener: TextProcessorListener

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
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //spinner = view.findViewById(R.id.spinner)
        container = view as FrameLayout
        val textProcessor = TextProcessor(this.requireContext())
        //listener = TextProcessorListener
        //textProcessor.setTextProcessorListener(TextProcessorListener)
        textProcessor.processOCR()

/*        while(!Repository.eventCreationCompletedSuccessfully)
        {
            Log.d(TAG, "Waiting for event creation success")
        }*/
        //displaySnackbar()
    }
    /*override fun onEventCreatedComplete(successful: Boolean) {
        var progressSpinner = view?.findViewById<ProgressBar>(R.id.progressSpinner)
        progressSpinner?.visibility = View.GONE
        displaySnackbar()
    }*/

    fun displaySnackbar(){
        Log.d(TAG, "++++++++ Start displaySnackbar() +++++++")
        Snackbar.make(container, getString(R.string.event_created_notification,
            Repository.eventTitle), Snackbar.LENGTH_LONG).show()
    }
    companion object {
        private const val TAG = "ProgressFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment progressFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            progressFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}