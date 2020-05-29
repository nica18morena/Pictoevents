package com.example.pictoevents

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.core.impl.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.pictoevents.Calendar.CalendarObject
import com.example.pictoevents.Calendar.CalendarObjectsGenerator
import com.example.pictoevents.Calendar.PictoCalendar
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.Util.FileManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.concurrent.Executors

// Request permission
private const val REQUEST_CODE_PERMISSIONS = 10
// Array of all permissions specified in the manifest file
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR, Manifest.permission.INTERNET)
//Class variables
private val TAG: String? = MainActivity::class.java.simpleName
//private val file = File(Environment.getExternalStorageDirectory().toString() + "/Pictoevents_${System.currentTimeMillis()}.jpg")
//private val fileBase = File(Environment.getExternalStorageDirectory().toString() + "/Pictoevents/")
//private val OCREngine: IOCREngine = OCREngineTesseract()
private val OCREngine: IOCREngine = OCREngineFreeOCR()
//private val fileManager = FileManager()
//private const val storageBucket = "gs://pictoevents"
private val cloudStorage = Firebase.storage
//private lateinit var appBarConfig : AppBarConfiguration

class MainActivity : AppCompatActivity(), LifecycleOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create NavHostFragment
        val host : NavHostFragment = supportFragmentManager.findFragmentById(R.id.host_fragment)
                as NavHostFragment? ?: return

        val navController = host.navController
        //appBarConfig = AppBarConfiguration(navController.graph)
        setupBottomNavMenu(navController)

        //Create actionbar
        //var toolbar: ActionBar = ActionBar

        //Create calendar
        /*val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val selectedDate = calendarView.getDate()

        calendarView.setOnDateChangeListener{view, year, month, dayOfMonth ->
            val msg = "Date changed"
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }*/


        //Line 53 -69 is camera related. can comment out for now to get the UI going
        /*viewFinder = findViewById(R.id.view_finder)

        *///Request  permissions
        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
       /* viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
        Log.d(TAG,"Camera created")*/
        createDirectory()
    }
    private fun setupBottomNavMenu(navController : NavController){
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.setupWithNavController(navController)
    }
    /* //Old camera related code comment out starts here
    // Add this after onCreate
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera() {
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
            updateTransform()
        }
        Log.d(TAG,"Viewfinder use case created")
        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        // Build the image capture use case and attach button click listener
        // This is commented out only to test updating the initial UI
        /*val imageCapture = ImageCapture(imageCaptureConfig)
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            /*val file = File(externalMediaDirs.first(),
                "${System.currentTimeMillis()}.jpg")*/
            val fileBase = File(externalMediaDirs.first(), "/")
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
                        Log.e("CameraXApp", msg, exc)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("CameraXApp", msg)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                        // Once the image is saved, set the image location so it can be found by
                        // the OCR engine
                        OCREngine.setImageFileLocation(file)
                        uploadFileToStorage(file)
                        processOCR()
                        createCalEvent()
                    }
                })
        }
        Log.d(TAG,"Image capture use case completed")
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)*/
    }

    private fun updateTransform() {
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
*/ //This is to unblock the permisisons code
    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun createDirectory()
    {
        val fileBase = File(this.externalMediaDirs.first(), "/")
        FileManager.setFileBase(fileBase)
        FileManager.prepareDirectory(
            FileManager.getFileBase())
    }
    /* //This is to re-block the remaining camera code

    /** Create an instance of the OCR engine and process OCR*/
    private fun processOCR() {
        //Setup file directory and context for OCR
        val dataPath = File(externalMediaDirs.first(), "/tessdata")
        FileManager.setDataPath(dataPath.toString())
        FileManager.prepareDirectory(
            FileManager.getDataPath())
        OCREngine.setContext(this)

        //retrieve the image
        //val bitmapUri = OCREngine.getCaptureImageOutputUri()
        OCREngine.prepareOCREngine()

        //process OCR- need bitmap...TODO: Bitmap is empty rn- need to properly create it
        val bitmap = BitmapFactory.decodeFile(OCREngine.getImageFileLocation().toString())
        //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, bitmapUri)
        val ocrText = OCREngine.extractText(bitmap)
        Log.d(TAG, "OCR text is: $ocrText")
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

    fun createCalEvent()
    {
        val text = OCREngine.getOCRResults() // Get OCR text
        val generateCalendarObjects = CalendarObjectsGenerator(text)
        generateCalendarObjects.identifyCalendarComponents() // identify from text all relevant components

        val calendar = PictoCalendar(this)// Instance of PictoCalander to get the calendar ID
        calendar.checkCalendars()// This finds all calendars and assigned the calendarID to the Picto cal

        val formatter = generateCalendarObjects.getObjectFormatter() // Get the formatter to format all the data
        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId())
        // TODO: Create the cal event
        calendar.setCalObj(calObject)
        calendar.buildCalEvent()
    }*/ //End comment out of old camera stuff
}
