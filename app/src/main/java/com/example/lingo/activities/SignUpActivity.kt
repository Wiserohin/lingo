package com.example.lingo.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.lingo.R
import com.example.lingo.firebase.FireStoreClass
import com.example.lingo.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : CommonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setUpActionBar()
        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(tb_sign_up_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        tb_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    private fun registerUser() {
        val name: String = et_name_signup.text.toString().trim { it <= ' ' }
        val email: String = et_email_signup.text.toString().trim { it <= ' ' }
        val phoneNumber: String = et_phone_number_signup.text.toString().trim { it <= ' ' }
        val password: String = et_password_signup.text.toString()
        val confirmPassword: String = et_confirm_password_signup.text.toString()

        if (validateForm(name, email, phoneNumber, password, confirmPassword)) {
            showProgressDialog("Please wait...")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail, phoneNumber)
                        FireStoreClass().registerUser(this, user)
                        userRegisteredSuccess()
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this,
            "you have registered",
            Toast.LENGTH_SHORT
        ).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(
        name: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email")
                false
            }
            TextUtils.isEmpty(phoneNumber) -> {
                showErrorSnackBar("Please enter a phone number")
                false
            }
            password.length < 8 -> {
                showErrorSnackBar("Please enter a password 8 characters or longer")
                false
            }
            password != confirmPassword -> {
                showErrorSnackBar("Passwords do not match")
                false
            }
            else -> true
        }
    }
}