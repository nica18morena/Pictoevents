/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.pictoevents.UI

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pictoevents.Util.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel for the [FilesFragment].
 */
class FilesFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = FilesFragmentViewModel::class.java.simpleName
    private val _documents = MutableLiveData<List<File>>()
    val documents = _documents

    private val _openDirectory = MutableLiveData<Event<File>>()
    val openDirectory = _openDirectory

    private val _openDocument = MutableLiveData<Event<File>>()
    val openDocument = _openDocument

    fun loadDirectory(){//(directoryUri: Uri) {
        //val documentsTree = DocumentFile.fromTreeUri(getApplication(), directoryUri) ?: return
        //val childDocuments = documentsTree.listFiles(). .toCachingList()

        val childDocuments = FileManager.getFileBase().listFiles().toList()//toURI()//.context.fileList().toList()
        printFileNames(childDocuments)
        // It's much nicer when the documents are sorted by something, so we'll sort the documents
        // we got by name. Unfortunate there may be quite a few documents, and sorting can take
        // some time, so we'll take advantage of coroutines to take this work off the main thread.
        viewModelScope.launch {
            val sortedDocuments = withContext(Dispatchers.IO) {
                childDocuments.toMutableList().apply {
                    sortBy { it.length()}
                }
            }
            _documents.postValue(sortedDocuments)
        }
    }

    fun printFileNames(childDocuments : List<File>){
        childDocuments.forEach{item -> Log.d(TAG, "$item")}
    }
    /**
     * Method to dispatch between clicking on a document (which should be opened), and
     * a directory (which the user wants to navigate into).
     */
    fun documentClicked(clickedDocument: File) {
        if (clickedDocument.isDirectory) {
            openDirectory.postValue(Event(clickedDocument))
        } else {
            openDocument.postValue(Event(clickedDocument))
        }
    }
}