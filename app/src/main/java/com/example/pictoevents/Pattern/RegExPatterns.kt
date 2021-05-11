package com.example.pictoevents.Pattern

object RegExPatterns {
    val DATE =
        "(([0|1])?[1-9][-/]){2}([1-9][0-9]([0-9][0-9])?)?"
    val TIME = "([0|1])?[1-9][:]([0-5])?[0-9]"
    val YEAR = "^20[1-9][0-9]"
    val DAY = "^([0-3])?[0-9](st|nd|rd|th)?(,|\\\\.)?"
    val AMPM = "(a|A)(\\.?)(m|M)(\\.?)|(p|P)(\\.?)(m|M)(\\.?)"
    val WORD = "[a-z,A-Z]"
}