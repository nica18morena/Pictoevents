package com.example.pictoevents.UI

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pictoevents.R
import com.example.pictoevents.Util.FileManager
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val OPEN_DIRECTORY_REQUEST_CODE = 0xf11e
private const val ARG_DIRECTORY_URI = "com.example.pictoevents.ARG_DIRECTORY_URI"

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
    private lateinit var viewModel: FilesFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var directoryUriTest2 = this.requireContext().externalMediaDirs

        directoryUri = DocumentFile.fromFile(directoryUriTest2[0]).uri

        viewModel = ViewModelProviders.of(this)
            .get(FilesFragmentViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_files, container, false)
        recyclerView = view.findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)

        adapter = DirectoryEntryAdapter(object : ClickListeners {
            override fun onDocumentClicked(clickedDocument: File) {
                viewModel.documentClicked(clickedDocument)
            }

            override fun onDocumentLongClicked(clickedDocument: File) {
            }
        })

        recyclerView.adapter = adapter

        viewModel.documents.observe(this, Observer { documents ->
            documents?.let { adapter.setEntries(documents) }
        })

        viewModel.openDocument.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { document ->
                openDocument(document)
            }
        })

        return view
    }

    fun getImageContentUri(imageFile: File): Uri? {
        var context = this.requireContext()
        val filePath = imageFile.absolutePath
        val cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(
                cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID)
            )
            val baseUri =
                Uri.parse("content://media/external/images/media")
            Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }
    private fun openDocument(document: File) {
        var documentfile = getImageContentUri(document)
        try {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                //data = documentfile
                if (document.toUri().toString().contains(".png"))
                {
                    setDataAndType(documentfile, "image/png")
                }
                else if (document.toUri().toString().contains(".txt"))
                {
                    setDataAndType(documentfile, "text/plain")
                }
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
    private fun openDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.loadDirectory()//(directoryUri)
    }

    companion object {

        fun newInstance(directoryUri: Uri) =
            FilesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DIRECTORY_URI, directoryUri.toString())
                }
            }
    }
}
