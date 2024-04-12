package com.rg.rentapp.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.rg.rentapp.classes.Owner
import com.rg.rentapp.classes.Tenant
import kotlinx.coroutines.tasks.await


class OwnerRepository (private val context: Context) {
    private val TAG = this.toString();

    private val db = Firebase.firestore
    private val COLLECTION_OWNER = "Owners"
    private val COLLECTION_TENANT = "TenantCollection"
    private val FIELD_EMAIL = "email"
    private val FIELD_NAME = "name"
    private val FIELD_PHONE = "phoneNumber"
    private val FIELD_USERTYPE = "userType"


    fun addOwnerToDB (newOwner: Owner) {
        try{
            val data : MutableMap<String, Any> = HashMap()

            data[FIELD_EMAIL] = newOwner.email
            data[FIELD_PHONE] = newOwner.phoneNumber
            data[FIELD_NAME] = newOwner.name
            data[FIELD_USERTYPE] = "Owner"



            db.collection(COLLECTION_OWNER)
                .document(newOwner.email)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addOwnerToDB: User document successfully added with ID $docRef")
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "addOwnerToDB: Unable to add user document due to exception : $ex", )
                }

        }catch (ex : Exception){
            Log.e(TAG, "addOwnerToDB: Couldn't add user document $ex", )
        }
    }

    fun addTenantToDB (newTenant: Tenant) {
        try{
            val data : MutableMap<String, Any> = HashMap()

            data[FIELD_EMAIL] = newTenant.email
            data[FIELD_PHONE] = newTenant.phoneNumber
            data[FIELD_NAME] = newTenant.name
            data[FIELD_USERTYPE] = "Tenant"


            db.collection(COLLECTION_TENANT)
                .document(newTenant.email)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addOwnerToDB: User document successfully added with ID $docRef")
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "addOwnerToDB: Unable to add user document due to exception : $ex", )
                }

        }catch (ex : Exception){
            Log.e(TAG, "addOwnerToDB: Couldn't add user document $ex", )
        }
    }

    suspend fun getOwnerFromDB (userEmail:String): Owner {
        var currUser : Owner? = Owner()
        try{
            var userRef = db.collection(COLLECTION_OWNER).document(userEmail)
            currUser = userRef.get().await().toObject(Owner::class.java)

        }catch (ex : Exception){
            Log.e(TAG, "getOwnerFromDB: Couldn't get user document $ex", )
        }
        return currUser!!
    }

    suspend fun getUserType (userEmail: String) :String {
        var userType :String = "Tenant"
        var currOwner : Owner? = null
        try{
            var userRef = db.collection(COLLECTION_OWNER).document(userEmail)
            var currUser = userRef.get().await().toObject(Owner::class.java)
            Log.d("User", "$currUser")
            if (currUser != null) {
                userType = "Owner"
            }

        }catch (ex : Exception){
            Log.e(TAG, "getOwnerFromDB: Couldn't get user document $ex", )
        }

        return userType
    }
}