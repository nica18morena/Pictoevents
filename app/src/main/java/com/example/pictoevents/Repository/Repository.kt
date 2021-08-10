package com.example.pictoevents.Repository

import com.example.pictoevents.calendar.CalendarObject
import java.util.*

object Repository {
    var eventTitle = ""
    var text = ""
    var eventCreationCompletedSuccessfully = false
    var manuallyCreatedEvent = false
    var automaticallyCreatedEvent = false
    var manualText = ""
    lateinit var identifiedPictoCalValueCalendar: Calendar
    var shouldSetCreatedDate = false
    lateinit var calendarObject : CalendarObject //From auto event
    var transitionalWorkDone = false
}