package st10036509.countify.service

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {

    // get the instance of the firestore database
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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
    fun getDocument(collection: String, documentId: String, onComplete: (DocumentSnapshot?, String?) -> Unit) {
        db.collection(collection).document(documentId).get()
            .addOnSuccessListener { document ->
                onComplete(document, null)
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
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
}