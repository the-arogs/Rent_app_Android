package com.rg.rentapp.classes

import com.rg.rentapp.enums.PropertyTypes
import java.io.Serializable
import java.util.UUID

class Rental (
    var propertyType: PropertyTypes = PropertyTypes.CONDO,
    var owner: String = "",
    var specification: MutableList<String> = mutableListOf(),
    var price: Double = 0.0,
    var description: String = "",
    var address: String = "",
    var lat : Double = 0.0,
    var long: Double = 0.0,
    var available: String = "",
    var rentalID:String = UUID.randomUUID().toString()
): Serializable{
    companion object{
        var RENTAL_COUNTER = 0
    }

    override fun toString(): String {
        return "Rental(propertyType=$propertyType, owner='$owner', specification=$specification, price=$price, description='$description', address='$address', lat=$lat, long=$long, isAvailable=$available, rentalID=$rentalID)"
    }


}