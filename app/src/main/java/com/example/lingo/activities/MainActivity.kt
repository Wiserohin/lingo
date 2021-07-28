package com.example.lingo.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.lingo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.rating_dialog.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpActionBar()
        ib_to_profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        btn_helper_mode_main.setOnClickListener {
            startActivity(Intent(this, HelperStart::class.java))
        }
        btn_helpee_mode_main.setOnClickListener {
            startActivity(Intent(this, HelpeeStart::class.java))
        }
        if (intent.getStringExtra("isfrom").equals("helper")){
            showRatingDialog()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(tb_main_activity)
        supportActionBar!!.title = resources.getString(R.string.app_name)
    }

    private fun showRatingDialog(){
        val ratingDialog = Dialog(this)
        ratingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        ratingDialog.setCancelable(true)
        ratingDialog.setContentView(R.layout.rating_dialog)

        ratingDialog.submit_rating_button.setOnClickListener {
            Toast.makeText(this@MainActivity, "Thanks for rating the helpee.", Toast.LENGTH_SHORT).show()
            ratingDialog.dismiss()
        }
        ratingDialog.show()
    }
}