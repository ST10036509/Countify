/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 25/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel

// class to handle Google account sign-in functionality
class GoogleAccountService(private val context: Context) {

    // reference to Google SignIn client
    private lateinit var googleSignInClient: GoogleSignInClient

    // setup Google sign-in options with client id
    fun setupGoogleSignIn(clientId: String) {
        Log.i("Google Account Service:", "")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .requestProfile()
            .build()

        Log.i("Google Account Service:", "Google Sign In Options Set. Initialising Google Sign On Client With Options")

        // initialize GoogleSignInClient with the configured options
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // method to launch Google sign-in process
    fun launchSignIn(resultsLauncher: ActivityResultLauncher<Intent>) {
        Log.i("Google Account Service", "Launching Google Sign in Prompt")
        googleSignInClient.signOut().addOnCompleteListener {
            // start Google sign-in activity
            val signInIntent = googleSignInClient.signInIntent
            resultsLauncher.launch(signInIntent)
        }
    }

    // handle the result of Google sign-in and authenticate with Firebase
    fun handleSignInResult(resultCode: Int, data: Intent?, onSuccess: (FirebaseUser) -> Unit, onFailure: (String) -> Unit) {
        Log.i("Google Account Service", "Handling Google Sign In Request")
        if (resultCode == Activity.RESULT_OK) {
            Log.i("Google Account Service", "Google Account Token Retrieval Successful")
            // retrieve the Google sign-in account
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // get Google sign-in result and authenticate with Firebase using idToken
                val account = task.getResult(ApiException::class.java)
                Log.i("Google Account Service", "Google Account & Data Successfully Pulled")
                firebaseAuthWithGoogle(account.idToken!!, onSuccess, onFailure)
            } catch (e: ApiException) {
                // handle sign-in failure
                Log.i("Google Account Service", "Google Sign In Failed - ${e.message.toString()}")
                onFailure(e.message.toString())
            }
        }
    }

    // method to authenticate user with Firebase using Google sign-in credentials
    private fun firebaseAuthWithGoogle(idToken: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (String) -> Unit) {
        Log.i("Google Account Service", "Authenticating Google Account With Firebase")
        // create a Firebase credential using the Google idToken
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.i("Google Account Service", "Account Credentials Captured. Signing In With Credentials...")
        // sign in to Firebase using the credential
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                Log.i("Google Account Service", "Google Sign In Successful")
                if (task.isSuccessful) {
                    // if sign-in is successful, return the authenticated user
                    val user = Firebase.auth.currentUser
                    if (user != null) {
                        onSuccess(user)
                    }
                } else {
                    Log.i("Google Account Service", "Google Sign In Failed")
                    // if sign-in fails, return an error message
                    onFailure("Sign In Failed")
                }
            }
    }

    // method to check if the user exists in Firestore, and if not, create a new user document
    fun checkIfUserExistsInFirestore(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        Log.i("Google Account Service", "Checking If User Exists In Firestore")

        // check if user document exists in Firestore
        FirestoreService.getUserDocument(user.uid) { isDocumentRetrieved, empty ->
            Log.i("Google Account Service", "Attempting to Pull User Document for Google Account Id")

            if (!isDocumentRetrieved) {
                Log.i("Google Account Service", "User Document Does Not Exist. Creating New User Document...")

                // if the document does not exist, create a new user model
                val account = GoogleSignIn.getLastSignedInAccount(context)

                val newUser = UserModel(
                    userId = user.uid,
                    firstName = account?.givenName.toString(),
                    lastName = account?.familyName.toString(),
                    email = account?.email.toString(),
                    notificationsEnabled = true,
                    darkModeEnabled = false,
                    language = 0,
                    counters = emptyList()
                )
                Log.i("Google Account Service", "User Model Created From Google Account Details")

                // add the new user document to Firestore
                FirestoreService.addUserDocument(user.uid, newUser) { isDocumentAdded, documentError ->
                    Log.i("Google Account Service", "Adding User Model as a Document to the Database")
                    if (isDocumentAdded) {
                        Log.i("Google Account Service", "Document Creation Process Successful. UserManager Model Updated")
                        // store the new user in UserManager and return success
                        UserManager.currentUser = newUser
                        onSuccess()
                    } else {
                        Log.i("Google Account Service", "Document Creation Process Failed.")
                        // return failure message if adding the document failed
                        onFailure(documentError.toString())
                    }
                }
            } else {
                Log.i("Google Account Service", "User Document Already Exist")
                // if document already exists, return success
                onSuccess()
            }
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________