package com.example.pictoevents.OCREngine

//import okhttp3.MediaType
//import okhttp3.OkHttpClient
//import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
//import jdk.nashorn.internal.objects.NativeObject.keys
//import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
//import sun.text.normalizer.UTF16.append

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.pictoevents.Util.FileManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.fuel.util.encodeBase64
import okio.internal.commonToUtf8String
import org.json.JSONObject
import java.io.ByteArrayOutputStream
//import org.junit.runner.Request.method
//import java.awt.PageAttributes.MediaType
import java.io.File
import java.net.URL
import java.util.*
import kotlin.math.round

//import javax.swing.text.html.HTML.Tag.FORM


class OCREngineFreeOCR : IOCREngine
{
    private val TAG: String = OCREngineFreeOCR::class.java.getSimpleName()
    private val LANG = "eng"
    private val JPG = "jpg"
    private val OCRAPIKEY = "858bc9797c88957"
    private val OCRENDPOINT = "https://api.ocr.space/parse/image"
    private lateinit var activityContext: Context
    private lateinit var imageFile: File
    private lateinit var ocrText: String
    private lateinit var URL: URL

    override fun setContext(_context: Context)
    {
        this.activityContext = _context
    }

    override fun getCaptureImageOutputUri(): Uri?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun prepareOCREngine2()
    {
        URL = URL(OCRENDPOINT)
    }

    override fun prepareOCREngine()
    {
        URL = URL(OCRENDPOINT)
        Log.d(TAG, "OcrEngine prepped")
    }

    fun createJsonBody(): String
    {
        val JSONObj = JSONObject()
        JSONObj.put("apikey", OCRAPIKEY)
        JSONObj.put("filetype", JPG )
        JSONObj.put("language", LANG)
        JSONObj.put("detectOrientation", true)
        JSONObj.put("OCREngine", 2)

        return JSONObj.toString()
    }

/*    fun HttpsURLConnection.extractText2()
    {
        BufferedReader(InputStreamReader(this.getInputStream())).use {
            val response = StringBuffer()
            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            setOCRResults(response.toString())
        }
    }*/

    private fun resizeImage(bitmap : Bitmap) : Bitmap
    {
        val MAXBYTES = 1000000
        var bitWidth = bitmap.width.toDouble()
        var bitHeight = bitmap.height.toDouble()
        var totalBytes = (bitWidth * bitHeight) * 4
        var delta = 0.00
        var scaleFactor = 1.00
        var bytecount = bitmap.byteCount

        Log.d(TAG, "Bitmap \"total bytes\" $totalBytes, bh: $bitHeight, bw: $bitWidth, bytecount: $bytecount")
        while(totalBytes > MAXBYTES)
        {
            delta = totalBytes - MAXBYTES
            scaleFactor = delta / totalBytes
            bitWidth = bitWidth - (bitWidth * scaleFactor)
            bitHeight = bitHeight - (bitHeight * scaleFactor)
            totalBytes = (bitWidth * bitHeight) * 4

            Log.d(TAG, "Modified \"total bytes\" $totalBytes, bh: $bitHeight, bw: $bitWidth")
        }

        return Bitmap.createScaledBitmap(bitmap, bitWidth.toInt(), bitHeight.toInt(), false)
    }
    private fun convertImageToBase64(path: File?): String
    {
        if( path != null)
        {
            val bytes = path.readBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.lastIndex)
            Log.d(TAG, "Image size ${bytes.size} \n " +
                    "Image h: ${bitmap.height} Image w: ${bitmap.width}")
            //val bitmap = Bitmap.createScaledBitmap(b, 720, 1080, false)
            //val bitmap = this.resizeImage(b)
            val stream = ByteArrayOutputStream()
            val quality = this.calculateImageFactor(bytes.size)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            val bytes2 = stream.toByteArray()
            Log.d(TAG, "After resize image size ${bytes2.size} \n " +
                    "Image h: ${bitmap.height} Image w: ${bitmap.width}")
            val base64 = Base64.getEncoder().encodeToString(bytes2)

            return base64
        }
        return "Image is null"
    }

    private fun calculateImageFactor(imageSize: Int): Int
    {
        val MAXSIZE = 1000000//1MB
        var percentDelta = 100.00
        if (imageSize > MAXSIZE){
            val sizeDelta = imageSize - MAXSIZE
            val scale = sizeDelta.toDouble()/ imageSize.toDouble()
            percentDelta = (1 - scale) * 100
        }

        return percentDelta.toInt()
    }

    override fun extractText(_bitmap: Bitmap): String
    {
       /* //This version is using File type to send over to OCR
        val response = Fuel.post(URL.toString(), listOf("apikey" to OCRAPIKEY,

                                                        "filetype" to JPG,
                                                        "language" to LANG,
                                                        "detectOrientation" to true,
                                                        "OCREngine" to "2",
                                                        "file" to "/"+ getImageFileLocation().toString()))
            //.header(Headers.CONTENT_TYPE, "multipart/form-data")
            //.jsonBody(createJsonBody())
            .upload()
            .add{ FileDataPart(getImageFileLocation(),contentType = "application/octet-stream") }
            .responseJson()*/

        val image = this.convertImageToBase64(getImageFileLocation())
        val imageURL = FileManager.getCloudImageURL().toString()
        Log.d(TAG, "Starting API request")
        FuelManager.instance.timeoutInMillisecond = 15000 //Default 10 sec
        FuelManager.instance.timeoutReadInMillisecond = 15000 //Default 10 sec
        val response = Fuel.post(URL.toString(), listOf("apikey" to OCRAPIKEY,
            //"filetype" to JPG,
            "language" to LANG,
            "detectOrientation" to true,
            "OCREngine" to "2",
            "scale" to true,
            // "URL Hardcoded:"//"url" to "https://firebasestorage.googleapis.com/v0/b/pictoevents-1a825.appspot.com/o/paymentsOrtho11-26.PNG?alt=media&token=20e7c49b-7346-4685-a8cb-0620f953c2f7"))
            // "URL with Image://"url" to imageURL))
            "base64Image" to "data:image/$JPG;base64,$image"))
            .responseJson()
        Log.d(TAG, "OcrEngine: Response sent")
        val (resdata, reserror) = response.third

        val extractedText : String
        if(resdata != null)
        {
            val test = resdata.obj()
            val test2 = test.optJSONArray("ParsedResults")

            if (test2 != null)
            {
                val test4 = test2.getJSONObject(0)
                val test5 = test4.optString("ParsedText")

                extractedText = resdata.obj()
                    .optJSONArray("ParsedResults")
                    .getJSONObject(0)
                    .optString("ParsedText")

                Log.d(TAG, "OcrEngine: Success $extractedText")
            }
            else
            {
                extractedText = resdata.obj()
                    .optString("ErrorDetails")
                Log.d(TAG, "OcrEngine: Error $extractedText")
            }
        }
        else
        {
            extractedText = reserror.toString()
            Log.d(TAG, "OcrEngine: Error $extractedText")
        }
        this.setOCRResults(extractedText)
        return extractedText
    }

 /*   fun extractText2(_bitmap: Bitmap): String {

        //val base64Image: String  = convertImageToBase64(getImageFileLocation())
        with(URL.openConnection() as HttpsURLConnection)
        {
            this.requestMethod = "POST"
            this.setRequestProperty("User-Agent", "Mozilla/5.0")
            this.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
            this.setRequestProperty("Content-Type", "multipart/form-data")
            val JSONObj: JSONObject = JSONObject()
            JSONObj.put("apikey", OCRAPIKEY)
            // Open file
            getImageFileLocation()?.inputStream().use{
                val bytes = it?.readBytes()
                JSONObj.put("file", bytes)
            }

//            JSONObj.put("base64image", base64Image)
            JSONObj.put("language", LANG)
            JSONObj.put("detectOrientation", true)
            JSONObj.put("OCREngine", 2)

            this.doOutput = true
            val wr = DataOutputStream(this.getOutputStream())
            wr.writeBytes(extractTextFromJSON(JSONObj))
            wr.flush()
            wr.close()

            extractText()
        }
        return getOCRResults()
    }*/
  /*  fun extractTextFromJSON(params: JSONObject): String
    {
        val result = StringBuilder()
        var first = true

        val itr: Iterator<String> = params.keys()

        while (itr.hasNext()) {
            val key = itr.next()
            val value: Any = params.get(key)
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }*/

    override fun setImageFileLocation(_imageLocation: File)
    {
        imageFile = _imageLocation
    }
    override fun getImageFileLocation(): File
    {
        return imageFile
    }
    fun setOCRResults(ocrText: String)
    {
        this.ocrText = ocrText
    }
    override fun getOCRResults() : String
    {
        return this.ocrText
    }

    fun resizeImage2(path: File?):Bitmap
    {
        val bitOps = BitmapFactory.Options()
        var bitmap = BitmapFactory.decodeFile(path.toString(), bitOps)
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        val size = bitmap.byteCount
        return bitmap
    }

    private fun lessResolution (filePath : String, width : Int, height : Int) : Bitmap
    {
    val reqHeight = height
    val reqWidth = width
    val options = BitmapFactory.Options()

    // First decode with inJustDecodeBounds=true to check dimensions
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(filePath, options)
}

    private fun calculateInSampleSize(options : BitmapFactory.Options, reqWidth : Int, reqHeight : Int) :Int
    {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth)
        {
            // Calculate ratios of height and width to requested height and width
            val heightRatio = round((height.toFloat() / reqHeight.toFloat()))
            val widthRatio = round((width.toFloat() / reqWidth.toFloat()))

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio.toInt() else widthRatio.toInt()
        }
        return inSampleSize;
    }
}