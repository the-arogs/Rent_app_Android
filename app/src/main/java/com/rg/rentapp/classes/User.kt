package com.rg.rentapp.classes

import com.rg.rentapp.enums.PropertyTypes
import com.rg.rentapp.enums.UserTypes

open class User(
    var name: String,
    var email: String,
    var phoneNumber: String,
    var password: String,
    var properties: MutableList<Rental>,
    var ownedProperties: MutableList<Rental>,
    var userID: Int = ++USER_COUNTER
) {

    companion object{
        var USER_COUNTER = 0
    }

    override fun toString(): String {
        return "User(name='$name', email='$email', phoneNumber='$phoneNumber', password='$password', properties=$properties, userID=$userID)"
    }


}