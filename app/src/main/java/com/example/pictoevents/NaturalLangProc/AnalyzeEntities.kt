package com.example.pictoevents.NaturalLangProc

import com.google.cloud.language.v1beta2.AnalyzeEntitiesRequest
import com.google.cloud.language.v1beta2.Document
import com.google.cloud.language.v1beta2.EncodingType
import com.google.cloud.language.v1beta2.LanguageServiceClient

class AnalyzeEntities {

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
        LanguageServiceClient.create().use { language ->
            val response = language.analyzeEntities(request)
            for (entity in response.entitiesList) {
                System.out.printf("Entity: %s", entity.name)
                System.out.printf("Salience: %.3f\n", entity.salience)
                println("Metadata: ")
                for ((key, value) in entity.metadataMap
                    .entries) {
                    System.out.printf("%s : %s", key, value)
                }
                for (mention in entity.mentionsList) {
                    System.out.printf(
                        "Begin offset: %d\n",
                        mention.text.beginOffset
                    )
                    System.out.printf("Content: %s\n", mention.text.content)
                    System.out.printf("Type: %s\n\n", mention.type)
                }
            }
        }
    }
    // Organize to create Title
}