package com.example.pictoevents

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pictoevents.Processor.TextProcessor
import com.example.pictoevents.Processor.TextProcessor.TextProcessorListener
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.calendar.CalendarObject
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class progressFragment : Fragment(){
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val progressFragScope = MainScope()
    private lateinit var container: FrameLayout


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

        container = view as FrameLayout

        val context = this.requireContext()
        viewLifecycleOwner.lifecycleScope.launch{
            val textProcessor = TextProcessor(context)
            textProcessor.setTextProcessorListener(object: TextProcessorListener{
                override fun onEventCreatedComplete(successful: Boolean) {

                    progressFragScope.launch(Dispatchers.Main){
                        Repository.shouldSetCreatedDate = true

                        findNavController().navigate(R.id.action_progressFragment_to_calendarFragment)

                        val progressSpinner = view.findViewById<ProgressBar>(R.id.progressSpinner)
                        progressSpinner?.visibility = View.GONE
                        displaySnackbar()
                    }
                }
            })

            if(Repository.manuallyCreatedEvent)
            {
                Log.d(TAG, "++++++++ manual event creation +++++++")
                textProcessor.processManuallyAddedEvent()
                Repository.manuallyCreatedEvent = false
            }
            else if(Repository.automaticallyCreatedEvent){

                Log.d(TAG, "++++++++ auto event creation +++++++")
                completeAutoEventCreation(textProcessor, view)
            }
            else{
                Log.d(TAG, "++++++++ shouldn't come here +++++++")
                val progressSpinner = view.findViewById<ProgressBar>(R.id.progressSpinner)
                progressSpinner?.visibility = View.GONE
                val progressText = view.findViewById<TextView>(R.id.textView2)
                progressText.setText(R.string.eventCreatedText)
            }
        }
   }

    private suspend fun completeAutoEventCreation(
        textProcessor: TextProcessor,
        view: View
    ) {
        textProcessor.processOCR()

        Repository.automaticallyCreatedEvent = false

        while(!Repository.transitionalWorkDone){
            delay(500)
        }
        Repository.transitionalWorkDone = false
        //val progressSpinner = view.findViewById<ProgressBar>(R.id.progressSpinner)
        //progressSpinner?.visibility = View.GONE

        findNavController().navigate(R.id.action_progressFragment_to_reviewEventFragment)
    }

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