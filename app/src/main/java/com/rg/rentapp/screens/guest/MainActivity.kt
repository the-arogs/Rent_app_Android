package com.rg.rentapp.screens.guest

import android.content.Intent
import android.content.SharedPreferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rg.rentapp.R
import com.rg.rentapp.adapters.SearchListAdaptor
import com.rg.rentapp.adapters.ShortListAdaptor
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.classes.User
import com.rg.rentapp.databinding.ActivityMainBinding
import com.rg.rentapp.enums.PropertyTypes
import com.rg.rentapp.enums.UserTypes
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.repositories.ShortlistRepository
import com.rg.rentapp.screens.login.LoginActivity
import com.rg.rentapp.screens.owner.AddRentalActivity
import com.rg.rentapp.screens.owner.OwnedPropertyList
import com.rg.rentapp.screens.tenant.ShortListActivity
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    // declaring shared preference variables
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var shortlistRepository: ShortlistRepository
    lateinit var adapter: SearchListAdaptor
    var searchList:ArrayList<Rental> = ArrayList()

    private lateinit var firebaseAuth : FirebaseAuth


    lateinit var ownerRepository: OwnerRepository
    private var isLoggedIn :Boolean = false

    // on startup initialize variable with hardcoded rentals
    // user searches through hardcoded rentals adn yields a search result
    // place that search result in another variable
    // display the contents of the search result variable

    // when a user clicks on a search result item
    // open another activity displaying the details of that search result item

    // when a user clicks on the add to shortlist btn;
    // the add to shortlist function  runs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        ownerRepository = OwnerRepository(this)

        this.firebaseAuth = FirebaseAuth.getInstance()
        shortlistRepository = ShortlistRepository(applicationContext)


//        binding.btnSearchAddress.setOnClickListener(this)

        // initializing shared preferences variables
        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        var currUserFromSP = sharedPreferences.getString("USER_EMAIL", "")
        Log.d(TAG, "onCreate:  logged in user: $currUserFromSP")
        Log.d(TAG, "onCreate: is logged in : $isLoggedIn")



        adapter = SearchListAdaptor(searchList, { pos -> addToShortlist(pos) })
        binding.rvSearchList.adapter = adapter
        binding.rvSearchList.layoutManager = LinearLayoutManager(this)


        // adding a line between each item in the list
        binding.rvSearchList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        binding.btnSearch.setOnClickListener(this)
        binding.btnMapSearch.setOnClickListener(this)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.item_logout -> {
                logout()
                return true
            }
            R.id.item_view_shortlist -> {
                if(isLoggedIn){
                    // add to shortlist
                    var intent = Intent(this, ShortListActivity::class.java)
                    startActivity(intent)
                } else {
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                }
                return true
            }
            R.id.item_view_property_list -> {
                if(isLoggedIn){
                    var intent = Intent(this, OwnedPropertyList::class.java)
                    startActivity(intent)
                } else {
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                return true
            }
            R.id.item_post_property_list -> {
                var userType = ""
                lifecycleScope.launch {
                    var currUserFromSP = sharedPreferences.getString("USER_EMAIL", "")
                    userType = ownerRepository.getUserType(currUserFromSP!!)

                }

                if(isLoggedIn &&  userType == "Owner"){

                    var intent = Intent(this, AddRentalActivity::class.java)
                    startActivity(intent)
                } else {
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    val snackbar = Snackbar.make(binding.rootLayout, "User does not have Owner privileges", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
                return true
            }
            R.id.item_view_login -> {
                if(isLoggedIn){
                    val snackbar = Snackbar.make(binding.rootLayout, "User is already logged in", Snackbar.LENGTH_LONG)
                    snackbar.show()
                } else {
                    var intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }




    override fun onClick(v: View?) {
        when(v?.id) {

            R.id.btn_search  -> {
                if (binding.etSearch.text.toString().isNullOrEmpty()){
                    this.getSearchList()
                } else {
                    searchByList()
                }
            }

//            R.id.btn_search_address  -> {
//                val searchStr = binding.etSearch.text.toString()
//
//                this.getSearchByAddressList(searchStr)
//            }
            R.id.btn_map_search -> {
                if (searchList.isNullOrEmpty()){
                    val snackbar = Snackbar.make(binding.rootLayout, "Search from list first", Snackbar.LENGTH_LONG)
                    snackbar.show()
                } else {
                    openMap()
                }

            }

        }

    }

//    fun isLoggedIn():Boolean {
//        val currUserFromSP = sharedPreferences.getString("USER_EMAIL", "")
//        return (currUserFromSP != "")
//    }

//    fun checkUserType(): String {
//        return UserTypes.OWNER.name
//    }




    fun getSearchList() {
        val snackbar = Snackbar.make(binding.rootLayout, "Fetching search result", Snackbar.LENGTH_LONG)
        snackbar.show()

        shortlistRepository.retrieveAllRentals()

        shortlistRepository.allRentals.observe(this, androidx.lifecycle.Observer { rentalsList ->
            if(rentalsList != null){
                Log.d(TAG, "retrieveAllRentals: rentalsList : $rentalsList")

                // clear the existing list to avoid duplicate records
                searchList.clear()
                searchList.addAll(rentalsList)
                adapter.notifyDataSetChanged()

//                for (expense in expenseList){
//                    Log.e(TAG, "onResume: Expense : ${expense}", )
//                    if (!expenseArrayList.contains(expense)) {
//                        expenseArrayList.add(expense)
//                        expenseAdapter.notifyDataSetChanged()
//                    }
//                }
            } })

    }


    fun logout() {
        Firebase.auth.signOut()
        this.prefEditor.clear()

        this.prefEditor.apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        val snackbar = Snackbar.make(binding.root, "Logout", Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    fun addToShortlist(position:Int) {
        if (isLoggedIn()){
            val snackbar = Snackbar.make(binding.rootLayout, "Adding to Shortlist", Snackbar.LENGTH_LONG)
            snackbar.show()

            val selectedRental = this.searchList[position]

            lifecycleScope.launch {
                var currentShortlist = shortlistRepository.checkShortlist()
                if (currentShortlist != null){
                    if (currentShortlist.contains(selectedRental.rentalID.toString())) {
                        val snackbar = Snackbar.make(
                            binding.rootLayout,
                            "Already added to Shortlist",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.show()
                    } else {
                        shortlistRepository.addToShortlist(selectedRental)
                    }
                } else {
                    shortlistRepository.addToShortlist(selectedRental)

                }

            }
        } else {
            viewLogin()
        }
    }

    fun viewShortlist() {
        if (isLoggedIn()) {
            var intent = Intent(this, ShortListActivity::class.java)
            startActivity(intent)
        } else {
            viewLogin()
        }
    }

    fun viewLogin() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    fun isLoggedIn(): Boolean {
        val loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", "")
        return loggedInUserEmail != ""
    }

    fun searchByList() {
        var keyword = binding.etSearch.text.toString()


        if(keyword.isNotEmpty()) {
            lifecycleScope.launch{
                var searchResult = shortlistRepository.searchByListRentals(keyword)
                Log.d(TAG, "search: $searchResult")
                if(searchResult.isNotEmpty()) {
                    searchList.clear()
                    searchList.addAll(searchResult)
                    adapter.notifyDataSetChanged()
                } else {
                    val snackbar = Snackbar.make(binding.rootLayout, "No Rental Found", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            }
        } else {
            val snackbar = Snackbar.make(binding.rootLayout, "Search field is empty", Snackbar.LENGTH_LONG)
            snackbar.show()
            binding.etSearch.error = "Search field is empty"
        }
    }

    fun openMap() {


        if (!searchList.isNullOrEmpty()) {
            val gson = Gson()
            var objStrings = gson.toJson(searchList)

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("MARKER_LIST", objStrings)
            startActivity(intent)
        } else {
            val snackbar = Snackbar.make(binding.rootLayout, "No Search results", Snackbar.LENGTH_LONG)
            snackbar.show()

        }

    }




}



// Shared Preferences Schema
// "RENTALS" : -> Stores list of rental properties
// "USER_COLLECTION": -> Stores list of all users in system
// "CURR_USER": -> Stores details of the user logged in
// "SHORTLIST": -> Stores shortlist


// Check Login function
// it checks if the CURR_USER key is empty or not and return boolean
// If empty return false
// If not empty return true

//






// val snackbar = Snackbar.make(binding.rootLayout, "Add btn", Snackbar.LENGTH_LONG)
//                snackbar.show()

