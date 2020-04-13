package com.example.pictoevents.Camera

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import com.example.pictoevents.Calendar.CalendarObject
import com.example.pictoevents.Calendar.CalendarObjectsGenerator
import com.example.pictoevents.Calendar.PictoCalendar
import com.example.pictoevents.Util.FileManager
import java.io.File

class Camera : Application(), CameraXConfig.Provider {
    private val TAG: String = Camera::class.java.getSimpleName()

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
        val imageCapture = ImageCapture(imageCaptureConfig)
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
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform()

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
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
    }
}
