package com.rg.rentapp.screens.login

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rg.rentapp.R
import com.rg.rentapp.classes.Owner
import com.rg.rentapp.databinding.ActivityLoginBinding
import com.rg.rentapp.databinding.ActivityProfileBinding
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.screens.guest.MainActivity
import com.rg.rentapp.screens.owner.AddRentalActivity
import com.rg.rentapp.screens.owner.OwnedPropertyList
import com.rg.rentapp.screens.tenant.ShortListActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity(){

    private lateinit var binding: ActivityProfileBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var auth : FirebaseAuth
    private lateinit var ownerRepository : OwnerRepository
    private var currUser : Owner = Owner()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        this.firebaseAuth = FirebaseAuth.getInstance()
        this.auth = Firebase.auth
        // binding.btnViewShortlist.setOnClickListener(this)
        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()
        val userEmail = sharedPreferences.getString("USER_EMAIL", "")
        ownerRepository = OwnerRepository(this)
        lifecycleScope.launch {
            currUser = ownerRepository.getOwnerFromDB(userEmail!!)
            binding.tvPhoneData.text = currUser.phoneNumber
            binding.tvEmailData.text = currUser.email
            binding.tvNameData.text = currUser.name
        }


//        binding.btnViewShortlist.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

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


    open override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.owner_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

//






    fun isLoggedIn():Boolean {
        val currUserFromSP = sharedPreferences.getString("USER_EMAIL", "")
        return currUserFromSP != ""
    }

    fun logout() {
        Firebase.auth.signOut()
        this.prefEditor.clear()

        this.prefEditor.apply()

        val snackbar = Snackbar.make(binding.root, "Logged out", Snackbar.LENGTH_LONG)
        snackbar.show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}





