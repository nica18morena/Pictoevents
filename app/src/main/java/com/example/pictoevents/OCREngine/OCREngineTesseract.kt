package com.example.pictoevents.OCREngine
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.pictoevents.Util.FileManager
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class OCREngineTesseract : IOCREngine {
    private val TAG: String = OCREngineTesseract::class.java.getSimpleName()
    private val LANG = "eng"
    private val TESSDATA = "tessdata"
    private lateinit var activityContext: Context
    private lateinit var tessFile : String
    var imageFile: File? = null

    override fun extractText(_bitmap: Bitmap): String {
        //super.extractText()
        val tessBaseAPI = TessBaseAPI()

        tessBaseAPI.init(FileManager.getFileBase().toString(), LANG)
        Log.d(TAG, "training file loaded")
        tessBaseAPI.setImage(_bitmap)
        var extractedText = "empty result"
        try {
            extractedText = tessBaseAPI.utF8Text
        } catch (e: Exception) {
            Log.e(TAG,"Error in recognizing text.")
        }
        tessBaseAPI.end()
        return extractedText
    }

    override fun prepareOCREngine() {
        try {
            FileManager.prepareDirectory(
                FileManager.getDataPath()
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        copyTessDataFiles()
    }

    private fun copyTessDataFiles() {
        try {
            val fileList = activityContext.assets.list(TESSDATA)
            for (fileName in fileList) {
                val pathToDataFile: String = FileManager.getDataPath() + "/" + fileName
                if (!File(pathToDataFile).exists()) {
                    val `in` =
                        activityContext.assets.open("$TESSDATA/$fileName")
                    val out: OutputStream = FileOutputStream(pathToDataFile)
                    setTessFilePath(pathToDataFile)
                    val buf = ByteArray(1024)
                    var len: Int
                    while (`in`.read(buf).also { len = it } > 0) {
                        out.write(buf, 0, len)
                    }
                    `in`.close()
                    out.close()
                    Log.d(TAG,"Copied $fileName to tessdata")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG,"Unable to copy files to tessdata $e")
        }
    }

    override fun getCaptureImageOutputUri(): Uri? {
        return Uri.fromFile(getImageFileLocation())
    }

    /**
     * Set image location
     */
    override fun setImageFileLocation(_imageLocation: File){
         imageFile = _imageLocation
     }
     /**
      * Get image location
      */
    override fun getImageFileLocation(): File?{
         return imageFile
     }

    override fun setContext(_context: Context) {
        activityContext = _context
    }

    fun setTessFilePath(_tessFilePath : String){
        tessFile = _tessFilePath
    }

    fun getTessFilePath(): String{
        return tessFile
    }

    override fun getOCRResults(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}