package com.example.pictoevents.Dictionary

class MonthDictionary {
    private val monthSet = HashSet<String>()

    private fun initialize(){
        monthSet.add("january")
        monthSet.add("february")
        monthSet.add("march")
        monthSet.add("april")
        monthSet.add("may")
        monthSet.add("june")
        monthSet.add("july")
        monthSet.add("august")
        monthSet.add("september")
        monthSet.add("october")
        monthSet.add("november")
        monthSet.add("december")
        monthSet.add("jan")
        monthSet.add("feb")
        monthSet.add("mar")
        monthSet.add("apr")
        monthSet.add("may")
        monthSet.add("jun")
        monthSet.add("jul")
        monthSet.add("aug")
        monthSet.add("sep")
        monthSet.add("oct")
        monthSet.add("nov")
        monthSet.add("dec")
        monthSet.add("sept")
    }

    fun getMonthDict(): HashSet<String>{
        this.initialize()
        return monthSet
    }
}