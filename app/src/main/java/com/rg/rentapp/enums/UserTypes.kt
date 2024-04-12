package com.rg.rentapp.enums

enum class UserTypes {
    Tenant,
    Owner;

    override fun toString(): String {
        return "$name"
    }

}