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
import kotlinx.android.synthetic.main.activity_helpee_start.*

class HelpeeStart : CommonActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLongitude: Double? = null
    private var mLatitude: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helpee_start)

        setUpActionBar()
        rl_offline_button_helpee.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showProgressDialog("Getting location...")
                setLatLng()
                setLatLng()
                getUserData()
                hideProgressDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Constants.REQUEST_LOCATION_PERMISSION_CODE
                )
            }
        }
        rl_online_button_helpee.setOnClickListener {
            showProgressDialog("Please wait...")
            getUserData()
            hideProgressDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLatLng()
                setLatLng()
                getUserData()
            } else {
                Toast.makeText(
                    this,
                    "We need your location data for the offline mode",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(tb_helpee_start)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_home_button_36dp)
        tb_helpee_start.setNavigationOnClickListener {
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
            FireStoreClass().becomeAvailable(this, currentUser, Constants.OFFLINE_HELPEES)
        } else {
            val currentUser =
                AvailableUser(user.id, user.name, user.phoneNumber, user.image, true)
            FireStoreClass().becomeAvailable(this, currentUser, Constants.ONLINE_HELPEES)
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
        mLocationRequest.numUpdates = 1

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!
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
        //startActivity(Intent)
    }

}