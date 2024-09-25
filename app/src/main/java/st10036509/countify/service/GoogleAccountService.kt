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


class GoogleAccountService(private val context: Context) {

    private lateinit var googleSignInClient: GoogleSignInClient

    fun setupGoogleSignIn(clientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun launchSignIn(resultsLauncher: ActivityResultLauncher<Intent>) {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            resultsLauncher.launch(signInIntent)
        }
    }

    fun handleSignInResult(resultCode: Int, data: Intent?, onSuccess: (FirebaseUser) -> Unit, onFailure: (String) -> Unit) {
        if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!, onSuccess, onFailure)
            } catch (e: ApiException) {
                onFailure(e.message.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    if (user != null) {
                        onSuccess(user)
                    }
                } else {
                    onFailure("Sign In Failed")
                }
            }
    }

    fun checkIfUserExistsInFirestore(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        FirestoreService.getUserDocument(user.uid) { isDocumentRetrieved,  empty ->
            if (!isDocumentRetrieved) {
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

                FirestoreService.addUserDocument(user.uid, newUser) { isDocumentAdded, documentError ->
                    if (isDocumentAdded) {
                        UserManager.currentUser = newUser
                        onSuccess()
                    } else {
                        onFailure(documentError.toString())
                    }
                }
            } else {
                    onSuccess()
            }
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________