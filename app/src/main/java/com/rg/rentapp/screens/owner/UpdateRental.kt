package com.rg.rentapp.screens.owner

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rg.rentapp.R
import com.rg.rentapp.classes.Owner
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.databinding.ActivityUpdateRentalBinding
import com.rg.rentapp.enums.PropertyTypes
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.repositories.RentalRepository
import com.rg.rentapp.screens.guest.MainActivity
import kotlinx.coroutines.launch
import java.util.Locale

class UpdateRental : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateRentalBinding
    private lateinit var rentalObj:Rental
    lateinit var rentalRepository: RentalRepository
    val TAG = "UpdateRental Activity"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var ownerRepository: OwnerRepository
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    // permissions array
    private val APP_PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRentalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()
        rentalRepository = RentalRepository(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        var currUserFromSP = sharedPreferences.getString("USER_EMAIL", "")





        var latitude : Double = 0.0
        var longitude : Double = 0.0
        var currentIntent = this@UpdateRental.intent

        if (currentIntent != null) {
            rentalObj = currentIntent.getSerializableExtra("RENTAL_OBJ") as Rental
        } else {
            rentalObj = Rental(
                PropertyTypes.NONE, "N/A", mutableListOf(),
                0.0, "N/A", "N/A", 0.0, 00.0)
        }

        binding.etDesc.setText( rentalObj.description)
        binding.etAddress.setText(rentalObj.address)
        binding.etPrice.setText(rentalObj.price.toString())


        this.binding.btnUpdate.setOnClickListener {
            val propertyType: RadioButton = findViewById(binding.rg.checkedRadioButtonId)
            var isAvailCheck: Boolean = binding.swAvailable.isChecked
            var available = ""
            if (isAvailCheck) {
                available = "Yes"
            } else {
                available = "No"
            }
            val propDesc: String = binding.etDesc.text.toString()
            val propAddress: String = binding.etAddress.text.toString()
            var result : Pair<Double, Double>? = getLatandLong(propAddress)
            if (result != null) {
                var (lat, long) = result!!
                latitude = lat
                longitude = long
            }
            Log.d(TAG, "onCreate: After getLatandLong() lat: $latitude, long: $longitude,")
            val rentPrice: Double = binding.etPrice.text.toString().toDouble()
            val numBedrooms: String = binding.etBedroom.text.toString()
            val numBathrooms: String = binding.etBathroom.text.toString()
            val numKitchens: String = binding.etKitchen.text.toString()
            var propSpecs = mutableListOf<String>()
            propSpecs.add("$numBedrooms Bedroom(s)")
            propSpecs.add("$numBathrooms Bathroom(s)")
            propSpecs.add("$numKitchens Kitchen(s)")
            if (binding.cbGym.isChecked) propSpecs.add(binding.cbGym.text.toString())
            if (binding.cbPool.isChecked) propSpecs.add(binding.cbPool.text.toString())
            if (binding.cbDen.isChecked) propSpecs.add(binding.cbDen.text.toString())
            if (binding.cbBalcony.isChecked) propSpecs.add(binding.cbBalcony.text.toString())

            var ownerName = currUserFromSP!!


            var newRental = Rental(
                PropertyTypes.valueOf(propertyType.text.toString().uppercase()),
                ownerName,
                propSpecs,
                rentPrice,
                propDesc,
                propAddress,
                latitude,
                longitude,
                available,
                rentalObj.rentalID
            )

            lifecycleScope.launch {
                rentalRepository.addRentalToRentalCollection(newRental)
                rentalRepository.addRentalToOwnerCollection(newRental)
            }


            Log.d("newRental", newRental.toString())
            Snackbar.make(binding.root, "Added rental successfully", Snackbar.LENGTH_LONG).show()

            val intent = Intent(this, OwnedPropertyList::class.java)
            startActivity(intent)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.owner_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.home -> {
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                return true
            }
            R.id.item_logout -> {
                logout()
                return true
            }

            R.id.item_view_property_list -> {

                var intent = Intent(this, OwnedPropertyList::class.java)
                startActivity(intent)

                return true
            }
            R.id.item_post_property_list -> {

                var intent = Intent(this, AddRentalActivity::class.java)
                startActivity(intent)

                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

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

    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {

            resultsList ->
        Log.d(TAG, resultsList.toString())


        var allPermissionsGrantedTracker = true

        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }

        if (allPermissionsGrantedTracker == true) {
//            var snackbar = Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG)
//            snackbar.show()

            // TODO: Get the user's location from the device (GPS, Wifi, etc)

        } else {
//            var snackbar = Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG)
//            snackbar.show()
            // TODO: Output a rationale for why we need permissions
            // TODO: Disable the get current location button so they can't accidently click on
            //handlePermissionDenied()
        }
    }

    fun getDeviceLocation()  {
        // helper function to get device location
        // Before running fusedLocationClient.lastLocation, CHECK that the user gave you permission for FINE_LOCATION and COARSE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Usually, an app will ask again for permission (ask a second time)
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }


        // if YES, then go get the location, everything is fine ;)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            // Got last known location. In some rare situations this can be null.
            if (location == null) {
                Log.d(TAG, "Location is null")
                return@addOnSuccessListener
            }


            // Output the location
            getDeviceCity(location.latitude, location.longitude)
            val message = "The device is located at: ${location.latitude}, ${location.longitude}"
            Log.d(TAG, "$message location Object: $location")
        }

    }
    fun getDeviceCity(lat: Double , long: Double) {
        val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())

        Log.d(TAG, "Getting address for $lat, $long")

        // 3. try to find a matching street address
        try {
            val searchResults:MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
            if (searchResults == null) {
                Log.e(TAG, "getting Street Address: searchResults is NULL ")
                return
            }

            if (searchResults.size ==0) {
                Log.d(TAG, "Search results <= 0")
            } else {
                // 3. get the result
                val matchingAddress: Address = searchResults.get(0)

                // output this information to the UI
                runOnUiThread{

                    binding.etAddress.setText("${matchingAddress.getAddressLine(0)}")
                }
            }


        } catch(ex:Exception) {
            Log.e(TAG, "Error encountered while getting coordinate location.")
            Log.e(TAG, ex.toString())
        }
    }

    fun getAddress (lat: Double, long: Double) :String{
        val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())

        Log.d(TAG, "Getting address for $lat, $long")

        // 3. try to find a matching street address
        try {
            val searchResults:MutableList<Address>? = geocoder.getFromLocation(lat, long, 1)
            if (searchResults == null) {
                Log.e(TAG, "getting Street Address: searchResults is NULL ")
                return "Unknown Location"
            }

            if (searchResults.size ==0) {
                Log.d(TAG, "Search results <= 0")
            } else {
                // 3. get the result
                val matchingAddress: Address = searchResults.get(0)

                // output this information to the UI
                return "${matchingAddress.getAddressLine(0)}"
            }


        } catch(ex:Exception) {
            Log.e(TAG, "Error encountered while getting coordinate location.")
            Log.e(TAG, ex.toString())
        }
        return "Unknown Location"
    }

    fun getLatandLong (address:String) : Pair<Double,Double>?{
        val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())

        Log.d(TAG, "Getting coordinates for $address")

        // 3. try to find a matching street address
        try {
            val searchResults:MutableList<Address>? = geocoder.getFromLocationName(address, 1)
            if (searchResults == null) {
                Log.e(TAG, "getting Street Address: searchResults is NULL ")
                return null
            }

            if (searchResults.size ==0) {
                Log.d(TAG, "Search results <= 0")
            } else {
                // 3. get the result
                val matchingAddress: Address = searchResults.get(0)

                // output this information to the UI
                return Pair(matchingAddress.latitude, matchingAddress.longitude)
            }


        } catch(ex:Exception) {
            Log.e(TAG, "Error encountered while getting coordinate location.")
            Log.e(TAG, ex.toString())
        }
        return null
    }
}