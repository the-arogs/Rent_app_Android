package com.rg.rentapp.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.classes.Tenant

import kotlinx.coroutines.tasks.await
import java.text.FieldPosition

class ShortlistRepository (private val context : Context) {
    private val tag = "Shortlist Repository"
    private val db = Firebase.firestore
    private val collectionRentals = "AllRentals"
    private val collectionUsers = "TenantCollection";

    var allRentals : MutableLiveData<List<Rental>> = MutableLiveData<List<Rental>>()
    var shortlist : MutableLiveData<List<Rental>> = MutableLiveData<List<Rental>>()
    // var shortlist:MutableList<Rental> = mutableListOf()
    var currentShortlistArray: ArrayList<String>? = arrayListOf()
    var shortlistArray:MutableLiveData<List<String>> = MutableLiveData<List<String>>()

    private var loggedInUserEmail = ""
    private lateinit var sharedPrefs: SharedPreferences

    init {
        sharedPrefs = context.getSharedPreferences("MY_APP_DB", Context.MODE_PRIVATE)

        if (sharedPrefs.contains("USER_EMAIL")){
            loggedInUserEmail = sharedPrefs.getString("USER_EMAIL", "NA").toString()
        }
    }

    fun retrieveAllRentals(){

        try {
            db.collection(collectionRentals)
                .addSnapshotListener(EventListener { result, error ->
                    if (error != null){
                        Log.e(tag,
                            "retrieveAllRentals: Listening to Expenses collection failed due to error : $error", )
                        return@EventListener
                    }

                    if (result != null){
                        Log.d(tag, "retrieveAllRentals: Number of documents retrieved : ${result.size()}")

                        val tempList : MutableList<Rental> = ArrayList<Rental>()

                        for (docChanges in result.documentChanges){

                            val currentDocument : Rental = docChanges.document.toObject(Rental::class.java)
                            Log.d(tag, "retrieveAllRentals: currentDocument : $currentDocument")

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
                        Log.d(tag, "retrieveAllRentals: tempList : $tempList")
                        //replace the value in allExpenses

                        allRentals.postValue(tempList)

                    }else{
                        Log.d(tag, "retrieveAllRentals: No data in the result after retrieving")
                    }
                })


        } catch (ex : java.lang.Exception){
            Log.e(tag, "retrieveAllRentals: Unable to retrieve all rentals : $ex", )
        }

    }

    fun addToShortlist(rental: Rental) {
        if (loggedInUserEmail.isNotEmpty()) {
            Log.d(tag, "addToShortlist: Logged in user: $loggedInUserEmail")
            Log.d(tag, "addToShortlist: selectedRental: $rental")

            // checking existing list
            retrieveShortlistArray()


            try {

                db.collection(collectionUsers)
                    .document(loggedInUserEmail)
                    .update("shortlist", FieldValue.arrayUnion(rental.rentalID.toString()))
                    .addOnSuccessListener { docRef ->
                        Log.d(tag, "addToShortlist: Document updated successfully : $docRef")
                    }
                    .addOnFailureListener { ex ->
                        Log.e(tag, "addToShortlist: Failed to update document : $ex", )
                    }

            } catch (ex : java.lang.Exception) {
                Log.d(tag, "addToShortlist: Couldn't add to shortlist  due to exception $ex")
            }

        } else {
            Log.e(tag, "No logged in user!")
        }

    }

    fun retrieveShortlistArray() {
        try {
            db.collection(collectionUsers)
                .document(loggedInUserEmail)
                .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(tag, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(tag, "Current data: ${snapshot.data}")
                    var currentUser = snapshot.toObject(Tenant::class.java)
                    // tenant.value = currentUser
                    shortlistArray.value = currentUser?.shortlist

                    // Log.d(tag, "shortlistArray data: ${shortlistArray.value}")

                } else {
                    Log.d(tag, "Current data: null")
                }
            }


        } catch(ex : java.lang.Exception) {
            Log.e(tag, "retrieveShortlist: Unable to retrieve shortlist due to: $ex")
        }
    }


    fun retrieveShortlist(documentIds:List<String>) {
        Log.d(tag, "documentIds data: $documentIds")

        val tempList : MutableList<Rental> = ArrayList<Rental>()
        for (id in documentIds) {
            db.collection(collectionRentals)
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        var rental = document.toObject(Rental::class.java)
                        tempList.add(rental!!)
                        // Log.d(tag, "tempList from repo: ${tempList}")
                        shortlist.postValue(tempList)

                    } else {
                        Log.d(tag, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)
                }
        }

        Log.d(tag, "shortlist from repo: ${shortlist.value}")

    }

    fun removeRental(rentalId: String){
        if (loggedInUserEmail.isNotEmpty()) {
            Log.d(tag, "removeRental: Logged in user: $loggedInUserEmail")
            Log.d(tag, "removeRental: selectedRentalId: $rentalId")

            try {
                db.collection(collectionUsers)
                    .document(loggedInUserEmail)
                    .update("shortlist", FieldValue.arrayRemove(rentalId))
                    .addOnSuccessListener { docRef ->
                        Log.d(tag, "removeRental: Document updated successfully : $docRef")
                    }
                    .addOnFailureListener { ex ->
                        Log.e(tag, "removeRental: Failed to update document : $ex", )
                    }

            } catch (ex : java.lang.Exception) {
                Log.d(tag, "removeRental: Couldn't remove from shortlist  due to exception $ex")
            }


        }  else {
            Log.e(tag, "No logged in user!")
        }

    }

    suspend fun searchByListRentals(keyword:String):ArrayList<Rental> {

        var tempList:ArrayList<Rental> = ArrayList<Rental>()
        var searchResult: ArrayList<Rental> = ArrayList<Rental>()

        try {

            db.collection(collectionRentals)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(tag, "searchRentals: ${document.id} => ${document.data}")
                        var docData = document.toObject(Rental::class.java)
                        tempList.add(docData)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "searchRentals: Error getting documents: ", exception)
                }.await()
        } catch (ex : java.lang.Exception) {
            Log.e(tag, "searchRentals: Unable to search rentals due to: $ex", )
        }

        if(tempList.isNotEmpty()){
            for (tmp in tempList) {
                if (tmp.address.contains(keyword)){
                    searchResult.add(tmp)
                }
            }
        }


        return searchResult

    }



    suspend fun checkShortlist(): ArrayList<String>? {
        var shortlistArray: ArrayList<String>? = null
        if (loggedInUserEmail.isNotEmpty()) {
            try {
                val userDocument = db.collection(collectionUsers).document(loggedInUserEmail)
                val userData = userDocument.get().await().toObject(Tenant::class.java)
                shortlistArray = userData?.shortlist

            } catch (ex : java.lang.Exception) {
                Log.e(tag, "checkShortlist: Unable to retrieve shortlist array due to: $ex")
            }

        } else {
            Log.e(tag, "No logged in user!")
        }
        return shortlistArray
    }



}