package com.rg.rentapp.screens.owner

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rg.rentapp.R
import com.rg.rentapp.adapters.PropertyListAdaptor
import com.rg.rentapp.adapters.ShortListAdaptor
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.classes.User
import com.rg.rentapp.databinding.ActivityOwnedPropertyListBinding
import com.rg.rentapp.databinding.ActivityShortListBinding
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.repositories.RentalRepository
import com.rg.rentapp.screens.guest.MainActivity
import com.rg.rentapp.screens.tenant.DetailsActivity

class OwnedPropertyList : AppCompatActivity() {
    private lateinit var binding: ActivityOwnedPropertyListBinding
    var ownedPropertyList:MutableList<Rental> = mutableListOf()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    lateinit var rentalRepository: RentalRepository

    lateinit var adapter:PropertyListAdaptor
    var TAG = "OwnedProperty List"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnedPropertyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()

        rentalRepository = RentalRepository(this)
        rentalRepository.retrieveAllOwnerRentals()
        rentalRepository.allOwnerRentals.observe(this) { rentalList ->
            if (rentalList != null) {
                adapter.submitList(rentalList)
                ownedPropertyList.addAll(rentalList)
                Log.d(TAG, "onCreate: owned properties in observe call $ownedPropertyList")

            }
        }
        adapter = PropertyListAdaptor(
            {pos -> removeFromShortlist(pos)},
            {pos-> openDetailsActivity(pos)},
            {pos-> openDetailsActivity2(pos)})

        binding.rvPropertyList.adapter = adapter
        binding.rvPropertyList.layoutManager = LinearLayoutManager(this)
        // adding a line between each item in the list
        binding.rvPropertyList.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
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

    override fun onResume() {
        super.onResume()

    }


    fun removeFromShortlist(position:Int) {
        Log.d(TAG, "onCreate: owned properties before removing $ownedPropertyList")

        rentalRepository.deleteRental(ownedPropertyList[position])
        rentalRepository.deleteOwnerRental(ownedPropertyList[position])
        ownedPropertyList.removeAt(position)
        adapter.notifyDataSetChanged()

        val snackbar = Snackbar.make(binding.rootLayout, "Rental removed", Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    fun openDetailsActivity(position: Int) {
        var selectedRental = ownedPropertyList.get(position)

        var intent = Intent(this, UpdateRental::class.java)
        intent.putExtra("RENTAL_OBJ", selectedRental)

        startActivity(intent)
    }

    fun openDetailsActivity2(position: Int) {
        var selectedRental = ownedPropertyList.get(position)

        var intent = Intent(this, OwnerDetailsActivity::class.java)
        intent.putExtra("RENTAL_OBJ", selectedRental)

        startActivity(intent)
    }
}