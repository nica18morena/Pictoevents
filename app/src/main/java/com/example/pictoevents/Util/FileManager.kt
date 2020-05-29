package com.example.pictoevents.Util

import android.net.Uri
import android.util.Log
import java.io.File

object FileManager {
    private val TAG: String = FileManager::class.java.getSimpleName()
    private lateinit var fileBase : File
    private lateinit var dataPath : String
    private lateinit var fileName : String
    private var cloudURL : Uri? = null

    fun prepareDirectory(_path: String) {
        val dir = File(_path)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG,"ERROR: Creation of directory $_path failed, check Android Manifest")
            }
        } else {
            Log.i(TAG,"Created directory $_path")
        }
    }
    fun setCloudURL(imageURL: Uri?){
        cloudURL = imageURL
    }

    fun getCloudImageURL() : Uri?
    {
        return cloudURL
    }
    fun prepareDirectory(_path : File) {
        if (!_path.exists()) {
            if (!_path.mkdirs()) {
                Log.e(TAG,"ERROR: Creation of directory $_path failed, check Android Manifest")
            }
        } else {
            Log.i(TAG,"Created directory $_path")
        }
    }

   fun getFileBase() : File {
        return fileBase
    }

   fun setFileBase(_fileBase : File){
       fileBase = _fileBase
   }
    fun getDataPath() : String {
        return dataPath
    }

    fun setDataPath(_dataPath : String){
        dataPath = _dataPath
    }

    fun setFileName(fName: String){
        fileName = fName
    }

    fun getFileName() : String{
        return fileName
    }

    fun createOCRTextFile(ocrText: String) {

        val fileName = getFileName().replace(".png", ".txt")
        return File(getFileBase(), "/$fileName").writeText(ocrText)
    }
}