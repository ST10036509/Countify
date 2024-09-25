package st10036509.countify.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.model.CounterModel
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel

object FirestoreService {

    // get the instance of the firestore database
    private val db by lazy { FirebaseFirestore.getInstance() }

    // method to handle the addition of a document to a specified collection
    fun addDocument(collection: String, data: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        db.collection(collection).add(data)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener {exception ->
                onComplete(false, exception.message)
            }
    }

    // method to handle the fetching of a specified document from a collection
    fun getUserDocument(userId: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userModel = document.toObject(UserModel::class.java)
                    if (userModel != null) {
                        UserManager.currentUser = userModel
                        onComplete(true, null)
                    } else {
                        onComplete(false, "User data is null")
                    }
                } else {
                    onComplete(false, "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // method to handle the deletion of a specified document from a collection
    fun deleteDocument(collection: String, documentId: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection(collection).document(documentId).delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun addUserDocument(userId: String, user: UserModel, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            db.collection("users").document(userId).set(user)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    onComplete(false, exception.message)
                }
        } else {
            onComplete(false, "User not authenticated")
        }
    }

    fun addCounter(
        counter: CounterModel,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val counterData = hashMapOf(
            "createdTimestamp" to counter.createdTimestamp,
            "currentValue" to counter.currentValue,
            "incrementValue" to counter.changeValue,
            "name" to counter.name,
            "repetition" to counter.repetition,
            "startValue" to counter.startValue,
            "userId" to counter.userId
        )

        // Add document to Firestore
        addDocument("counters_tests", counterData) { success, error ->
            if (success) {
                onComplete(true, null)
            } else {
                // Log or print the error for debugging
                Log.e("FirestoreService", "Failed to add counter: $error")
                onComplete(false, error)
            }
        }
    }



}