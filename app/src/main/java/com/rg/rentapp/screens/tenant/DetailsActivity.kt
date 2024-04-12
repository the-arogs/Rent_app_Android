package com.rg.rentapp.screens.tenant

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rg.rentapp.R
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.databinding.ActivityDetailsBinding
import com.rg.rentapp.enums.PropertyTypes
import com.rg.rentapp.screens.guest.MainActivity
import com.rg.rentapp.screens.owner.AddRentalActivity
import com.rg.rentapp.screens.owner.OwnedPropertyList


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var rentalObj: Rental

    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()

        var currentIntent = this@DetailsActivity.intent

        if (currentIntent != null) {
            rentalObj = currentIntent.getSerializableExtra("RENTAL_OBJ") as Rental
        } else {
            rentalObj = Rental(
                PropertyTypes.NONE, "N/A", mutableListOf(),
                0.0, "N/A", "N/A", 0.0,0.0,"")
        }

        binding.tvPriceData.text = rentalObj.price.toString()
        binding.tvPropertyTypeData.text = rentalObj.propertyType.toString()
        binding.tvAddressData.text = rentalObj.address
        var specOutput =""
        for (spec in rentalObj.specification) {
            specOutput += "$spec\n"
        }
        binding.tvSpecificationData.text = specOutput
        binding.tvDescriptionData.text = rentalObj.description
        binding.tvAvailabilityData.text = rentalObj.available

        binding.tvOwnerData.text = rentalObj.owner


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tenant_options, menu)
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
}