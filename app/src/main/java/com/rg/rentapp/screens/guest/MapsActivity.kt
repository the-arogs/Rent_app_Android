package com.rg.rentapp.screens.guest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rg.rentapp.R
import com.rg.rentapp.classes.LocationHelper
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.databinding.ActivityMapsBinding
import com.rg.rentapp.enums.PropertyTypes
import com.rg.rentapp.screens.tenant.DetailsActivity

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var currentLocation: LatLng
    private lateinit var locationCallback: LocationCallback
    var objString:String? = "No map stuff"
    private var searchArrayList:ArrayList<Rental> = arrayListOf()
    var indexToOpen:Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        var currentIntent = this@MapsActivity.intent

        if (currentIntent != null) {
            objString = currentIntent.getStringExtra("MARKER_LIST")
            val gson = Gson()
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            var rentalList = gson.fromJson<MutableList<Rental>>(objString, typeToken)
            if (rentalList != null) {
                searchArrayList.addAll(rentalList)
            }

            Log.d("MApList", "$rentalList")
        } else {
            Log.d("MApList", "$")
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        currentLocation = LatLng(-43.65059181173081, 79.37120405429074)
        locationHelper = LocationHelper.instance
        locationHelper.checkPermissions(this)
        if (locationHelper.locationPermissionGranted) {
            this.locationCallback = object : LocationCallback() {
                override fun onLocationResult(location: LocationResult) {
                    // super.onLocationResult(p0)
                    for (loc in location.locations) {
                        currentLocation = LatLng(loc.latitude, loc.longitude)
                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 8.0f))
                        mMap.uiSettings.setZoomControlsEnabled(true)
                        // mMap.addMarker(MarkerOptions().position(currentLocation).title("You're Here"))
                    }
                }

            }

            locationHelper.requestLocationUpdates(this, locationCallback)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.auth_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.home -> {
                finish()
//                var intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onPause() {
        super.onPause()
        this.locationHelper.stopLocationUpdates(this, locationCallback)
    }

    override fun onResume() {
        super.onResume()
        this.locationHelper.requestLocationUpdates(this, locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        lateinit var mapFocus: LatLng
        if (searchArrayList != null) {
            mapFocus = LatLng(searchArrayList[0].lat, searchArrayList[0].long )
        }else {
            mapFocus = LatLng(currentLocation.latitude, currentLocation.longitude)
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapFocus, 8.0f))
        // mMap.addMarker(MarkerOptions().position(this.currentLocation).title("You're Here").visible(true))
        if (searchArrayList != null) {
            for (rental in searchArrayList) {
                var rentalCoordinates = LatLng(rental.lat, rental.long)
                var displayInfo = "$${rental.price} - ${rental.address}"

                mMap.addMarker(MarkerOptions().position(rentalCoordinates).title(displayInfo))?.showInfoWindow()
            }
        }

        mMap.setOnMarkerClickListener(this)

    }

    override fun onMarkerClick(marker: Marker): Boolean {

        if (searchArrayList != null) {
            for (rental in searchArrayList) {
                if (rental.lat == marker.position.latitude) {
                    var intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("RENTAL_OBJ", rental)
                    startActivity(intent)
                }
            }


        } else {
            var rentalObj = Rental(
                PropertyTypes.NONE, "N/A", mutableListOf(),
                0.0, "N/A", "N/A", 0.0, 0.0, "", "")


            var intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("RENTAL_OBJ", rentalObj)
            startActivity(intent)
        }



        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }
}