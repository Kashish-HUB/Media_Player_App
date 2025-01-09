package com.example.mediaplayer

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserDob: TextView
    private lateinit var tvGender: TextView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_profile)

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserDob = findViewById(R.id.tvUserDob)
        tvGender = findViewById(R.id.tvGender)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)

        // Initialize SharedPreferences to retrieve saved data
        sharedPreferences = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE)

        // Retrieve saved profile data from SharedPreferences
        val userName = sharedPreferences.getString("userName", "N/A")
        val userEmail = sharedPreferences.getString("userEmail", "N/A")
        val userPhone = sharedPreferences.getString("userPhone", "N/A")
        val userDob = sharedPreferences.getString("userDob", "N/A")
        val gender = sharedPreferences.getString("gender", "Not Specified")
        val profilePictureUriString = sharedPreferences.getString("profilePictureUri", null)

        // Set the retrieved data to the TextViews
        tvUserName.text = "Name: $userName"
        tvUserEmail.text = "Email: $userEmail"
        tvUserPhone.text = "Phone: $userPhone"
        tvUserDob.text = "DOB: $userDob"
        tvGender.text = "Gender: $gender"

        // Display the profile picture if it's available
        profilePictureUriString?.let {
            val profilePictureUri = Uri.parse(it) // Convert the string back to Uri
            ivProfilePicture.setImageURI(profilePictureUri) // Display the image
        }
    }
}
