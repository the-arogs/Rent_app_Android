package com.rg.rentapp.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rg.rentapp.classes.Rental

class RentalRepository (private val context: Context) {
    private val TAG = this.toString()
    private val db = Firebase.firestore

    private val COLLECTION_RENTALS = "Rentals"
    private val COLLECTION_ALLRENTALS = "AllRentals"
    private val COLLECTION_OWNERS = "Owners"
    private val FIELD_PROPERTYTYPE = "propertyType"
    private val FIELD_OWNER = "owner"
    private val FIELD_PRICE = "price"
    private val FIELD_DESCRIPTION = "description"
    private val FIELD_ADDRESS = "address"
    private val FIELD_AVAILABLE = "available"
    private val FIELD_LATITUDE = "lat"
    private val FIELD_LONGITUDE = "long"
    private val FIELD_RENTALID = "rentalID"
    private val FIELD_SPECIFICATIONS = "specification"
    var allRentals : MutableLiveData<List<Rental>> = MutableLiveData<List<Rental>>()
    var allOwnerRentals : MutableLiveData<List<Rental>> = MutableLiveData<List<Rental>>()


    private var loggedInUserEmail = ""
    private var sharedPrefs : SharedPreferences = context.getSharedPreferences("MY_APP_DB", Context.MODE_PRIVATE)

    init {
        if (sharedPrefs.contains("USER_EMAIL")){
            loggedInUserEmail = sharedPrefs.getString("USER_EMAIL", "NA").toString()
        }
    }

    fun addRentalToOwnerCollection(newRental: Rental) {
        try{
            val data : MutableMap<String, Any> = HashMap()

            data[FIELD_PROPERTYTYPE] = newRental.propertyType
            data[FIELD_OWNER] = newRental.owner
            data[FIELD_PRICE] = newRental.price
            data[FIELD_DESCRIPTION] = newRental.description
            data[FIELD_ADDRESS] = newRental.address
            data[FIELD_LATITUDE] = newRental.lat
            data[FIELD_LONGITUDE] = newRental.long
            data[FIELD_AVAILABLE] = newRental.available
            data[FIELD_RENTALID] = newRental.rentalID.toString()
            data[FIELD_SPECIFICATIONS] = newRental.specification


            db.collection(COLLECTION_OWNERS)
                .document(loggedInUserEmail)
                .collection(COLLECTION_RENTALS)
                .document(newRental.rentalID)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addRentalToOwner: Rental document successfully created with ID $docRef")
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "addRentalToOwner: Unable to create rental document due to exception : $ex", )
                }

        }catch (ex : Exception){
            Log.e(TAG, "addRentalToOwner: Couldn't add rental document $ex", )
        }
    }

    fun addRentalToRentalCollection (newRental: Rental) {
        try{
            val data : MutableMap<String, Any> = HashMap()

            data[FIELD_PROPERTYTYPE] = newRental.propertyType
            data[FIELD_OWNER] = newRental.owner
            data[FIELD_PRICE] = newRental.price
            data[FIELD_DESCRIPTION] = newRental.description
            data[FIELD_ADDRESS] = newRental.address
            data[FIELD_LATITUDE] = newRental.lat
            data[FIELD_LONGITUDE] = newRental.long
            data[FIELD_AVAILABLE] = newRental.available
            data[FIELD_RENTALID] = newRental.rentalID.toString()
            data[FIELD_SPECIFICATIONS] = newRental.specification


            db.collection(COLLECTION_ALLRENTALS)
                .document(newRental.rentalID)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "addRentalToOwner: Rental document successfully created with ID $docRef")
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "addRentalToOwner: Unable to create rental document due to exception : $ex", )
                }

        }catch (ex : Exception){
            Log.e(TAG, "addRentalToOwner: Couldn't add rental document $ex", )
        }
    }

    fun retrieveAllOwnerRentals(){
        if (loggedInUserEmail.isNotEmpty()) {
            try{
                db.collection(COLLECTION_OWNERS)
                    .document(loggedInUserEmail)
                    .collection(COLLECTION_RENTALS)
                    .addSnapshotListener(EventListener{ result, error ->
                        if (error != null){
                            Log.e(TAG,
                                "retrieveAllOwnerRentals: Listening to rentals collection failed due to error : $error", )
                            return@EventListener
                        }

                        if (result != null){
                            Log.d(TAG, "retrieveAllOwnerRentals: Number of documents retrieved : ${result.size()}")

                            var tempList : MutableList<Rental> = ArrayList<Rental>()

                            for (docChanges in result.documentChanges){

                                val currentDocument : Rental = docChanges.document.toObject(Rental::class.java)
                                Log.d(TAG, "retrieveAllOwnerRentals: currentDocument : $currentDocument")

                                when(docChanges.type){
                                    DocumentChange.Type.ADDED -> {
                                        //do necessary changes to your local list of objects
                                        tempList.add(currentDocument)
                                    }
                                    DocumentChange.Type.MODIFIED -> {

                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        tempList.remove(currentDocument)
                                    }
                                }
                            }//for
                            Log.d(TAG, "retrieveAllOwnerRentals: tempList : $tempList")
                            //replace the value in allExpenses

                            allOwnerRentals.postValue(tempList)

                        }else{
                            Log.d(TAG, "retrieveAllOwnerRentals: No data in the result after retrieving")
                        }
                    })


            }
            catch (ex : java.lang.Exception){
                Log.e(TAG, "retrieveAllOwnerRentals: Unable to retrieve all rentals : $ex", )
            }
        }else{
            Log.e(TAG, "retrieveAllOwnerRentals: Cannot retrieve rentals without user's email address. You must sign in first.", )
        }
    }

    fun retrieveAllRentals () {
        try{
            db
                .collection(COLLECTION_ALLRENTALS)
                .addSnapshotListener(EventListener{ result, error ->
                    if (error != null){
                        Log.e(TAG,
                            "retrieveAllOwnerRentals: Listening to rentals collection failed due to error : $error", )
                        return@EventListener
                    }

                    if (result != null){
                        Log.d(TAG, "retrieveAllOwnerRentals: Number of documents retrieved : ${result.size()}")

                        val tempList : MutableList<Rental> = ArrayList<Rental>()

                        for (docChanges in result.documentChanges){

                            val currentDocument : Rental = docChanges.document.toObject(Rental::class.java)
                            Log.d(TAG, "retrieveAllOwnerRentals: currentDocument : $currentDocument")

                            when(docChanges.type){
                                DocumentChange.Type.ADDED -> {
                                    //do necessary changes to your local list of objects
                                    tempList.add(currentDocument)
                                }
                                DocumentChange.Type.MODIFIED -> {

                                }
                                DocumentChange.Type.REMOVED -> {

                                }
                            }
                        }//for
                        Log.d(TAG, "retrieveAllOwnerRentals: tempList : $tempList")
                        //replace the value in allExpenses

                        allRentals.postValue(tempList)

                    }else{
                        Log.d(TAG, "retrieveAllOwnerRentals: No data in the result after retrieving")
                    }
                })


        }
        catch (ex : java.lang.Exception){
            Log.e(TAG, "retrieveAllOwnerRentals: Unable to retrieve all rentals : $ex", )
        }

    }

    fun deleteOwnerRental(currRental: Rental) {
        try {
            db.collection(COLLECTION_OWNERS)
                .document(loggedInUserEmail)
                .collection(COLLECTION_RENTALS)
                .document(currRental.rentalID)
                .delete()
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "deleteOwnerRental: Deleted successfully $doc")
                }.addOnFailureListener { doc ->
                    Log.d(TAG, "deleteOwnerRental: Failed to delete $doc")
                }
        }
        catch (err: Exception) {
            Log.d(TAG, "deleteOwnerRental: failed to delete. Error: $err")
        }
    }

    fun deleteRental(currRental: Rental) {
        try {
            db.collection(COLLECTION_ALLRENTALS)
                .document(currRental.rentalID)
                .delete()
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "deleteOwnerRental: Deleted successfully $doc")
                }.addOnFailureListener { doc ->
                    Log.d(TAG, "deleteOwnerRental: Failed to delete $doc")
                }
        }
        catch (err: Exception) {
            Log.d(TAG, "deleteOwnerRental: failed to delete. Error: $err")
        }
    }
}