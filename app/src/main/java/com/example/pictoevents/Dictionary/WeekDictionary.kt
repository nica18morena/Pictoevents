package com.example.pictoevents.Dictionary

class WeekDictionary {
    private val weekSet = HashSet<String>()

    private fun initialize(){
        weekSet.add("monday")
        weekSet.add("tuesday")
        weekSet.add("wednesday")
        weekSet.add("thursday")
        weekSet.add("friday")
        weekSet.add("saturday")
        weekSet.add("sunday")
        weekSet.add("mon")
        weekSet.add("tue")
        weekSet.add("wed")
        weekSet.add("thu")
        weekSet.add("fri")
        weekSet.add("sat")
        weekSet.add("sun")
        weekSet.add("tu")
        weekSet.add("tues")
        weekSet.add("th")
        weekSet.add("thur")
        weekSet.add("thurs")
    }
    fun weekDict(): HashSet<String>{
        this.initialize()
        return weekSet
    }
}