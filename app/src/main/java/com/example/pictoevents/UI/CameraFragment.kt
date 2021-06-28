package com.example.pictoevents.UI

    import android.annotation.SuppressLint
    import android.content.Context
    import android.content.res.Configuration
    import android.graphics.Color
    import android.graphics.drawable.ColorDrawable
    //import android.graphics.Camera
    import android.hardware.display.DisplayManager
    import android.media.MediaScannerConnection
    import android.net.Uri
    import android.os.Bundle
    import android.util.DisplayMetrics
    import android.util.Log
    import android.view.*
    import android.webkit.MimeTypeMap
    import android.widget.ImageButton
    import android.widget.ProgressBar
    import androidx.camera.core.AspectRatio
    import androidx.camera.core.CameraSelector
    import androidx.camera.core.ImageCapture
    import androidx.camera.core.ImageCapture.Metadata
    import androidx.camera.core.ImageCaptureException
    import androidx.camera.core.Preview
    import androidx.camera.lifecycle.ProcessCameraProvider
    import androidx.camera.view.PreviewView
    import androidx.constraintlayout.widget.ConstraintLayout
    import androidx.core.content.ContextCompat
    import androidx.core.net.toFile
    import androidx.fragment.app.Fragment
    import androidx.localbroadcastmanager.content.LocalBroadcastManager
    import androidx.navigation.fragment.NavHostFragment.findNavController
    import androidx.navigation.fragment.findNavController
    import com.example.pictoevents.Processor.TextProcessor
    import com.example.pictoevents.R
    import com.example.pictoevents.Util.FileManager
    import kotlinx.coroutines.*
    import org.json.JSONObject
    import java.io.File
    import java.lang.Runnable
    import java.text.SimpleDateFormat
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
     */
    class CameraFragment : Fragment() {

        private lateinit var container: ConstraintLayout
        private lateinit var viewFinder: PreviewView
        private lateinit var broadcastManager: LocalBroadcastManager
        private lateinit var captureButton: ImageButton

        private var displayId: Int = -1
        private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
        private var preview: Preview? = null
        private var imageCapture: ImageCapture? = null
        private var camera: androidx.camera.core.Camera? = null
        private var cameraProvider: ProcessCameraProvider? = null
        /** Milliseconds used for UI animations */
        val ANIMATION_FAST_MILLIS = 50L
        val ANIMATION_SLOW_MILLIS = 100L

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

                // Setup camera
                setUpCamera()
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

        private fun setUpCamera() {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener(Runnable {

                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Select lensFacing depending on the available cameras
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    else -> throw IllegalStateException("Back camera is unavailable")
                }

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext()))
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

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

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

            //val textProcessor = TextProcessor(this.requireContext())
            // Listener for button used to capture photo
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

                                // Display flash animation to indicate that photo was captured
                                container.postDelayed({
                                    container.foreground = ColorDrawable(Color.WHITE)
                                    container.postDelayed(
                                        { container.foreground = null }, ANIMATION_FAST_MILLIS)
                                }, ANIMATION_SLOW_MILLIS)

                                // Here start my custom code for OCR stuff
                                FileManager.setImageFileLocation(photoFile)
                                //uploadFileToStorage(photoFile)- not needed for now

                                findNavController().navigate(R.id.action_cameraFragment_to_image3)
                            }
                        })
                }
            }
        }



        private fun hasBackCamera(): Boolean {
            return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
        }

        //suspend fun loadTitleOptionsOntoDialog()
        fun loadTitleOptionsOntoDialog(titleOptions: JSONObject)
        {
            Log.d(TAG, "++++++++ Start loadTitleOptionsOntoDialog() +++++++")
            //Present a dialog here
            GlobalScope.launch(Dispatchers.Main){
                withContext(Dispatchers.Main){ generateTitleDialog(titleOptions) }//get selected text
            }
        }

        private fun generateTitleDialog(titleOptions : JSONObject){
            val args = Bundle()
            args.putString("primary", titleOptions.optString("Primary"))
            args.putString("secondary", titleOptions.optString("Secondary"))
            Log.d(CameraFragment.TAG, "Title options are: ${titleOptions.optString("Primary")}," +
                    "${titleOptions.optString("Secondary")}")
            val dialog = TitleDialogFragment()
            dialog.arguments = args
            dialog.show(childFragmentManager, TitleDialogFragment.TAG)
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
