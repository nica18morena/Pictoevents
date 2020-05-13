package com.example.pictoevents.UI

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract.renameDocument
import android.text.TextUtils.replace
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.transaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pictoevents.MainActivity

import com.example.pictoevents.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val OPEN_DIRECTORY_REQUEST_CODE = 0xf11e
private const val ARG_DIRECTORY_URI = "com.example.android.directoryselection.ARG_DIRECTORY_URI"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FilesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FilesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FilesFragment : Fragment() {
    private lateinit var directoryUri: Uri

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DirectoryEntryAdapter

    private lateinit var viewModel: DirectoryFragmentViewModel

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
        directoryUri = arguments?.getString(ARG_DIRECTORY_URI)?.toUri()
            ?: throw IllegalArgumentException("Must pass URI of directory to open")

        viewModel = ViewModelProviders.of(this)
            .get(DirectoryFragmentViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_files, container, false)
        recyclerView = view.findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)

        adapter = DirectoryEntryAdapter(object : ClickListeners {
            override fun onDocumentClicked(clickedDocument: CachingDocumentFile) {
                viewModel.documentClicked(clickedDocument)
            }

            override fun onDocumentLongClicked(clickedDocument: CachingDocumentFile) {
                //renameDocument(clickedDocument)
            }
        })

        recyclerView.adapter = adapter

        viewModel.documents.observe(this, Observer { documents ->
            documents?.let { adapter.setEntries(documents) }
        })

        /*viewModel.openDirectory.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { directory ->
                (activity as? MainActivity)?.showDirectoryContents(directory.uri)
            }
        })*/

        viewModel.openDocument.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { document ->
                openDocument(document)
            }
        })

        return view
    }

    private fun openDocument(document: CachingDocumentFile) {
        try {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                data = document.uri
            }
            startActivity(openIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.error_no_activity, document.name),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    /*private fun openDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
    }*/


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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.loadDirectory(directoryUri)
    }

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FilesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FilesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
    companion object {

        /**
         * Convenience method for constructing a [DirectoryFragment] with the directory uri
         * to display.
         */
        @JvmStatic
        fun newInstance(directoryUri: Uri) =
            FilesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIRECTORY_URI, directoryUri.toString())
                }
            }
    }
}
