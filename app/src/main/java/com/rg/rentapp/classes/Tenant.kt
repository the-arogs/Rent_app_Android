package com.rg.rentapp.classes

import com.rg.rentapp.enums.UserTypes

class Tenant(
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var userType: String = "Tenant",
    var shortlist: ArrayList<String> = arrayListOf()
)  {
    override fun toString(): String {
        return "User(name='$name', email='$email', phoneNumber='$phoneNumber', password='$password', userType='$userType', shortlist=$shortlist)"
    }

}


