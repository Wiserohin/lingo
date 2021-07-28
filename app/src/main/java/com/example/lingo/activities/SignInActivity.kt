package com.example.lingo.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.lingo.R
import com.example.lingo.firebase.FireStoreClass
import com.example.lingo.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : CommonActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth
        setUpActionBar()
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    fun signInSuccess(user: User){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setUpActionBar() {
        setSupportActionBar(tb_sign_in_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        tb_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    private fun signInRegisteredUser() {
        val email: String = et_email_signin.text.toString()
        val password: String = et_password_signin.text.toString()

        if (validateForm(email, password)) {
            showProgressDialog("Please wait...")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Log.d("sign in stuff", "signInWithEmail:success")
                        FireStoreClass().loadUserData(this)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("sign in stuff", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(
        email: String,
        password: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email")
                false
            }
            password.length < 8 -> {
                showErrorSnackBar("Your password should be over 7 characters")
                false
            }
            else -> true
        }
    }
}