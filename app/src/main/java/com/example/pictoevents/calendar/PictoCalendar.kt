package com.example.pictoevents.calendar

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.example.pictoevents.Repository.Repository
import java.lang.NullPointerException
import java.util.*

class PictoCalendar (val context: Context){
    private val TAG = PictoCalendar::class.java.simpleName
    private var calID: Long = 0L
    private var TITLE = "Event"
    private val ACCOUNT_NAME = "Pictoevents"
    private val CALENDAR_NAME = "Cal Pictoevents"
    private val CALENDAR_COLOR = 0xEA8561
    private val CALENDAR_TIMEZONE = "America/Los_Angeles"
    private lateinit var calObj: CalendarObject

    fun checkCalendars() {
        val calCursor: Cursor?
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE
        )
        try {
            calCursor = this.context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
            if (calCursor!!.moveToFirst()) {
                do {
                    val displayName = calCursor.getString(1)
                    Log.d(
                        ContentValues.TAG,
                        "Calendar names: $displayName"
                    )
                    if (displayName != null && displayName.isNotEmpty()
                        && displayName == CALENDAR_NAME)
                    {
                        calID = calCursor.getLong(0)
                    }
                } while (calCursor.moveToNext())
            }
            calCursor.close()
        } catch (e: SecurityException) {
            Log.e(ContentValues.TAG, "Error occurred: " + e.stackTrace)
        }
    }

    fun getAllCalendarEvents() : MutableList<String> {
        val eventList: MutableList<String> = mutableListOf()
        val calCursor: Cursor?
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DURATION,
            CalendarContract.Events.DESCRIPTION
        )
        try {
            calCursor = this.context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                CalendarContract.Events.CALENDAR_ID + " = ${calID}",
                null,
                CalendarContract.Events.DTSTART + " ASC"
            )
            if (calCursor!!.moveToFirst()) {
                do {
                    val eventInfo = "${calCursor.getString(0)},${calCursor.getString(1)},${calCursor.getString(2)},${calCursor.getString(3)}"
                    eventList.add(eventInfo)

                } while (calCursor.moveToNext())
            }
            calCursor.close()
        } catch (e: SecurityException) {
            Log.e(ContentValues.TAG, "Error occurred: " + e.stackTrace)
        }
        return eventList
    }
    fun getCalId (): Long{
        return calID
    }

    fun setCalObj(calObject: CalendarObject){
        calObj = calObject
    }

    fun getCalObj(): CalendarObject{
        return calObj
    }

    fun buildCalEvent(): Boolean{
        var isSuccessful = true
        try {
            val cv = this.buildCalContentValues()
            val builder = this.generateCalendarBuilder()
            this.getCalendarID(builder, cv)
            val calendar = this.initializeCalendar()
            Repository.calendar = calendar
            val cv2 = this.createCalEvents(calendar)
            this.getCalEventID(cv2)
        }
        catch(e: NullPointerException){
            isSuccessful = false
            Log.e(TAG, "Encountered error: $e")
        }
        return isSuccessful
    }

    private fun getContentResolver(): ContentResolver {
        return this.context.contentResolver
    }

    private fun buildCalContentValues(): ContentValues{
        val cv = ContentValues()

        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        cv.put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME)
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, CALENDAR_COLOR)
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
        cv.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, CALENDAR_TIMEZONE)
        cv.put(CalendarContract.Calendars.VISIBLE, 1)
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1)

        return cv
    }

    private fun generateCalendarBuilder(): Uri.Builder {
        val builder = CalendarContract.Calendars.CONTENT_URI.buildUpon()
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        return builder
    }
//check if this needs to be called if already have calID
    private fun getCalendarID(builder: Uri.Builder,
                              cv: ContentValues): Long {

        if(this.getCalId() == 0L) {
            val uri = getContentResolver().insert(builder.build(), cv)
            if (uri != null){
                calID = uri.lastPathSegment!!
                    .toLong()
            }
       }

        return this.getCalId()
    }

    private fun initializeCalendar(): Calendar{

        val calendar = GregorianCalendar()

        calendar.timeZone = TimeZone.getDefault()
        if (calObj.year != 0){
            calendar.set(Calendar.YEAR, calObj.year)
        }

        if(calObj.month != 0){
            calendar.set(Calendar.MONTH, calObj.month - 1)
        }

        if(calObj.dayOfMonth != 0){
            calendar.set(Calendar.DAY_OF_MONTH, calObj.dayOfMonth)
        }

        if(calObj.hour != 0){
            calendar.set(Calendar.HOUR, calObj.hour)
        }

        if(calObj.second != 0){
            calendar.set(Calendar.SECOND, calObj.second)
        }

        if(calObj.title.isNotEmpty()){
            TITLE = calObj.title
        }

        if(calObj.AmPm == 0){
            calendar.set(Calendar.AM_PM, Calendar.AM)
        }
        else{
            calendar.set(Calendar.AM_PM, Calendar.PM)
        }
        calendar.set(Calendar.MINUTE, calObj.minute)

        //calendar.set(Calendar.AM_PM, calObj.AmPm)
        Log.d(TAG, "Event: ${calObj.title} " +
                "Month: ${calObj.month}, Day: ${calObj.dayOfMonth},\n" +
                "Year: ${calObj.year}, Hour: ${calObj.hour},\n" +
                "Min: ${calObj.minute}, AMPM: ${calObj.AmPm}")

        return calendar
    }

    private fun createCalEvents(cal: Calendar): ContentValues{
        val cv = ContentValues()
        val timeinMillS = cal.timeInMillis

        cv.put(CalendarContract.Events.DTSTART, cal.timeInMillis)
        cv.put(CalendarContract.Events.DTEND, cal.timeInMillis)
        cv.put(CalendarContract.Events.TITLE, TITLE)
        cv.put(CalendarContract.Events.EVENT_LOCATION, " ")
        cv.put(CalendarContract.Events.CALENDAR_ID, this.getCalId())
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, cal.timeZone.id)
        cv.put(CalendarContract.Events.DESCRIPTION, " ")
        cv.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
        cv.put(CalendarContract.Events.SELF_ATTENDEE_STATUS, CalendarContract.Events.STATUS_CONFIRMED)
        cv.put(CalendarContract.Events.ORGANIZER, ACCOUNT_NAME)
        cv.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, 1)
        cv.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        return cv
    }

    private fun getCalEventID(cv: ContentValues): Long{

        if (context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED){
            val uri = this.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, cv)
            if (uri != null)
            {
                return uri.lastPathSegment!!.toLong()
            }
        }
        return 0
    }
}