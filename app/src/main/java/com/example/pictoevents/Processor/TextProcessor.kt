package com.example.pictoevents.Processor

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.pictoevents.Calendar.CalendarObject
import com.example.pictoevents.Calendar.CalendarObjectsGenerator
import com.example.pictoevents.Calendar.PictoCalendar
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.Util.FileManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TextProcessor (val context: Context)
{
    private val OCREngine: IOCREngine = OCREngineFreeOCR()
    private val cloudStorage = Firebase.storage
    private lateinit var outputDirectory: File

    suspend fun processOCR() {

        //Setup file directory and context for OCR
        val dataPath = File(context.externalMediaDirs.first(), "/tessdata")
        FileManager.setDataPath(dataPath.toString())
        FileManager.prepareDirectory(
            FileManager.getDataPath())

        OCREngine.setContext(context)

        //retrieve the image
        //val bitmapUri = OCREngine.getCaptureImageOutputUri()
        OCREngine.prepareOCREngine()

        //process OCR- need bitmap
        val bitmap = BitmapFactory.decodeFile(FileManager.getImageFileLocation().toString())

        Repository.text = OCREngine.extractText(bitmap)
        saveTextFile()
        Log.d(TextProcessor.TAG, "OCR text is: ${Repository.text}")
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

    private suspend fun saveTextFile()
    {
        //Sample temp text: Stephie and Jarrot wedding at 2:00 Pm, 9/19/2020
        //val text = "Stephie and Jarrot wedding at 2:30 Pm, 9/19/2020"
        //Repository.text = OCREngine.getOCRResults() // Get OCR text

        //Create txt file
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) { FileManager.createOCRTextFile(Repository.text) }
        }
    }

    suspend fun createCalEvent()
    {
        val generateCalendarObjects = CalendarObjectsGenerator(Repository.text, context)
        //need to update generateCalendarObjects title formatter with the selected title
        generateCalendarObjects.setTitle(Repository.eventTitle)
        generateCalendarObjects.identifyCalendarComponents() // identify from text all relevant components except title

        val calendar = PictoCalendar(this.context)// Instance of PictoCalander to get the calendar ID
        calendar.checkCalendars()// This finds all calendars and assigned the calendarID to the Picto cal

        val formatter = generateCalendarObjects.getObjectFormatter() // Get the formatter to format all the data
        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId(), formatter.getFormattedTitle())
        Log.d(TextProcessor.TAG, "Calendar object has values: ${calObject.toString()}")
        calendar.setCalObj(calObject)
        calendar.buildCalEvent()
    }

    companion object
    {
        private const val TAG = "TextProcessor"
    }
}