package com.example.mediaplayer

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etUserPhone: EditText
    private lateinit var etUserDob: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var btnUploadPicture: Button
    private lateinit var btnSaveProfile: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ivProfilePicture: ImageView

    private val PICK_IMAGE_REQUEST = 1 // Request code for the image picker
    private var selectedImageUri: Uri? = null // To store the selected image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize views
        etUserName = findViewById(R.id.etUserName)
        etUserEmail = findViewById(R.id.etUserEmail)
        etUserPhone = findViewById(R.id.etUserPhone)
        etUserDob = findViewById(R.id.etUserDob)
        radioGender = findViewById(R.id.radioGender)
        btnUploadPicture = findViewById(R.id.btnUploadPicture)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)

        // Initialize SharedPreferences for saving data
        sharedPreferences = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE)

        // Load saved profile data if any
        loadProfileData()

        // Button click listener to save the profile
        btnSaveProfile.setOnClickListener {
            val userName = etUserName.text.toString()
            val userEmail = etUserEmail.text.toString()
            val userPhone = etUserPhone.text.toString()
            val userDob = etUserDob.text.toString()

            // Get gender
            val selectedGenderId = radioGender.checkedRadioButtonId
            var gender = ""
            when (selectedGenderId) {
                R.id.rbMale -> gender = "Male"
                R.id.rbFemale -> gender = "Female"
                R.id.rbOther -> gender = "Other"
            }

            // Save data to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("userName", userName)
            editor.putString("userEmail", userEmail)
            editor.putString("userPhone", userPhone)
            editor.putString("userDob", userDob)
            editor.putString("gender", gender)
            selectedImageUri?.let {
                editor.putString("profilePictureUri", it.toString()) // Save image URI
            }
            editor.apply()

            // Create an Intent to send data to the next Activity
            val intent = Intent(this, DisplayProfileActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("userPhone", userPhone)
            intent.putExtra("userDob", userDob)
            intent.putExtra("gender", gender)
            selectedImageUri?.let {
                intent.putExtra("profilePictureUri", it.toString())
            }

            // Start DisplayProfileActivity
            startActivity(intent)
        }

        // Handle Upload Picture button click
        btnUploadPicture.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK) // Use ACTION_PICK to select images
        intent.type = "image/*" // Restrict to image types
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the result from the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                // Display the selected image in the ImageView
                ivProfilePicture.setImageURI(it)
                this.selectedImageUri = it
            }
        }
    }

    private fun loadProfileData() {
        // Retrieve saved profile data from SharedPreferences
        val userName = sharedPreferences.getString("userName", "")
        val userEmail = sharedPreferences.getString("userEmail", "")
        val userPhone = sharedPreferences.getString("userPhone", "")
        val userDob = sharedPreferences.getString("userDob", "")
        val gender = sharedPreferences.getString("gender", "")
        val profilePictureUri = sharedPreferences.getString("profilePictureUri", null)

        // Load the data into views
        etUserName.setText(userName)
        etUserEmail.setText(userEmail)
        etUserPhone.setText(userPhone)
        etUserDob.setText(userDob)

        // Set gender RadioButton
        when (gender) {
            "Male" -> radioGender.check(R.id.rbMale)
            "Female" -> radioGender.check(R.id.rbFemale)
            "Other" -> radioGender.check(R.id.rbOther)
        }

        // Load the profile picture if available
        profilePictureUri?.let {
            ivProfilePicture.setImageURI(Uri.parse(it))
            selectedImageUri = Uri.parse(it)
        }
    }
}
