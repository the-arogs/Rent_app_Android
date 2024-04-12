package com.rg.rentapp.classes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper private constructor(){

    private val TAG = "LocationHelper"
    var locationPermissionGranted = false
    val REQUEST_LOCATION_CODE = 101
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest
    private val UPDATE_INTERVAL_IN_MILLISECONDS = 5000
    var currLocation = MutableLiveData<Location>()

    companion object{
        val instance = LocationHelper()
    }

    init {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_IN_MILLISECONDS.toLong()).build()
    }

    private fun hasFineLocationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            return false
        }
    }

    private fun hasCoarseLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context.applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun checkPermissions(context: Context) {
        if (hasFineLocationPermission(context) == true && hasCoarseLocationPermission(context) == true) {
            this.locationPermissionGranted = true
        }
        Log.d(TAG, "checkPermissions: Are location permissions granted? : " + this.locationPermissionGranted)

        if (this.locationPermissionGranted == false) {
            Log.d(TAG, "Permissions not granted, so requesting permission now...")
            requestLocationPermission(context)
        }
    }

    fun requestLocationPermission(context: Context) {
        val listOfRequiredPermission
                = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(
            (context as Activity),
            listOfRequiredPermission,
            REQUEST_LOCATION_CODE)
    }

    fun getFusedLocationProviderClient(context: Context): FusedLocationProviderClient {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        }

        return fusedLocationProviderClient!!
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(context: Context, locationCallback: LocationCallback) {
        if (locationPermissionGranted) {
            try {
                this.getFusedLocationProviderClient(context).requestLocationUpdates(this.locationRequest, locationCallback, Looper.getMainLooper())
            } catch (ex: Exception) {
                Log.d(TAG, "stopLocationUpdates: Exception occurred while receiving location updates " + ex.localizedMessage)
            }
        } else {
            Log.d(TAG, "requestLocationUpdates: No Permission.. No Location updates")
        }
    }

    fun stopLocationUpdates(context: Context, locationCallback: LocationCallback) {
        try {
            this.getFusedLocationProviderClient(context).removeLocationUpdates(locationCallback)
        } catch (ex: Exception) {
            Log.e(TAG, "stopLocationUpdates: Exception occurred while stopping location updates " + ex.localizedMessage)
        }
    }




}