package st10036509.countify.service

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import st10036509.countify.R

object FirebaseAuthService {

    // reference to the Firebase Authentication API
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // method to register a new user with a given email and password or handle errors
    fun registerUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    // method to verify the user credentials and log them in or handle errors
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    // method to handle lunching the google sign in UI and getting the selected account ID token
    fun googleSignIn(activity: Activity, resultLauncher: ActivityResultLauncher<Intent>) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))  // Ensure you've added your web client ID in strings.xml
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    // method to handle the authentication of the selected google account token from the UI
    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null)
            } else {
                onComplete(false, task.exception?.message)
            }
        }
    }

    // method to sign the user out
    fun logout() {
        auth.signOut()
    }

    // method to get the currently logged in user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}