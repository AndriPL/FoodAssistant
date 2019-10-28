package com.slightlynonprofessionalprogrammer.foodassistant.data

import android.util.Log
import java.lang.Exception
import java.lang.NullPointerException

class ProductComparator : Comparator<Product> {
    override fun compare(o1: Product?, o2: Product?): Int {
        val FIRST_GREATER = +1
        val SECOND_GREATER = -1
        val EQUAL = 0
        if(o1 == null || o1.expiryDate == "")
            return FIRST_GREATER
        else if(o2 == null || o2.expiryDate == "")
            return SECOND_GREATER
        else {
            try{
                if(DateParser.daysBetween(o1.expiryDate, o2.expiryDate) >= 0)
                    return FIRST_GREATER
                else
                    return SECOND_GREATER
            } catch (e: Exception) {
                Log.e("Product Comparator", "", e)
                return EQUAL
            }
        }
    }
}