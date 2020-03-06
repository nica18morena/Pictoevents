package com.example.pictoevents.Calendar

import com.example.pictoevents.Dictionary.MonthDictionary
import com.example.pictoevents.Dictionary.WeekDictionary
import com.example.pictoevents.Pattern.RegExPatterns
import java.util.regex.Matcher
import java.util.regex.Pattern

class CalendarObjectsGenerator(val ocrText: String, val calendarObject: CalendarObject)
{
    private fun tokenizeText(): List<String>
    {
        val tokens = this.ocrText.split("//s")
        return tokens
    }

    fun identifyCalendarComponents() {

        val iterator = this.tokenizeText().listIterator()
        for (words in iterator ){
            val wordSplit = words.split(",'")

            if (!this.isValid(wordSplit)){
                continue;
            }

            var word = wordSplit[0]
            val hasDatePattern = this.findDatePattern(word)
            val hasAMPMPattern = this.findAMPMPattern(word)
            var hasTimePattern = false
            var hasYearPattern = false
            var hasDaysPattern = false
            var matchWeek = false
            var matchMonth = false

            if(!hasDatePattern){
                hasTimePattern = this.findTimePattern(word)
            }
            if( !hasDatePattern && !hasTimePattern){
                hasDaysPattern = this.findDayPattern(word)

                if(hasDatePattern && word.length > 4){
                    hasDaysPattern = false
                }
            }
            if(!hasDatePattern && !hasTimePattern && !hasDaysPattern){
                hasYearPattern = this.findYearPattern(word)
            }
            if(!hasDatePattern && !hasTimePattern && !hasYearPattern && !hasDaysPattern){
                matchWeek = WeekDictionary().weekDict().contains(word.toLowerCase())
                matchMonth = MonthDictionary().getMonthDict().contains(word.toLowerCase())
            }

            if(hasDatePattern){

            }
        }

    }

    private fun findDatePattern(word: String): Boolean{

        val datePattern = Pattern.compile(RegExPatterns.DATE)
        val dateMatcher = datePattern.matcher(word)

        return this.foundMatch(dateMatcher)
    }

    private fun findTimePattern(word: String): Boolean{

        val timePattern = Pattern.compile(RegExPatterns.TIME)
        val timeMatcher = timePattern.matcher(word)

        return this.foundMatch(timeMatcher)
    }

    private fun findYearPattern(word: String): Boolean{

        val yearPattern = Pattern.compile(RegExPatterns.YEAR)
        val yearMatcher = yearPattern.matcher(word)

        return this.foundMatch(yearMatcher)
    }

    private fun findDayPattern(word: String): Boolean{

        val dayPattern = Pattern.compile(RegExPatterns.DAY)
        val dayMatcher = dayPattern.matcher(word)

        return this.foundMatch(dayMatcher)
    }

    private fun findAMPMPattern(word: String): Boolean{

        val ampmPattern = Pattern.compile(RegExPatterns.AMPM)
        val ampmMatcher = ampmPattern.matcher(word)

        return this.foundMatch(ampmMatcher)
    }

    private fun foundMatch(_matcher: Matcher): Boolean {
        var foundMatch = false
        while (_matcher.find()) {
            if (_matcher.group().isNotEmpty()) {
                foundMatch = true
            }
        }
        return foundMatch
    }

    private fun isValid(word: List<String>): Boolean {
        if (word.isEmpty() || word[0] == "") {
            //Contains nothing
            return false
        }
        if (word.size <= 2 && word[0].matches("\\w".toRegex())) {
            //Is a word less than 2 chars
            return false
        }
        if (word.size > 7 && word[0].matches("[^0-9]".toRegex())) {
            //Is a word with more than 7 chars (contains no numbers)
            return false
        }
        return true
    }

    private fun decomposeDate(date: String){
        // Sample date pattern 3/5/2020
        // Assumption M-D-Y formats
        val dates = date.split("[/-]".toRegex())
    }
}