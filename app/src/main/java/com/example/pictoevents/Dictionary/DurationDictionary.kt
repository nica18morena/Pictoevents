package com.example.pictoevents.Dictionary

class DurationDictionary {
    private val durationSet = HashSet<String>()

    private fun initialize(){
        durationSet.add("second")
        durationSet.add("sec")
        durationSet.add("hour")
        durationSet.add("hr")
        durationSet.add("a m ")
        durationSet.add("midnight")
        durationSet.add("month")
        durationSet.add("mo")
        durationSet.add("mos")
        durationSet.add("century")
        durationSet.add("cent")
        durationSet.add("minute")
        durationSet.add("min")
        durationSet.add("week")
        durationSet.add("wk")
        durationSet.add("wks")
        durationSet.add("noon")
        durationSet.add("year")
        durationSet.add("yr")
    }

    fun getMonthDict(): HashSet<String>{
        this.initialize()
        return durationSet
    }
}