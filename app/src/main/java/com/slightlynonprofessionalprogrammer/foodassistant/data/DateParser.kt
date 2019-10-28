package com.slightlynonprofessionalprogrammer.foodassistant.data

import android.util.Log
import java.lang.NullPointerException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateParser {

    val FORMAT = DateFormat.LONG
    val LOCALE = Locale.ENGLISH
    val PATTERN = "dd-MM-yyyy"

    fun currentDate(): String{
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat(PATTERN)
        val dateStr =  sdf.format(date)
        Log.d("Parser", "Currnet Date: $dateStr")
        return  dateStr
    }

    fun dateToString(date: Date) : String {
        val sdf = SimpleDateFormat(PATTERN)
        return sdf.format(date)
    }

    fun daysBetweenCurrent(dateStr: String): Long {
        Log.d("Parser", "$dateStr")
        val result: Long
        try {
            result = daysBetween(dateStr, DateParser.currentDate())
        } catch (e: Exception) {
            throw e
        }
        return result
    }

    fun daysBetween(dateStr1: String, dateStr2: String): Long {
        val sdf = SimpleDateFormat(PATTERN)
        var date1: Date? = null
        var date2: Date? = null
        try {
            date1 = sdf.parse(dateStr1)
            date2 = sdf.parse(dateStr2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if(date1 == null || date2 == null)
            throw NullPointerException("Cannot parse dateString to Date")
        return (date1!!.getTime() - date2!!.getTime()) / (24 * 60 * 60 * 1000)
    }


}