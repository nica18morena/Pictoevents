package com.example.pictoevents.UI

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.core.impl.PreviewConfig
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import com.example.pictoevents.Calendar.CalendarObject
import com.example.pictoevents.Calendar.CalendarObjectsGenerator
import com.example.pictoevents.Calendar.PictoCalendar
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.R
import com.example.pictoevents.Util.FileManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private val TAG: String? = CameraFragment::class.java.simpleName
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA, Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR, Manifest.permission.INTERNET)
private val OCREngine: IOCREngine = OCREngineFreeOCR()
private val cloudStorage = Firebase.storage
/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CameraFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment_OG : Fragment() {

  /* start of comment out
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val camViewFinder = view.findViewById<TextureView>(R.id.view_finder)


        camViewFinder.post {

            startCamera(camViewFinder, view) }

        camViewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform(camViewFinder)
        }
    }
    private fun startCamera(viewFinder: TextureView, view: View) {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform(viewFinder)
        }
        Log.d(TAG,"Viewfinder use case created")
        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)
        view.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            val fileBase = File(this.requireContext().externalMediaDirs.first(), "/")
            FileManager.setFileBase(fileBase)
            FileManager.prepareDirectory(
                FileManager.getFileBase())
            val imageName = "${System.currentTimeMillis()}.jpg"
            FileManager.setFileName(imageName)
            val imageFile = File(FileManager.getFileBase(), "/$imageName")

            imageCapture.takePicture(imageFile, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        exc: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Log.e("TAG", msg, exc)
                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("TAG", msg)

                        OCREngine.setImageFileLocation(file)
                        uploadFileToStorage(file)
                        processOCR()
                        createCalEvent()
                    }
                })
        }
        Log.d(TAG,"Image capture use case completed")
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        executor.shutdown()
    }
    private fun updateTransform(viewFinder : TextureView) {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    private fun uploadFileToStorage(file: File)
    {
        val cloudStorageRef = cloudStorage.reference.child("images/${FileManager.getFileName()}")
        val uploadTask = cloudStorageRef.putFile(Uri.fromFile(file))

        uploadTask.addOnFailureListener{
            Log.e(TAG, "File did not upload successfully: $it")
        }.addOnSuccessListener{
            Log.d(TAG, "File uploaded to cloud successfully")
            cloudStorageRef.downloadUrl.addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val downloadUri = task.result
                    FileManager.setCloudURL(downloadUri)
                }
            }
        }
    }

    private fun processOCR() {
        //Setup file directory and context for OCR
        val dataPath = File(this.requireContext().externalMediaDirs.first(), "/tessdata")
        FileManager.setDataPath(dataPath.toString())
        FileManager.prepareDirectory(
            FileManager.getDataPath())
        OCREngine.setContext(this.requireContext())

        OCREngine.prepareOCREngine()

        //process OCR- need bitmap...TODO: Bitmap is empty rn- need to properly create it
        val bitmap = BitmapFactory.decodeFile(OCREngine.getImageFileLocation().toString())
        //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, bitmapUri)
        val ocrText = OCREngine.extractText(bitmap)
        Log.d(TAG, "OCR text is: $ocrText")
    }

    fun createCalEvent()
    {
        val text = OCREngine.getOCRResults() // Get OCR text
        val generateCalendarObjects = CalendarObjectsGenerator(text)
        generateCalendarObjects.identifyCalendarComponents() // identify from text all relevant components

        val calendar = PictoCalendar(this.requireContext())// Instance of PictoCalander to get the calendar ID
        calendar.checkCalendars()// This finds all calendars and assigned the calendarID to the Picto cal

        val formatter = generateCalendarObjects.getObjectFormatter() // Get the formatter to format all the data
        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId())

        calendar.setCalObj(calObject)
        calendar.buildCalEvent()
    }

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/ */ //End of comment out
}
