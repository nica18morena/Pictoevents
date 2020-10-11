package com.example.pictoevents.NaturalLangProc

import android.util.Log
import com.google.cloud.language.v1.LanguageServiceSettings
import com.google.cloud.language.v1.stub.LanguageServiceStub
import com.google.cloud.language.v1beta2.AnalyzeEntitiesRequest
import com.google.cloud.language.v1beta2.Document
import com.google.cloud.language.v1beta2.EncodingType
import com.google.cloud.language.v1beta2.LanguageServiceClient

class AnalyzeEntities {

    private val TAG: String = AnalyzeEntities::class.java.getSimpleName()
    // Create instance of Language client
    fun buildRequest(text : String) : AnalyzeEntitiesRequest
    {
        // Instantiate the Language client com.google.cloud.language.v2.LanguageServiceClient
        val doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build()

        return AnalyzeEntitiesRequest.newBuilder()
            .setDocument(doc)
            .setEncodingType(EncodingType.UTF16)
            .build()
    }

    // parse response
    fun parseResponse(request : AnalyzeEntitiesRequest)
    {
        var settingsBuilder : LanguageServiceSettings.Builder = LanguageServiceSettings.newBuilder()

        try{
            LanguageServiceClient.create().use { language ->
                val response = language.analyzeEntities(request)
                for (entity in response.entitiesList) {
                    Log.d(TAG, "Entity: ${entity.name}")
                    Log.d(TAG,"Salience: ${entity.salience}")
                    Log.d(TAG, "Metadata: ")
                    for ((key, value) in entity.metadataMap
                        .entries) {
                        Log.d(TAG, "$key, $value")
                    }
                    for (mention in entity.mentionsList) {
                        Log.d(TAG,
                            "Begin offset: ${mention.text.beginOffset}")
                        Log.d(TAG, "Content: ${mention.text.content}\n")
                        Log.d(TAG,"Type: ${mention.type}\n")
                    }
                }
            }
        }
        catch (e:Exception){
            Log.d(TAG, "AnalyzeEntities: Error $e")
        }
    }
    // Organize to create Title
}