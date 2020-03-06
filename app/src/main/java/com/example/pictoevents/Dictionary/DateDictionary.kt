package com.example.pictoevents.Dictionary

class DateDictionary {
    private val dateSet = HashSet<String>()

    private fun initialize(){
        dateSet.add("1")
        dateSet.add("01")
        dateSet.add("2")
        dateSet.add("02")
        dateSet.add("3")
        dateSet.add("03")
        dateSet.add("4")
        dateSet.add("04")
        dateSet.add("5")
        dateSet.add("05")
        dateSet.add("6")
        dateSet.add("06")
        dateSet.add("7")
        dateSet.add("07")
        dateSet.add("8")
        dateSet.add("08")
        dateSet.add("9")
        dateSet.add("09")
        dateSet.add("10")
        dateSet.add("11")
        dateSet.add("12")
        dateSet.add("13")
        dateSet.add("14")
        dateSet.add("15")
        dateSet.add("16")
        dateSet.add("17")
        dateSet.add("18")
        dateSet.add("19")
        dateSet.add("20")
        dateSet.add("21")
        dateSet.add("22")
        dateSet.add("23")
        dateSet.add("24")
        dateSet.add("25")
        dateSet.add("26")
        dateSet.add("27")
        dateSet.add("28")
        dateSet.add("29")
        dateSet.add("30")
        dateSet.add("31")
    }

    fun getMonthDict(): HashSet<String>{
        this.initialize()
        return dateSet
    }
}