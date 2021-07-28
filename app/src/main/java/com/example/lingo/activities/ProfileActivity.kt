package com.example.lingo.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.lingo.R
import com.example.lingo.firebase.FireStoreClass
import com.example.lingo.models.User
import com.example.lingo.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.io.IOException

class ProfileActivity : CommonActivity() {

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setUpActionBar()

        FireStoreClass().loadUserData(this)

        iv_user_image_update.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update_profile.setOnClickListener {
            showProgressDialog("Please wait...")
            if (mSelectedFileUri != null) {
                uploadUserImage(FireStoreClass().getCurrentUserID())
            } else {
                updateUserProfileData()
            }
            hideProgressDialog()
        }

        btn_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Log.v("reached code", "this code is reached")
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                Toast.makeText(
                    this,
                    "The read permissions are required for this feature to work. Please enable them in settings!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun uploadUserImage(id: String) {
        if (mSelectedFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_PROFILE_IMAGE_${id}.${
                    getFileExtension(mSelectedFileUri)
                }"
            )
            sRef.putFile(mSelectedFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "firebase image url",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("image url", uri.toString())
                    mProfileImageURL = uri.toString()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
        updateUserProfileData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.lingo_logo)
                    .into(iv_user_image_update)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun setUpActionBar() {
        setSupportActionBar(tb_profile_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        supportActionBar!!.title = "Profile"
        tb_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.lingo_logo)
            .into(iv_user_image_update)

        et_name_update.setText(user.name)
        et_email_update.setText(user.email)
        et_mobile_update.setText(user.phoneNumber)
        mProfileImageURL = user.image
        Log.e("profile image", mProfileImageURL!!)

    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        var changesMade = false

        Log.e("user image", mUserDetails.image)

        if (mProfileImageURL!!.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            Log.e("reached", "this code has been reached")
            userHashMap[Constants.IMAGE] = mProfileImageURL!!
            changesMade = true
        }

        if (et_name_update.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name_update.text.toString()
            changesMade = true
        }

        if (et_mobile_update.text.toString() != mUserDetails.phoneNumber) {
            userHashMap[Constants.PHONE_NUMBER] = et_mobile_update.text.toString()
            changesMade = true
        }
        if (changesMade) FireStoreClass().updateUserProfileData(this, userHashMap)

        if (et_password_update_profile.text.toString().isNotEmpty()) {
            if (et_password_update_profile.text.toString().length > 7) {
                if (et_password_update_profile == et_password_update_confirm_profile) {
                    FireStoreClass().updateUserPassword(et_password_update_profile.text.toString())
                } else {
                    showErrorSnackBar("Check if passwords match")
                }
            } else {
                showErrorSnackBar("Password should be over 8 characters long")
            }
        }
    }


    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        finish()
    }
}

