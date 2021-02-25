package com.example.pictoevents.UI

    import android.annotation.SuppressLint
    import android.content.BroadcastReceiver
    import android.content.Context
    import android.content.Intent
    import android.content.IntentFilter
    import android.content.res.Configuration
    import android.graphics.BitmapFactory
    //import android.graphics.Camera
    import android.graphics.Color
    import android.graphics.drawable.ColorDrawable
    import android.hardware.display.DisplayManager
    import android.media.MediaScannerConnection
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.util.DisplayMetrics
    import android.util.Log
    import android.view.*
    import android.webkit.MimeTypeMap
    import android.widget.ImageButton
    import androidx.camera.core.AspectRatio
    import androidx.camera.core.Camera
    import androidx.camera.core.CameraSelector
    import androidx.camera.core.ImageAnalysis
    import androidx.camera.core.ImageCapture
    import androidx.camera.core.ImageCapture.Metadata
    import androidx.camera.core.ImageCaptureException
    import androidx.camera.core.ImageProxy
    import androidx.camera.core.Preview
    import androidx.camera.lifecycle.ProcessCameraProvider
    import androidx.camera.view.PreviewView
    import androidx.constraintlayout.widget.ConstraintLayout
    import androidx.core.content.ContextCompat
    import androidx.core.net.toFile
    import androidx.core.view.setPadding
    import androidx.fragment.app.Fragment
    import androidx.lifecycle.lifecycleScope
    import androidx.localbroadcastmanager.content.LocalBroadcastManager
    import androidx.navigation.Navigation
    //import com.android.example.cameraxbasic.KEY_EVENT_ACTION
    //import com.android.example.cameraxbasic.KEY_EVENT_EXTRA
    //import com.android.example.cameraxbasic.MainActivity
    //import com.android.example.cameraxbasic.R
    //import com.android.example.cameraxbasic.utils.ANIMATION_FAST_MILLIS
    //import com.android.example.cameraxbasic.utils.ANIMATION_SLOW_MILLIS
    //import com.android.example.cameraxbasic.utils.simulateClick
    import com.bumptech.glide.Glide
    import com.bumptech.glide.request.RequestOptions
    import com.example.pictoevents.Calendar.CalendarObject
    import com.example.pictoevents.Calendar.CalendarObjectsGenerator
    import com.example.pictoevents.Calendar.PictoCalendar
    import com.example.pictoevents.MainActivity
    import com.example.pictoevents.OCREngine.IOCREngine
    import com.example.pictoevents.OCREngine.OCREngineFreeOCR
    import com.example.pictoevents.R
    import com.example.pictoevents.Util.FileManager
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.storage.ktx.storage
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.GlobalScope
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import java.io.File
    import java.nio.ByteBuffer
    import java.text.SimpleDateFormat
    import java.util.ArrayDeque
    import java.util.Locale
    import java.util.concurrent.ExecutorService
    import java.util.concurrent.Executors
    import kotlin.math.abs
    import kotlin.math.max
    import kotlin.math.min

    /**
     * Main fragment for this app. Implements all camera operations including:
     * - Viewfinder
     * - Photo taking
     * - Image analysis
     */
    class CameraFragment : Fragment() {

        private lateinit var container: ConstraintLayout
        private lateinit var viewFinder: PreviewView
        private lateinit var outputDirectory: File
        private lateinit var broadcastManager: LocalBroadcastManager
        private lateinit var captureButton: ImageButton
        private var displayId: Int = -1
        private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
        private var preview: Preview? = null
        private var imageCapture: ImageCapture? = null
        //private var imageAnalyzer: ImageAnalysis? = null
        private var camera: androidx.camera.core.Camera? = null
        private val OCREngine: IOCREngine = OCREngineFreeOCR()
        private val cloudStorage = Firebase.storage

        private val displayManager by lazy {
            requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        }

        /** Blocking camera operations are performed using this executor */
        private lateinit var cameraExecutor: ExecutorService

        /**
         * We need a display listener for orientation changes that do not trigger a configuration
         * change, for example if we choose to override config change in manifest or for 180-degree
         * orientation changes.
         */
        private val displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) = view?.let { view ->
                if (displayId == this@CameraFragment.displayId) {
                    Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                    imageCapture?.targetRotation = view.display.rotation
                    //imageAnalyzer?.targetRotation = view.display.rotation
                }
            } ?: Unit
        }

        override fun onDestroyView() {
            super.onDestroyView()

            // Shut down our background executor
            cameraExecutor.shutdown()

            // Unregister the and listeners
            displayManager.unregisterDisplayListener(displayListener)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_camera, container, false)

        @SuppressLint("MissingPermission")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            container = view as ConstraintLayout
            viewFinder = container.findViewById(R.id.view_finder)
            captureButton = container.findViewById(R.id.capture_button)

            // Initialize our background executor
            cameraExecutor = Executors.newSingleThreadExecutor()

            broadcastManager = LocalBroadcastManager.getInstance(view.context)

            // Every time the orientation of device changes, update rotation for use cases
            displayManager.registerDisplayListener(displayListener, null)

            // Wait for the views to be properly laid out
            viewFinder.post {

                // Keep track of the display in which this view is attached
                displayId = viewFinder.display.displayId

                // Build UI controls
                updateCameraUi()

                // Bind use cases
                bindCameraUseCases()
            }
        }

        /**
         * Inflate camera controls and update the UI manually upon config changes to avoid removing
         * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
         * transition on devices that support it.
         *
         * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
         * screen for devices that run Android 9 or below.
         */
        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            updateCameraUi()
        }

        /** Declare and bind preview, capture and analysis use cases */
        private fun bindCameraUseCases() {

            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

            val rotation = viewFinder.display.rotation

            // Bind the CameraProvider to the LifeCycleOwner
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener(Runnable {

                // CameraProvider
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                preview = Preview.Builder()
                    // We request aspect ratio but no resolution
                    .setTargetAspectRatio(screenAspectRatio)
                    // Set initial target rotation
                    .setTargetRotation(rotation)
                    .build()

                // ImageCapture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    // We request aspect ratio but no resolution to match preview config, but letting
                    // CameraX optimize for whatever specific resolution best fits our use cases
                    .setTargetAspectRatio(screenAspectRatio)
                    // Set initial target rotation, we will have to call this again if rotation changes
                    // during the lifecycle of this use case
                    .setTargetRotation(rotation)
                    .build()

                // Must unbind the use-cases before rebinding them
                cameraProvider.unbindAll()

                try {
                    // A variable number of use-cases can be passed here -
                    // camera provides access to CameraControl & CameraInfo
                    camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)

                    // Attach the viewfinder's surface provider to preview use case
                    preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
                } catch(exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(requireContext()))
        }

        /**
         *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
         *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
         *
         *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
         *  of preview ratio to one of the provided values.
         *
         *  @param width - preview width
         *  @param height - preview height
         *  @return suitable aspect ratio
         */
        private fun aspectRatio(width: Int, height: Int): Int {
            val previewRatio = max(width, height).toDouble() / min(width, height)
            if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
                return AspectRatio.RATIO_4_3
            }
            return AspectRatio.RATIO_16_9
        }

        /** Method used to re-draw the camera UI controls, called every time configuration changes. */
        private fun updateCameraUi() {

            // Remove previous UI if any
            /*container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
                container.removeView(it)
            }*/

            // Listener for button used to capture photo
            //controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
                captureButton.setOnClickListener{
                // Get a stable reference of the modifiable image capture use case
                imageCapture?.let { imageCapture ->

                    val fileBase = File(this.requireContext().externalMediaDirs.first(), "/")
                    FileManager.setFileBase(fileBase)
                    FileManager.prepareDirectory(
                        FileManager.getFileBase())
                    val imageName = "${System.currentTimeMillis()}.png"
                    FileManager.setFileName(imageName)
                    val photoFile = File(FileManager.getFileBase(), "/$imageName")

                    // Setup image capture metadata
                    val metadata = Metadata().apply {

                        // Mirror image when using the front camera
                        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                    }

                    // Create output options object which contains file + metadata
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                        .setMetadata(metadata)
                        .build()

                    // Setup image capture listener which is triggered after photo has been taken
                    imageCapture.takePicture(
                        outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                                Log.d(TAG, "Photo capture succeeded: $savedUri")

                                // If the folder selected is an external media directory, this is
                                // unnecessary but otherwise other apps will not be able to access our
                                // images unless we scan them using [MediaScannerConnection]
                                val mimeType = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                                MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(savedUri.toFile().absolutePath),
                                    arrayOf(mimeType)
                                ) { _, uri ->
                                    Log.d(TAG, "Image capture scanned into media store: $uri")
                                }

                                // Here start my custom code for OCR stuff
                                OCREngine.setImageFileLocation(photoFile)
                                //uploadFileToStorage(photoFile)- not needed for now
                                GlobalScope.launch(Dispatchers.Default){
                                    withContext(Dispatchers.Default) {processOCR()}
                                    withContext(Dispatchers.Default) {createCalEvent()}
                                }
                            }
                        })

                }
            }
        }

        private suspend fun processOCR() {
            //Check "this" info- may be the issue...
            //Setup file directory and context for OCR
            val dataPath = File(this.requireContext().externalMediaDirs.first(), "/tessdata")
            FileManager.setDataPath(dataPath.toString())
            FileManager.prepareDirectory(
                FileManager.getDataPath())

            OCREngine.setContext(this.requireContext())

            //retrieve the image
            //val bitmapUri = OCREngine.getCaptureImageOutputUri()
            OCREngine.prepareOCREngine()

            //process OCR- need bitmap...TODO: Bitmap is empty rn- need to properly create it
            val bitmap = BitmapFactory.decodeFile(OCREngine.getImageFileLocation().toString())
            //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, bitmapUri)
            val ocrText = OCREngine.extractText(bitmap)
            Log.d(TAG, "OCR text is: $ocrText")
        }
        /*  This method is currenlty not in use: not uploading image to cloud, but just makeing it base64
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
        }*/

        suspend fun createCalEvent()
        {
            //Sample temp text: Stephie and Jarrot wedding at 2:00 Pm, 9/19/2020
            //val text = "Stephie and Jarrot wedding at 2:30 Pm, 9/19/2020"
            val text = OCREngine.getOCRResults() // Get OCR text
            val generateCalendarObjects = CalendarObjectsGenerator(text)
            generateCalendarObjects.identifyCalendarComponents() // identify from text all relevant components

            val calendar = PictoCalendar(this.requireContext())// Instance of PictoCalander to get the calendar ID
            calendar.checkCalendars()// This finds all calendars and assigned the calendarID to the Picto cal

            val formatter = generateCalendarObjects.getObjectFormatter() // Get the formatter to format all the data
            val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
                formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
                formatter.getFormattedAMPM(), calendar.getCalId(), formatter.getFormattedTitle())
            Log.d(TAG, "Calendar object has values: ${calObject.toString()}")
            calendar.setCalObj(calObject)
            calendar.buildCalEvent()

            //Create txt file
            FileManager.createOCRTextFile(text)
        }

        companion object {

            private const val TAG = "CameraXBasic"
            private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
            private const val PHOTO_EXTENSION = ".jpg"
            private const val RATIO_4_3_VALUE = 4.0 / 3.0
            private const val RATIO_16_9_VALUE = 16.0 / 9.0

            /** Helper function used to create a timestamped file */
            private fun createFile(baseFolder: File, format: String, extension: String) =
                File(baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension)
        }
    }
