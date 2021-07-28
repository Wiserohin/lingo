package com.example.lingo.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.lingo.activities.*
import com.example.lingo.models.AvailableUser
import com.example.lingo.models.User
import com.example.lingo.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.lang.IndexOutOfBoundsException

class FireStoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error $e")
            }
    }

    fun loadUserData(activity: Activity) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .get().addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when (activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) activity.signInSuccess(loggedInUser)
                    }
                    is ProfileActivity -> {
                        if (loggedInUser != null) activity.setUserDataInUI(loggedInUser)
                    }
                    is HelpeeStart -> {
                        if (loggedInUser != null) activity.makeUserOnline(loggedInUser)
                    }
                    is HelperStart -> {
                        if (loggedInUser != null) activity.makeUserOnline(loggedInUser)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error $e")
            }
    }

    fun updateUserProfileData(activity: ProfileActivity, userHashmap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .update(userHashmap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Profile data updated")
                Toast.makeText(activity, "Profile update successfully", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener { e ->
                Log.e(activity.javaClass.name, "Error in updation = $e")
                Toast.makeText(activity, "Profile update failed", Toast.LENGTH_LONG).show()
            }
    }

    fun updateUserPassword(newPassword: String) {
        val currentUser = Firebase.auth.currentUser
        currentUser!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("password update", "User password updated.")
                }
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = Firebase.auth.currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun becomeAvailable(
        activity: Activity,
        availableUserInfo: AvailableUser,
        collection: String
    ) {
        mFireStore.collection(collection).document(getCurrentUserID())
            .set(availableUserInfo, SetOptions.merge()).addOnSuccessListener {
                when (activity) {
                    is HelpeeStart -> activity.afterDatabaseUpload()
                    is HelperStart -> activity.afterDatabaseUpload()
                }
            }.addOnFailureListener { e ->
                Log.e("become available 1", e.toString())
            }
    }

    fun searchDatabase(activity: Activity, collection: String) {
        mFireStore.collection(collection).whereEqualTo(Constants.AVAILABLE_USER, true).get()
            .addOnSuccessListener { documents ->
                val availableHelpees = ArrayList<AvailableUser>()
                for (document in documents){
                    availableHelpees.add(document.toObject(AvailableUser::class.java))
                }
                Log.e("list", availableHelpees.size.toString())
                try {
                    when (activity) {
                        is HelperSearchAndFound -> {
                            if (collection == Constants.OFFLINE_HELPEES) activity.checkClosestHelpee(
                                availableHelpees
                            )
                            else activity.setOnlineHelpee(availableHelpees[0])
                        }
                    }
                } catch (e: IndexOutOfBoundsException){
                    e.printStackTrace()
                    searchDatabase(activity, collection)
                }
            }
    }

    fun deleteEntry(activity: Activity, collection: String, idToDelete: String){
        mFireStore.collection(collection).document(idToDelete).delete().addOnSuccessListener {
            if(activity is HelperSearchAndFound) activity.showHelpeeDetails()
        }.addOnFailureListener {
            Log.e("deletion", "deletion failed because $it")
        }
    }

}