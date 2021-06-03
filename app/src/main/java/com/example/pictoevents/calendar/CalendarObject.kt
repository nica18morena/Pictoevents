package com.example.pictoevents.calendar

class CalendarObject(val hour: Int, val minute: Int, val second: Int, val dayOfMonth: Int,
                     val month: Int, val year: Int, val AmPm: Int, val calendarID: Long, val title: String) {

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

    override fun toString(): String ="Event hr: ${hour}, min: ${minute}, dayOfmonth: ${dayOfMonth}," +
            " month: ${month}, year: ${year}, ampm: ${AmPm}, title: ${title}"
}