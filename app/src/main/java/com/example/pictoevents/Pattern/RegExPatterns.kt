package com.example.pictoevents.Pattern

object RegExPatterns {
    val DATE =
        "(([0|1])?[1-9][-/]){2}([1-9][0-9]([0-9][0-9])?)?" //"\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}([a-zA-z]{2})?";
    val TIME = "([0|1])?[1-9][:]([0-5])?[0-9]" //"\\d{1,2}[:]\\d{1,2}";
    val YEAR = "^20[1-9][0-9]"
    val DAY = "\\s([0-3])?[0-9](st|nd|rd|th)?\\s" //"([0-3])?[0-9]";
    val AMPM = "^(a|A|p|P)(m|M)$"
}