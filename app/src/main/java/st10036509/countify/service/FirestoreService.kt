/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 20/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.model.CounterModel
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel

// singleton object to handle interactions with firestore
object FirestoreService {

    // get the instance of the firestore database
    private val db by lazy { FirebaseFirestore.getInstance() }

    // method to add a document to a specified collection in firestore
    private fun addDocument(collection: String, data: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        Log.i("Firestore Service:", "Fetching Document from Collection - $collection")
        db.collection(collection).add(data)
            .addOnSuccessListener {
                Log.i("Firestore Service:", "Document Fetch Process Successful")
                onComplete(true, null) // document successfully added
            }
            .addOnFailureListener { exception ->
                Log.i("Firestore Service:", "Document Fetch Process Failed")
                onComplete(false, exception.message) // failed to add document, return error message
            }
    }

    // method to fetch a specific user document from the "users" collection
    fun getUserDocument(userId: String, onComplete: (Boolean, String?) -> Unit) {
        Log.i("Firestore Service:", "Fetching User Document from Collection - users")
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                Log.i("Firestore Service:", "Document Fetch Process Successful")
                if (document != null && document.exists()) {
                    Log.i("Firestore Service:", "User Document Found")
                    // if document exists, convert it to a UserModel object
                    val userModel = document.toObject(UserModel::class.java)
                    if (userModel != null) {
                        Log.i("Firestore Service:", "User Data Was Not NULL")
                        UserManager.currentUser = userModel // store user in UserManager
                        onComplete(true, null) // document fetched successfully
                    } else {
                        Log.i("Firestore Service:", "User Data Was NULL")
                        onComplete(false, "user data is null") // error: user data is null
                    }
                } else {
                    Log.i("Firestore Service:", "User Document Does Not Exist")
                    onComplete(false, "user document does not exist") // error: document doesn't exist
                }
            }
            .addOnFailureListener { exception ->
                Log.i("Firestore Service:", "Document Fetch Process Failed")
                onComplete(false, exception.message) // failed to fetch document, return error message
            }
    }

    // method to add a user document to the "users" collection
    fun addUserDocument(userId: String, user: UserModel, onComplete: (Boolean, String?) -> Unit) {
        Log.i("Firestore Service:", "Adding User Document to Collection - users")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.i("Firestore Service:", "A User is Currently Signed In")
            db.collection("users").document(userId).set(user)
                .addOnSuccessListener {
                    Log.i("Firestore Service:", "Users Details Document Found")
                    onComplete(true, null) // user document successfully added
                }
                .addOnFailureListener { exception ->
                    Log.i("Firestore Service:", "Users Details Document Not Found")
                    onComplete(false, exception.message) // failed to add user document, return error message
                }
        } else {
            Log.i("Firestore Service:", "No Currently Signed In User Found")
            onComplete(false, "user not authenticated") // error: user is not authenticated
        }
    }

    // method to add a counter document to the "counters_tests" collection
    fun addCounter(
        counter: CounterModel,
        onComplete: (Boolean, String?) -> Unit
    ) {
        Log.i("Firestore Service:", "Adding Counter Document to Collection -  Counters")
        val counterData = hashMapOf(
            "createdTimestamp" to counter.createdTimestamp,
            "count" to counter.count,
            "incrementValue" to counter.changeValue,
            "name" to counter.name,
            "repetition" to counter.repetition,
            "userId" to counter.userId,
            "startValue" to counter.startValue,
            "lastReset" to counter.lastReset
        )

        Log.i("Firestore Service:", "Counter Model Created")

        // add counter document to the firestore
        addDocument("counters", counterData) { success, error ->
            if (success) {
                Log.i("Firestore Service:", "Counter Document Creation Process Successful")
                onComplete(true, null) // counter successfully added
            } else {
                // log the error for debugging purposes
                Log.i("Firestore Service:", "Counter Document Creation Process Failed - $error")
                onComplete(false, error) // failed to add counter, return error message
            }
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________