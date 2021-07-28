package com.example.lingo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lingo.R
import com.example.lingo.firebase.FireStoreClass
import com.example.lingo.models.AvailableUser
import com.example.lingo.models.User
import com.example.lingo.utils.Constants
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_helper_start.*

class HelperStart : CommonActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLongitude: Double? = null
    private var mLatitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_start)

        setUpActionBar()
        rl_offline_button_helper.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.INTERNET
                ) == PackageManager.PERMISSION_GRANTED) {
                showProgressDialog("Getting location...")
                setLatLng()
                getUserData()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                    ),
                    Constants.REQUEST_LOCATION_AND_INTERNET_PERMISSION_CODE
                )
            }
        }
        rl_online_button_helper.setOnClickListener {
            showProgressDialog("Please wait...")
            getUserData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_LOCATION_AND_INTERNET_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        setLatLng()
                        getUserData()
                } else {
                    Toast.makeText(
                        this,
                        "We need internet access for uploading data to the server",
                        Toast.LENGTH_LONG
                    ).show()
                    hideProgressDialog()
                }
            } else {
                Toast.makeText(
                    this,
                    "We need your location data for the offline mode",
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        } else {
            Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(tb_helper_start)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_home_button_36dp)
        tb_helper_start.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun getUserData() {
        FireStoreClass().loadUserData(this)
    }

    fun makeUserOnline(user: User) {
        if (mLatitude != null && mLongitude != null) {
            val currentUser =
                AvailableUser(
                    user.id,
                    user.name,
                    user.phoneNumber,
                    user.image,
                    true,
                    mLatitude!!,
                    mLongitude!!
                )
            FireStoreClass().becomeAvailable(this, currentUser, Constants.OFFLINE_HELPERS)
        } else {
            val currentUser =
                AvailableUser(user.id, user.name, user.phoneNumber, user.image, true)
            FireStoreClass().becomeAvailable(this, currentUser, Constants.ONLINE_HELPERS)
        }
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val mLastLocation: Location = p0.lastLocation
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude
        }
    }

    @SuppressLint("MissingPermission")
    private fun setLatLng() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 3

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mLatitude = location.latitude
                mLongitude = location.longitude
            }
            Log.i("location", "$mLatitude and $mLongitude")
        }
    }

    fun afterDatabaseUpload() {
        hideProgressDialog()
        val intent = Intent(this, HelperSearchAndFound::class.java)
        if (mLatitude != null && mLongitude != null) {
            intent.putExtra(Constants.CURRENT_USER_LAT, mLatitude)
            intent.putExtra(Constants.CURRENT_USER_LNG, mLongitude)
        }
        startActivity(intent)
    }
}