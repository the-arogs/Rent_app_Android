package com.rg.rentapp.screens.tenant

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
import com.rg.rentapp.adapters.ShortListAdaptor
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.classes.User
import com.rg.rentapp.databinding.ActivityShortListBinding
import com.rg.rentapp.repositories.ShortlistRepository
import com.rg.rentapp.screens.guest.MainActivity
import com.rg.rentapp.screens.owner.AddRentalActivity
import com.rg.rentapp.screens.owner.OwnedPropertyList

class ShortListActivity : AppCompatActivity() {

    private var tag = "ShortListActivity"

    private lateinit var binding: ActivityShortListBinding
    private lateinit var shortlistRepository : ShortlistRepository
    private lateinit var shortlist: ArrayList<Rental>
    lateinit var adapter:ShortListAdaptor

    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()

        shortlistRepository = ShortlistRepository(applicationContext)
        shortlist = ArrayList()

        adapter = ShortListAdaptor( shortlist,
            {pos -> removeFromShortlist(pos)},
            {pos-> openDetailsActivity(pos)})
        binding.rvShortlist.adapter = adapter
        binding.rvShortlist.layoutManager = LinearLayoutManager(this)
        // adding a line between each item in the list
//        binding.rvShortlist.addItemDecoration(
//            DividerItemDecoration(
//                this,
//                LinearLayoutManager.VERTICAL
//            )
//        )
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

    override fun onResume() {
        super.onResume()
        val snackbar = Snackbar.make(binding.rootLayout, "Fetching Shortlist", Snackbar.LENGTH_LONG)
        snackbar.show()
        shortlistRepository.retrieveShortlistArray()
        shortlistRepository.shortlistArray.observe(this) { shortlistArray ->
            if (shortlistArray != null) {
                shortlistRepository.retrieveShortlist(shortlistArray)
                shortlistRepository.shortlist.observe(this) { shortlist ->
                    if (shortlist != null) {
                        this.shortlist.clear()
                        this.shortlist.addAll(shortlist)
                        adapter.notifyDataSetChanged()
                        Log.d(tag, "shortlist: $shortlist")
                    }
                }

                //shortlist.addAll(shortlistRepository.shortlist)


            }
        }

    }

    fun removeFromShortlist(position:Int) {

        val snackbar = Snackbar.make(binding.rootLayout, "Removing From Shortlist", Snackbar.LENGTH_LONG)
        snackbar.show()


        val selectedRental = this.shortlist[position]
        shortlistRepository.removeRental(selectedRental.rentalID.toString())

    }

    fun openDetailsActivity(position:Int) {
        val selectedRental = this.shortlist[position]
        var intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("RENTAL_OBJ", selectedRental)
        startActivity(intent)
    }


}