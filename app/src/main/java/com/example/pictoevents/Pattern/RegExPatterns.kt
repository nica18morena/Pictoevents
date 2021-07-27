package com.example.pictoevents.Pattern

object RegExPatterns {
    val DATE =
        "(([01])?[1-9][-/])(([0-3])?[1-9][-/])([1-9][0-9]([0-9][0-9])?)?"
    val TIME = "([0-2][0-9]:)|((^[1-9])(:([0-5][0-9])\$)?)"
    val YEAR = "^20[1-9][0-9]$"
    val DAY = "^([0-3])?[0-9]{1}(st|nd|rd|th)?(,|\\\\.)?$"
    val AMPM = "(a|A)(\\.?)(m|M)(\\.?)|(p|P)(\\.?)(m|M)(\\.?)"
    val WORD = "[a-z,A-Z]"
}