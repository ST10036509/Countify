/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 20/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// singleton object for handling firebase authentication
object FirebaseAuthService {

    // reference to the firebase authentication instance
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // method to register a new user with email and password
    // onComplete is a callback that returns true if successful, false if there is an error
    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        Log.i("Firebase Authentication Service:", "Registering User")
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("Firebase Authentication Service:", "Registration Process Successful")
                onComplete(true, null) // registration successful
            } else {
                Log.i("Firebase Authentication Service:", "Registration Process Failed")
                onComplete(false, task.exception?.message) // registration failed, return error message
            }
        }
    }

    // method to log in a user with email and password
    // onComplete is a callback that returns true if successful, false if there is an error
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        Log.i("Firebase Authentication Service:", "Logging In User")
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("Firebase Authentication Service:", "Login Process Successful")
                onComplete(true, null) // login successful
            } else {
                Log.i("Firebase Authentication Service:", "Login Process Failed")
                onComplete(false, task.exception?.message) // login failed, return error message
            }
        }
    }

    // method to log out the current user
    fun logout() {
        Log.i("Firebase Authentication Service:", "Logging Out User")
        auth.signOut() // sign out from firebase
    }

    // method to get the currently logged in user, returns null if no user is logged in
    fun getCurrentUser(): FirebaseUser? {
        Log.i("Firebase Authentication Service:", "Getting Current User If Applicable")
        return auth.currentUser // return the current user or null
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________