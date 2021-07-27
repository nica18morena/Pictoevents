package com.example.pictoevents.calendar

class CalendarObjectFormatter {
    // Raw values
    var yearFromDate = ""
    var monthFromDate = ""
    var dayFromDate = ""
    var hourFromTime = ""
    var minFromTime = ""
    var ampm = ""
    var monthName = ""
    var dayOfMonth = ""
    var fullYear = ""
    var weekdayName = ""
    var title = ""

    fun getFormattedMonth(): Int{
        var formattedMonth = "0"
        if(monthName.isNotEmpty()){
            when (monthName) {
                "january", "jan" -> formattedMonth = "01"
                "february", "feb" -> formattedMonth = "02"
                "march", "mar" -> formattedMonth = "03"
                "april", "apr" -> formattedMonth = "04"
                "may" -> formattedMonth = "05"
                "june", "jun" -> formattedMonth = "06"
                "july", "jul" -> formattedMonth = "07"
                "august", "aug" -> formattedMonth = "08"
                "september", "sep" -> formattedMonth = "09"
                "october", "oct" -> formattedMonth = "10"
                "november", "nov" -> formattedMonth = "11"
                "december", "dec" -> formattedMonth = "12"
            }
        }
        if(monthFromDate.isNotEmpty()){
            formattedMonth = monthFromDate
        }

        return Integer.parseInt(formattedMonth) //- 1 //Dont know/ remember why this is offset
    }

    fun getFormattedDay(): Int{
        var formattedDay = "0"
        if(dayOfMonth.isNotEmpty()){
            formattedDay = dayOfMonth.replace("\\D".toRegex(), "")
        }
        if(dayFromDate.isNotEmpty()){
            formattedDay = dayFromDate
        }
        if(dayOfMonth.isNotEmpty() && formattedDay != "0"){
            when (formattedDay) {
                "1" -> formattedDay = "01"
                "2" -> formattedDay = "02"
                "3" -> formattedDay = "03"
                "4" -> formattedDay = "04"
                "5" -> formattedDay = "05"
                "6" -> formattedDay = "06"
                "7" -> formattedDay = "07"
                "8" -> formattedDay = "08"
                "9" -> formattedDay = "09"
                else -> formattedDay = dayOfMonth
            }
        }

        return Integer.parseInt(formattedDay)
    }

    fun getFormattedYear(): Int{
        var formattedYear = "-1"
        if(yearFromDate.isNotEmpty()){
            if(yearFromDate.length == 2){
                formattedYear = "20" + yearFromDate
            }
            if(yearFromDate.length == 4){
                formattedYear = yearFromDate
            }
            return Integer.parseInt(formattedYear)
        }
        else if(fullYear.isNotEmpty()){
            formattedYear = fullYear
            return Integer.parseInt(formattedYear)
        }
        return 0
    }

    fun getFormattedHour(): Int{
        var formattedHour = 0
        if(hourFromTime.isNotEmpty()){
            if(ampm.isNotEmpty()){
                ampm = ampm.replace(".","")
                when (ampm){
                    "am" -> formattedHour = Integer.parseInt(hourFromTime)
                    "pm" -> if (Integer.parseInt(hourFromTime) == 12) {
                        formattedHour = Integer.parseInt(hourFromTime)
                    }else{
                        formattedHour = Integer.parseInt(hourFromTime) + 12
                    }
                    else -> formattedHour = Integer.parseInt(hourFromTime)
                }
            }
            else{
                formattedHour = Integer.parseInt(hourFromTime)
            }
        }
        return formattedHour
    }

    fun getFormattedMin(): Int{
        var formattedMin = "0"
        if(minFromTime.isNotEmpty()){
            formattedMin = minFromTime.replace(".","")
        }
        else{
            formattedMin = "00"
        }
        return Integer.parseInt(formattedMin)
    }

    fun getFormattedAMPM(): Int{
        var formattedAMPM = "0"
        if(ampm.isNotEmpty()){
            ampm = ampm.replace(".","")
            when(ampm){
                "am"-> formattedAMPM = "0"
                "pm"-> formattedAMPM = "1"
            }
        }
        return Integer.parseInt(formattedAMPM)
    }

    fun getFormattedTitle(): String{
        return title
    }
}