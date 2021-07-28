package com.example.lingo.activities

import android.os.Bundle
import com.example.lingo.R
import com.example.lingo.models.AvailableUser
import com.example.lingo.utils.Constants
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.maps.route.extensions.drawMarker
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import com.maps.route.model.TravelMode
import kotlinx.android.synthetic.main.activity_helper_map.*

class HelperMapActivity : CommonActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var mLatitude: Double? = null
    private var mLongitude: Double? = null
    private var mAssignedHelpee: AvailableUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_map)

        mLatitude = intent.getDoubleExtra(Constants.CURRENT_USER_LAT, 9999.9)
        mLongitude = intent.getDoubleExtra(Constants.CURRENT_USER_LNG, 9999.9)
        mAssignedHelpee = intent.getParcelableExtra(Constants.SEND_DETAILS_TO_MAP)

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.direction_to_helpee_map) as SupportMapFragment

        supportMapFragment.getMapAsync(this)
        re_center.bringToFront()

    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0

        val source = LatLng(mLatitude!!, mLongitude!!) //starting point (LatLng)
        val destination =
            LatLng(mAssignedHelpee!!.latitude, mAssignedHelpee!!.longitude) // ending point (LatLng)

        //if you only want the Estimates (Distance & Time of arrival)
        /*getTravelEstimations(
            mapsApiKey = getString(R.string.api_key),
            source = source,
            destination = destination
        ) { estimates ->
            estimates?.let {
                //Google Estimated time of arrival
                Log.d(TAG, "ETA: ${it.duration?.text}, ${it.duration?.value}")
                //Google suggested path distance
                Log.d(TAG, "Distance: ${it.distance?.text}, ${it.distance?.text}")

            } ?: Log.e(TAG, "Nothing found")
        }*/

        re_center.setOnClickListener {
            googleMap?.run {
                moveCameraOnMap(latLng = source)
            }
        }

        googleMap?.run {
            //if you want to move the map on specific location
            moveCameraOnMap(latLng = source)

            //if you want to drop a marker of maps, call it
            drawMarker(location = source, context = this@HelperMapActivity, title = "You")
            drawMarker(
                location = destination,
                context = this@HelperMapActivity,
                title = "Helpee"
            )

            //if you only want to draw a route on maps
            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            drawRouteOnMap(
                getString(R.string.api_key),
                source = source,
                destination = destination,
                context = this@HelperMapActivity,
                travelMode = TravelMode.WALKING,
                markers = false,
                boundMarkers = false,
            )

            //if you only want to draw a route on maps and also need the ETAs
            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            /*drawRouteOnMap(
                getString(R.string.api_key),
                source = source,
                destination = destination,
                context = this@HelperMapActivity,
                travelMode = TravelMode.WALKING
            ) { estimates ->
                estimates?.let {
                    //Google Estimated time of arrival
                    Log.d(TAG, "ETA: ${it.duration?.text}, ${it.duration?.value}")
                    //Google suggested path distance
                    Log.d(TAG, "Distance: ${it.distance?.text}, ${it.distance?.text}")

                } ?: Log.e(TAG, "Nothing found")
            }*/
        }
    }

    companion object {
        var TAG = HelperMapActivity::javaClass.name
    }
}