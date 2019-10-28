package com.slightlynonprofessionalprogrammer.foodassistant.data

import java.io.Serializable

class Product(val productID:String, val userID:String, val productName: String  = "", val imageUrl: String = "", val expiryDate: String = "", val amount: String = "") : Serializable {
    constructor() : this("", "")
}