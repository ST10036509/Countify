/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 20/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.user_interface.account.LoginFragment
import st10036509.countify.utils.ProgressDialogFragment

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

    // method to delete the user's account and navigate to the login fragment
    fun deleteUserAccount(activity: FragmentActivity) {
        val currentUser = FirebaseAuthService.getCurrentUser()
        val userId = currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        //show the loading dialog
        val progressDialog = ProgressDialogFragment()
        progressDialog.show(activity.supportFragmentManager, "LoadingDialog")

        if (currentUser != null && userId != null) {
            //step 1: delete all user's counters in the counters collection
            db.collection("counters").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = db.batch()

                    //queue all documents for deletion
                    for (document in querySnapshot.documents) {
                        batch.delete(document.reference)
                    }

                    //commit the batched deletions
                    batch.commit().addOnCompleteListener { batchTask ->
                        if (batchTask.isSuccessful) {
                            Log.d("Firestore Deletion", "All counters deleted successfully")

                            //step 2: delete the user document from Firestore
                            db.collection("users").document(userId).delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("Firestore Deletion", "User document deleted from Firestore")

                                        //step 3: delete the user from Firebase Authentication
                                        currentUser.delete().addOnCompleteListener { authTask ->
                                            //dismiss the loading dialog
                                            progressDialog.dismiss()

                                            if (authTask.isSuccessful) {
                                                Log.d("Firestore Deletion", "User account deleted from Firebase Authentication")

                                                //navigate to login page after deletion
                                                NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
                                            } else {
                                                Log.e("Firestore Deletion", "Failed to delete user from Firebase Auth: ${authTask.exception}")
                                            }
                                        }
                                    } else {
                                        Log.e("Firestore Deletion", "Failed to delete user document: ${task.exception}")
                                        progressDialog.dismiss() //dismiss if there's an error
                                    }
                                }
                        } else {
                            Log.e("Firestore Deletion", "Failed to delete counters: ${batchTask.exception}")
                            progressDialog.dismiss() //dismiss if there's an error
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore Deletion", "Failed to retrieve counters for deletion: $exception")
                    progressDialog.dismiss() //dismiss if there's an error
                }
        } else {
            Log.e("Firestore Deletion", "No authenticated user found for deletion")
            progressDialog.dismiss() //dismiss if there's an error
        }
    }


    // method to get the currently logged in user, returns null if no user is logged in
    fun getCurrentUser(): FirebaseUser? {
        Log.i("Firebase Authentication Service:", "Getting Current User If Applicable")
        return auth.currentUser // return the current user or null
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________