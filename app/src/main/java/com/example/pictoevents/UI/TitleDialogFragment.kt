package com.example.pictoevents.UI

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pictoevents.R
import com.example.pictoevents.Repository.Repository

class TitleDialogFragment : DialogFragment() {

    internal lateinit var listener :TitleDialogFragmentListener
    var titleSelected = ""

    interface TitleDialogFragmentListener{
        fun onDialogPositiveClick(dialog : DialogFragment)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        try{
            listener = context as TitleDialogFragmentListener
        } catch (e: ClassCastException){
            throw ClassCastException((context.toString() +
                    " must implement TitleDialogFragmentListener"))
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val args = arguments
        var optionA = args?.get("primary")
        optionA =  if (optionA != null)  optionA else "Title not found"

        var optionB = args?.get("secondary")
        optionB = if(optionB != null) optionB else "Title not found"

        //Add each item to a list
        val options = arrayOf(
        getString(R.string.title_option_A, optionA),
        getString(R.string.title_option_B, optionB),
        getString(R.string.title_option_Custom))

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.TitleDialogMessage))
            .setSingleChoiceItems(options, -1, DialogInterface.OnClickListener(){
                    _, which ->
                titleSelected = options[which]
            })
            .setPositiveButton(getString(R.string.TitleDialogConfirm))
                { dialog, which ->
                    Log.d(TAG, "Which selected is: ${which}: $titleSelected")
                    val selection = titleSelected
                    Repository.eventTitle = selection
                    Log.d(TAG, "Selection: $selection")
                    listener.onDialogPositiveClick(this)
                }
            .create()
    }

    companion object{
        const val TAG = "SelectTitleDialog"
    }
}
