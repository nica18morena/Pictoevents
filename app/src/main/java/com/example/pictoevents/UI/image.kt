package com.example.pictoevents.UI

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.example.pictoevents.Processor.TextProcessor
import com.example.pictoevents.R
import com.example.pictoevents.Util.FileManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [image.newInstance] factory method to
 * create an instance of this fragment.
 */
class image : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set the image
        var imageFile  = FileManager.getImageFileLocation()
        var bitmap = BitmapFactory.decodeFile(imageFile.toString())
        var imageView = view.findViewById<ImageView>(R.id.imageView2)
        imageView?.setImageBitmap(bitmap)
        //Add buttons and listener actions
        view.findViewById<Button>(R.id.retake_button).setOnClickListener {
            findNavController().navigate(R.id.action_image_frag_to_cameraFragment)
        }
        view.findViewById<Button>(R.id.image_ok_button).setOnClickListener {
            findNavController().navigate(R.id.action_image_frag_to_progressFragment)
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment image.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            image().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}