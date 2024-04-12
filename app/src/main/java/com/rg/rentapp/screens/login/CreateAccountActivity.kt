package com.rg.rentapp.screens.login

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rg.rentapp.R
import com.rg.rentapp.classes.Owner
import com.rg.rentapp.classes.Rental
import com.rg.rentapp.classes.Tenant
import com.rg.rentapp.classes.User
import com.rg.rentapp.databinding.ActivityCreateAccountBinding
import com.rg.rentapp.enums.UserTypes
import com.rg.rentapp.repositories.OwnerRepository
import com.rg.rentapp.screens.guest.MainActivity

class CreateAccountActivity : AppCompatActivity() {
    private val TAG = this.javaClass.canonicalName

    private lateinit var binding: ActivityCreateAccountBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private  lateinit var firebaseAuth : FirebaseAuth
    lateinit var ownerRepository: OwnerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.optionsMenu)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        sharedPreferences = getSharedPreferences("MY_APP_DB", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()
        this.firebaseAuth = FirebaseAuth.getInstance()
        this.ownerRepository = OwnerRepository(this)

        binding.btnCreateUser.setOnClickListener{
            validateData()
            Log.d(TAG, "onClick: Create Account button Clicked")
        }

        binding.btnLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

    private fun validateData() {
        var validData = true
        var email = ""
        var password = ""
        val phoneNumber = binding.etNumber.text.toString()
        val userType = findViewById<RadioButton>(binding.rg.checkedRadioButtonId).text.toString()
        val name = binding.etName.text.toString()

        if (binding.etEmail.getText().toString().isEmpty()) {
            binding.etEmail.setError("Email Cannot be Empty")
            validData = false
        } else {
            email = binding.etEmail.getText().toString()
        }

        if (binding.etPassword.getText().toString().isEmpty()) {
            binding.etPassword.setError("Password Cannot be Empty")
            validData = false
        } else {
            password = binding.etPassword.getText().toString()
        }

        if (validData) {
            createAccount(email, password, phoneNumber, userType, name)
        } else {
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount(email: String, password: String, phoneNumber: String, userType:String, name:String) {
//        SignUp using FirebaseAuth

        this.firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->

                if (task.isSuccessful){

                    //create user document with default profile info
                    if (userType == "Owner") {
                        val userToAdd = Owner(
                            email = email,
                            name = name,
                            phoneNumber = phoneNumber
                        )
                        ownerRepository.addOwnerToDB(userToAdd)
                    } else {
                        val userToAdd = Tenant(
                            name,
                            email,
                            phoneNumber,
                            password,
                            userType
                        )
                        ownerRepository.addTenantToDB(userToAdd)
                    }


                    Log.d(TAG, "createAccount: User account successfully create with email $email")
                    saveToPrefs(email, password, userType,name)
                    goToMain()
                }else{
                    Log.d(TAG, "createAccount: Unable to create user account : ${task.exception}", )
                    Toast.makeText(this@CreateAccountActivity, "Account creation failed", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun saveToPrefs(email: String, password: String, userType: String, name: String) {
        prefEditor.putString("USER_EMAIL", email)
        prefEditor.putBoolean("isLoggedIn", true)
        prefEditor.putString("USER_TYPE", userType)
        prefEditor.apply()
    }

    private fun goToMain() {
//        val mainIntent = Intent(this, MainActivity::class.java)
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }
}