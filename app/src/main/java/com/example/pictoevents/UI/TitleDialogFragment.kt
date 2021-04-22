package com.example.pictoevents.UI

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pictoevents.R


class TitleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.TitleDialogMessage))
            .setTitle(getString(R.string.EventTitle))
            .setMultiChoiceItems(arrayOf("Title A", "Title B", "Custom"), null, DialogInterface.OnMultiChoiceClickListener({
                    dialog, which, isChecked ->
                if (isChecked) {
                    selectedTitle = which
                }
            }))
            .setPositiveButton(getString(R.string.TitleDialogConfirm)) { _,_ -> }
            .create()

    companion object{
        const val TAG = "SelectTitleDialog"
        var selectedTitle = 0
    }
}
