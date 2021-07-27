package com.example.pictoevents.Processor

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.pictoevents.MainActivity
import com.example.pictoevents.calendar.*
import com.example.pictoevents.OCREngine.IOCREngine
import com.example.pictoevents.OCREngine.OCREngineFreeOCR
import com.example.pictoevents.Repository.Repository
import com.example.pictoevents.UI.TitleDialogFragment
import com.example.pictoevents.Util.FileManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

class TextProcessor (val context: Context)//Try the approach to pass in listener to constructor
{
    private val OCREngine: IOCREngine = OCREngineFreeOCR()
    private lateinit var outputDirectory: File
    internal lateinit var listener : TextProcessorListener

    interface TextProcessorListener{
        fun onEventCreatedComplete(successful : Boolean)
    }

    fun setTextProcessorListener(tplistener : TextProcessorListener){
        listener = tplistener
    }
    //suspend fun processOCR() {
    suspend fun processOCR() {

        //listener = TextProcessorListener

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
        /*Repository.text = "16920 Pacitic Ave S\n" +
                "    98387\n" +
                "    (253) 537-4356\n" +
                "    end 7/31/20\n" +
                "    Starter\n" +
                "    Annual Fecal Test Due 02/24/20\n" +
                "    Special Reminder For:\n" +
                "    Starter\n"*/

        val job = GlobalScope.launch(Dispatchers.IO){
            Repository.text = OCREngine.extractText(bitmap)//TODO: make this coroutine call

            saveTextFile()
        }

        job.join()

        val titleOptions = CalendarObjectTitle().generateTitles(context)
        loadTitleOptionsOntoDialog(titleOptions)
        Log.d(TAG, "========= 2 =========")
        GlobalScope.launch(Dispatchers.Default) {
            coroutineScope{
                launch(){

/*                    val job1 = launch{
                        val titleOptions = CalendarObjectTitle().generateTitles(context)
                        loadTitleOptionsOntoDialog(titleOptions)
                        Log.d(TAG, "========= 2 =========")
                    }*/

                    delay(3000)
                    val formatter = identifyCalEvent()// wait for this to return
                    Log.d(TAG, "========= 3 =========")

                    //job1.join()
                    /*Hack start*/
                    while(Repository.eventTitle == ""){
                        delay(1000)
                        Log.d(TAG, "Waiting for title")
                    }
                    /*Hack end*/
                    createCalEvent(formatter)
                    Log.d(TAG, "========= 4 =========")
                    //displaySnackbar()
                    //Log.d(TAG, "======== 5 ========")
                }
            }
        }
    }

    fun processManuallyAddedEvent(){
        GlobalScope.launch(Dispatchers.Main) {
            val textSplit = Repository.manualText.split(",")

            val date = textSplit[0]
            val time = textSplit[1]
            val ampm = textSplit[2]
            val calendarObjManual = CalendarObjectsManual(date, time, ampm)
            val formatter = calendarObjManual.formatCalendarComponents()
            //val formatter = identifyCalEvent()// wait for this to return
            Log.d(TAG, "========= 3 =========")
            createCalEvent(formatter)
            Log.d(TAG, "========= 4 =========")
        }
    }
    /*//Ref: https://kotlinlang.org/docs/coroutines-basics.html#scope-builder-and-concurrency
    fun executeOnOCR() = runBlocking{
        processForTitleAndRegex()
        Log.d(TAG,"Completed both title and regex OCR")
    }
    suspend processForTitleAndRegex() = coroutineScope{
        launch{
            val titleOptions = CalendarObjectTitle().generateTitles(context)
            loadTitleOptionsOntoDialog(titleOptions)
            Log.d(TAG, "========= 2 =========")
        }
        launch{
            val formatter = identifyCalEvent()// wait for this to return
            Log.d(TAG, "========= 3 =========")
            //join all coroutines then proceed to next step
            Log.d(TAG, "Formatter has: $formatter")
        }
    }*/
    private fun loadTitleOptionsOntoDialog(titleOptions: JSONObject)
    {
        Log.d(TAG, "++++++++ Start loadTitleOptionsOntoDialog() +++++++")
        (context as MainActivity).generateTitleDialog(titleOptions)
        //Present a dialog here
        //withContext(Dispatchers.Main){ (context as MainActivity).generateTitleDialog(titleOptions) }
    }
    /*  This method is currently not in use: not uploading image to cloud, but just makeing it base64
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

    //private suspend fun saveTextFile()
    private fun saveTextFile()
    {
        //Create txt file
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) { FileManager.createOCRTextFile(Repository.text) }
        }
    }

    suspend fun identifyCalEvent(): CalendarObjectFormatter{
        Log.d(TAG, "++++++++ Start identifyCalEvent() +++++++")
        val generateCalendarObjects = CalendarObjectsRegex(Repository.text)
        val formatter = generateCalendarObjects.identifyCalendarComponents()
        return formatter
    }

/*    fun identifyCalEvent_manuallyCreated(): CalendarObjectFormatter{
        return formatter
    }*/
    //suspend fun createCalEvent()
    fun createCalEvent(formatter: CalendarObjectFormatter)
    {
        val calendar = PictoCalendar(this.context)
        calendar.checkCalendars()

        val calObject = CalendarObject(formatter.getFormattedHour(),formatter.getFormattedMin(), 0,
            formatter.getFormattedDay(), formatter.getFormattedMonth(), formatter.getFormattedYear(),
            formatter.getFormattedAMPM(), calendar.getCalId(), Repository.eventTitle)
        Log.d(TextProcessor.TAG, "Calendar object has values: ${calObject.toString()}")

        calendar.setCalObj(calObject)
        var completed = calendar.buildCalEvent()
        listener.onEventCreatedComplete(completed)
    }

    companion object
    {
        private const val TAG = "TextProcessor"
    }
}