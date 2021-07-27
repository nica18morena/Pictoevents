package com.example.pictoevents.Repository

import java.util.*

object Repository {
    var eventTitle = ""
    var text = ""
    var eventCreationCompletedSuccessfully = false
    var manuallyCreatedEvent = false
    var manualText = ""
    lateinit var calendar: Calendar
    var isNavigationFromProgressFrag = false
}