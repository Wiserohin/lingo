package com.example.lingo.activities

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.lingo.R
import com.example.lingo.firebase.FireStoreClass
import com.example.lingo.models.AvailableUser
import com.example.lingo.utils.Constants
import kotlinx.android.synthetic.main.activity_helper_search_and_found.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL

class HelperSearchAndFound : CommonActivity() {

    private var mClosestHelpeeTime: Int? = null
    private var mLatitude: Double? = null
    private var mLongitude: Double? = null
    private var mClosestHelpee: AvailableUser? = null
    private var mFirstOnlineHelpee: AvailableUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_search_and_found)

        helper_search_ripple.startRippleAnimation()
        mLatitude = intent.getDoubleExtra(Constants.CURRENT_USER_LAT, 9999.9)
        mLongitude = intent.getDoubleExtra(Constants.CURRENT_USER_LNG, 9999.9)
        if (mLatitude == 9999.9 || mLongitude == 9999.9) {
            mLatitude = null
            mLongitude = null
            FireStoreClass().searchDatabase(this, Constants.ONLINE_HELPEES)
        } else {
            FireStoreClass().searchDatabase(this, Constants.OFFLINE_HELPEES)
        }

        found_helpee_contact.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:${mClosestHelpee?.phoneNumber?.toInt()}")
            startActivity(callIntent)
        }

    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = false
            super.onBackPressed()
            FireStoreClass().deleteEntry(this, Constants.OFFLINE_HELPERS, getCurrentUserID())
            finish()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click again to go back", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)

    }

    fun checkClosestHelpee(helpeeList: ArrayList<AvailableUser>) {
        for (helpee in helpeeList) {
            val currentHelpeeTime = getTravelTime(helpee)
            Log.d("noticeable", currentHelpeeTime.toString())
            if (currentHelpeeTime == null || currentHelpeeTime == 0) continue
            if (mClosestHelpeeTime == null) {
                mClosestHelpeeTime = currentHelpeeTime
                mClosestHelpee = helpee
            } else {
                if (currentHelpeeTime < mClosestHelpeeTime!!) {
                    mClosestHelpeeTime = currentHelpeeTime
                    mClosestHelpee = helpee
                }
            }
        }
        if (mClosestHelpee != null) {
            found_helpee_name.text = mClosestHelpee?.name
            found_helpee_contact.text = mClosestHelpee?.phoneNumber
            found_helpee_contact.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            Glide
                .with(this)
                .load(mClosestHelpee!!.image)
                .centerCrop()
                .placeholder(R.drawable.logo_hq)
                .into(helpee_found_picture)

            updateDatabase()
        }
    }

    private fun getTravelTime(helpee: AvailableUser): Int? {
        var travelTime: Int?
        val requestURL =
            "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=$mLatitude,$mLongitude&destinations=${helpee.latitude},${helpee.longitude}&mode=walking&key=${
                getString(R.string.api_key)
            }"
        Log.d("request", requestURL)
        helper_search_ripple.stopRippleAnimation()
        runBlocking {
            helper_search_ripple.startRippleAnimation()
            travelTime = getDuration(requestURL)
        }
        Log.d("travel", travelTime.toString())
        return travelTime
    }

    private suspend fun getDuration(url: String): Int = withContext(Dispatchers.Default) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val jsonText =
            connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
        connection.disconnect()
        val jsonObject = JSONTokener(jsonText).nextValue() as JSONObject
        val result = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements")
            .getJSONObject(0).getJSONObject("duration").getInt("value")
        Log.d("result time", result.toString())
        return@withContext result
    }

    fun setOnlineHelpee(helpee: AvailableUser) {
        Log.e("helpee time", "online selected????")
        mFirstOnlineHelpee = helpee
        updateDatabase()
    }

    private fun updateDatabase() {
        if (mClosestHelpee != null) {
            FireStoreClass().deleteEntry(this, Constants.OFFLINE_HELPEES, mClosestHelpee!!.id)
            FireStoreClass().deleteEntry(this, Constants.OFFLINE_HELPERS, getCurrentUserID())
        } else {
            FireStoreClass().deleteEntry(this, Constants.ONLINE_HELPEES, mFirstOnlineHelpee!!.id)
            FireStoreClass().deleteEntry(this, Constants.ONLINE_HELPERS, getCurrentUserID())
        }
    }

    fun showHelpeeDetails() {
        helper_search_ripple.visibility = View.GONE
        tb_helpee_found.visibility = View.INVISIBLE
        cv_found_profile.visibility = View.VISIBLE
        helpee_found_heading.visibility = View.VISIBLE
        rl_button_set.visibility = View.VISIBLE
        if (mClosestHelpee != null) {
            btn_direction_to_helpee.setOnClickListener {
                val intent = Intent(this, HelperMapActivity::class.java)
                intent.putExtra(Constants.SEND_DETAILS_TO_MAP, mClosestHelpee)
                intent.putExtra(Constants.CURRENT_USER_LAT, mLatitude)
                intent.putExtra(Constants.CURRENT_USER_LNG, mLongitude)
                startActivity(intent)
            }
            btn_complete_help.setOnClickListener {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("isfrom", "helper")
                startActivity(intent)
            }
        }
    }

/*    private inner class getTravelTimeCoroutine(
        sourceLat: Double,
        sourceLng: Double,
        destLat: Double,
        destLng: Double
    ) : ViewModel() {
        var resultTime: Int? = null
        private lateinit var httpRequest: String

        fun execute(){
            onPreExecute()
        }

        private fun onPreExecute() {

        }
    }*/

}