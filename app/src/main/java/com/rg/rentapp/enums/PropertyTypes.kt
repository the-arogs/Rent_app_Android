package com.rg.rentapp.enums

enum class PropertyTypes {
    CONDO,
    HOUSE,
    BASEMENT,
    APARTMENT,
    NONE;

    override fun toString(): String {
        return "$name"
    }
}