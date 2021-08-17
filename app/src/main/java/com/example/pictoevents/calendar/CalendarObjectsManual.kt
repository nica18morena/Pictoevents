package com.example.pictoevents.calendar

class CalendarObjectsManual (val date: String, val time: String, val ampm: String){
    private var formatter = CalendarObjectFormatter()

    fun formatCalendarComponents() : CalendarObjectFormatter{
        this.identifyDate()
        this.identifyTime()

        return formatter
    }

    private fun identifyDate(){
        val dateSplit = date.split("/")

        if (dateSplit.size == 3){
            formatter.monthFromDate = dateSplit[0]
            formatter.dayFromDate = dateSplit[1]
            formatter.yearFromDate = dateSplit[2]
        }

        formatter.ampm = ampm.trim()
    }

    private fun identifyTime(){
        val timeSplit = time.split(":")

        if (timeSplit.size == 2){
            formatter.hourFromTime = timeSplit[0]
            formatter.minFromTime = timeSplit[1]
        }
    }
}