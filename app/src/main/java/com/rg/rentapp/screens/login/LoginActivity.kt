package com.rg.rentapp.screens.login

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rg.rentapp.R
import com.rg.rentapp.classes.User
import com.rg.rentapp.databinding.ActivityLoginBinding
import com.rg.rentapp.databinding.ActivityMainBinding
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.screens.guest.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var firebaseAuth : FirebaseAuth
    private val TAG = this.javaClass.canonicalName
    private lateinit var ownerRepository : OwnerRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        ownerRepository = OwnerRepository(this)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()

        binding.btnLogin.setOnClickListener(this)
        binding.btnCreateAccount.setOnClickListener(this)

        this.firebaseAuth = FirebaseAuth.getInstance()

        if (sharedPreferences.contains("USER_EMAIL")) {
            binding.etEmail.setText(this.sharedPreferences.getString("USER_EMAIL", ""))
        }
        if (sharedPreferences.contains("USER_PASSWORD")) {
            binding.etPassword.setText(this.sharedPreferences.getString("USER_PASSWORD", ""))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.auth_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.home -> {
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_login -> {
                // logging in
                // first get the list of users from sharedPreferences
                validateData()
            }

            R.id.btn_create_account -> {
                var intent = Intent(this, CreateAccountActivity::class.java)
                startActivity(intent)
            }
        }

    }



    private fun validateData() {
        var validData = true
        var email = ""
        var password = ""
        if (binding.etEmail.text.toString().isEmpty()) {
            binding.etEmail.error = "Email Cannot be Empty"
            validData = false
        } else {
            email = binding.etEmail.text.toString()
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Password Cannot be Empty"
            validData = false
        } else {
            password = binding.etPassword.text.toString()
        }
        if (validData) {
            signIn(email, password)
        } else {
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn(email: String, password: String) {
        //signIn using FirebaseAuth
        this.firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    Log.d(TAG, "signIn: Login successful email:$email, password: $password")

                    saveToPrefs( email , password)
                    lifecycleScope.launch {
                        val userType = ownerRepository.getUserType(email)
                        if (userType == "Tenant") {
                            goToMain()
                        } else {
                            goToProfile()
                        }
                    }

                }else{
                    Log.e(TAG, "signIn: Login Failed : ${task.exception}", )
                    Toast.makeText(this@LoginActivity,
                        "Authentication failed. Check the credentials",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveToPrefs(email: String, password: String) {

        prefEditor.putBoolean("isLoggedIn", true)
        prefEditor.putString("USER_EMAIL", email)
        prefEditor.apply()

    }

    private fun goToMain() {
//        val mainIntent = Intent(this, MainActivity::class.java)
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

    private fun goToProfile() {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        startActivity(profileIntent)
    }


}



//var output = ""
//for (currFruit in userList) {
//    output += "${currFruit}\n---------\n"
//}
//
//binding.tvTestResults.setText(output)


//val currUserFromSP = sharedPreferences.getString("CURR_USER", "")
//if (currUserFromSP != "") {
//    binding.tvTestResults.text = currUserFromSP
//}
