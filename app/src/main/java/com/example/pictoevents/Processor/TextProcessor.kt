package com.example.pictoevents.Processor

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.pictoevents.MainActivity
import com.example.pictoevents.calendar.*
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.Util.FileManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

class TextProcessor (val context: Context)
{
    private val textProcessorScope = MainScope()
    private val OCREngine: IOCREngine = OCREngineFreeOCR()
    private lateinit var outputDirectory: File
    internal lateinit var listener : TextProcessorListener

    interface TextProcessorListener{
        fun onEventCreatedComplete(successful : Boolean)
    }

    fun setTextProcessorListener(tplistener : TextProcessorListener){
        listener = tplistener
    }

    suspend fun processOCR() {

        Log.d(TAG, "++++++++ Start processOCR +++++++")
        //Setup file directory and context for OCR
        val dataPath = File(context.externalMediaDirs.first(), "/tessdata")
        FileManager.setDataPath(dataPath.toString())
        FileManager.prepareDirectory(
            FileManager.getDataPath())

        OCREngine.setContext(context)
        OCREngine.prepareOCREngine()

        //process OCR- need bitmap
        val bitmap = BitmapFactory.decodeFile(FileManager.getImageFileLocation().toString())

        //Testing: Sample temp text: Stephie and Jarrot wedding at 2:00 Pm, 9/19/2020
        //Repository.text = "Stephie and Jarrot wedding at 2:00 Pm, 9/19/2020"

        val job = textProcessorScope.launch(Dispatchers.IO){
            Repository.text = OCREngine.extractText(bitmap)

            saveTextFile()
        }

        job.join()

        val titleOptions = CalendarObjectTitle().generateTitles(context)
        loadTitleOptionsOntoDialog(titleOptions)
        Log.d(TAG, "========= 2 =========")

        textProcessorScope.launch(Dispatchers.Default) {
            coroutineScope{
                launch(){
                    delay(3000)
                    val formatter = identifyCalEvent()
                    Log.d(TAG, "========= 3 =========")

                    /*Hack start*/
                    while(Repository.eventTitle == ""){
                        delay(1000)
                        Log.d(TAG, "Waiting for title")
                    }
                    /*Hack end*/

                    createTransitionalCalEvent(formatter)
                    Log.d(TAG, "========= 4 =========")
                }
            }
        }
    }

    fun processManuallyAddedEvent(){
        textProcessorScope.launch(Dispatchers.Main) {
            val textSplit = Repository.manualText.split(",")

            val date = textSplit[0]
            val time = textSplit[1]
            val ampm = textSplit[2]
            val calendarObjManual = CalendarObjectsManual(date, time, ampm)
            val formatter = calendarObjManual.formatCalendarComponents()

            Log.d(TAG, "manual ========= 3 =========")
            createCalEvent(formatter)
            //createTransitionalCalEvent(formatter)
            Log.d(TAG, "manual ========= 4 =========")
        }
    }

    private fun loadTitleOptionsOntoDialog(titleOptions: JSONObject)
    {
        Log.d(TAG, "++++++++ Start loadTitleOptionsOntoDialog() +++++++")
        (context as MainActivity).generateTitleDialog(titleOptions)
    }
    /*  This method is currently not in use: not uploading image to cloud, but just making it base64
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

    private fun saveTextFile()
    {
        //Create txt file
        textProcessorScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) { FileManager.createOCRTextFile(Repository.text) }
        }
    }

    suspend fun identifyCalEvent(): CalendarObjectFormatter{
        Log.d(TAG, "++++++++ Start identifyCalEvent() +++++++")
        val generateCalendarObjects = CalendarObjectsRegex(Repository.text)
        val formatter = generateCalendarObjects.identifyCalendarComponents()
        return formatter
    }

    fun createCalEvent(formatter: CalendarObjectFormatter)
    {
        val calendar = PictoCalendar(this.context)
        calendar.checkCalendars()

        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId(), Repository.eventTitle)
        Log.d(TextProcessor.TAG, "Calendar object has values: ${calObject.toString()}")

        //Here add new transition
        Repository.calendarObject = calObject
        completeCalEvent(calendar, calObject)
    }

    fun completeCalEvent(
        calendar: PictoCalendar,
        calObject: CalendarObject
    ) {
        calendar.setCalObj(calObject)
        val completed = calendar.buildCalEvent()
        listener.onEventCreatedComplete(completed)
    }

    fun createTransitionalCalEvent(formatter: CalendarObjectFormatter)
    {
        val calendar = PictoCalendar(this.context)
        calendar.checkCalendars()

        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId(), Repository.eventTitle)
        Log.d(TextProcessor.TAG, "Transitional Calendar object has values: ${calObject.toString()}")

        Repository.calendarObject = calObject
        Repository.transitionalWorkDone = true
    }

    companion object
    {
        private const val TAG = "TextProcessor"
    }
}