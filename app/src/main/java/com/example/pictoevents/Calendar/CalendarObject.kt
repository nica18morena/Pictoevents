package com.example.pictoevents.Calendar

class CalendarObject(val hour: Int, val minute: Int, val second: Int, val dayOfMonth: Int,
                     val month: Int, val year: Int, val AmPm: Int, val calendarID: Long) {

    var title: String = ""

    private var eventLocation: String
        get() = this.eventLocation
        set(value){
            this.eventLocation = value
        }

    private var description: String
        get() = this.description
        set(value){
            this.description = value
        }
}