package com.example.pictoevents.OCREngine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.annotation.NonNull
import java.io.File

interface IOCREngine{

    //var imageFile: File
    /**
     * Prepare the OCR engine by configuring settings
     */
    fun prepareOCREngine(_bitmap: Bitmap): String{
        return "This needs to be overridden"
    }

    /**
     * Extract text from the given input
     */
    fun extractText(_bitmap: Bitmap): String
    fun prepareOCREngine()

    /**
     * Get the image URI
     */
    fun getCaptureImageOutputUri(): Uri?

    fun setContext(_context: Context)

    fun getOCRResults(): String
}