/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 03/11/2024
Last Modified: 03/11/2024
 */

package st10036509.countify.service

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import st10036509.countify.model.UserManager

class ResetCountersWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val db = FirebaseFirestore.getInstance() //get Firebase db instance
    private val currentUser = FirebaseAuthService.getCurrentUser() //get current user

    private var notificationCount: Int = 0



    // worker to reset counters to original values
    override suspend fun doWork(): Result {
        Log.d(ContentValues.TAG, "Worker started")
        return try {
            resetCounters()
            Log.d(ContentValues.TAG, "Worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error in worker: ${e.message}")
            Result.failure()
        }
    }

    //method to check all counters to see if they need to be reset
    private suspend fun resetCounters() {
        //get all documents from the counters collection
        val counters = db.collection("counters")
            .whereEqualTo("userId", currentUser?.uid)
            .get()
            .await()

        Log.i("User data:", "${UserManager.currentUser}")

        Log.w(ContentValues.TAG, "Getting counters for user")

        for (document in counters.documents) {
            val counterData = document.data ?: continue // Safely access document data
            val lastReset = (counterData["lastReset"] as? Long) ?: 0L // Default to 0 if null
            val resetFrequency = (counterData["repetition"] as? String) ?: "15 Minutes"
            val startValue = (counterData["startValue"] as? Long)?.toInt() ?: 0 // Default to 0 if null
            val currentValue = (counterData["count"] as? Long)?.toInt() ?: 0 // Default to 0 if null
            val counterName = (counterData["name"] as? String) ?: "Unknown"

            // Check if the current value is different from the start value
            if (currentValue != startValue && shouldReset(lastReset, resetFrequency)) {
                Log.e("ResetCounterWorker:", "Resetting counter for document: ${document.id}")
                document.reference.update(
                    mapOf(
                        "count" to startValue,
                        "lastReset" to System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    if (UserManager.currentUser?.notificationsEnabled != false){
                        sendResetNotification(counterName, applicationContext)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ResetCounterWorker:", "Error updating document: ${e.message}")
                }
            }
        }
    }

    // method to determine if a counter should be reset based on last reset time and frequency
    private fun shouldReset(lastReset: Long, frequency: String): Boolean {
        val now = System.currentTimeMillis() //get the current time in milliseconds

        return when (frequency) {
            "30 Seconds", "30 Sekondes" -> {
                val thirtySecondsAgo = lastReset + 30 * 1000 //30 seconds in milliseconds
                now >= thirtySecondsAgo
            }
            "15 Minutes", "15 Minute" -> {
                val fifteenMinutesAgo = lastReset + 15 * 60 * 1000 //15 minutes in milliseconds
                now >= fifteenMinutesAgo
            }
            "Daily", "Daagliks" -> {
                val oneDayLater = lastReset + 24 * 60 * 60 * 1000 //1 day in milliseconds
                now >= oneDayLater
            }
            "Weekly", "Weekliks" -> {
                val oneWeekLater = lastReset + 7 * 24 * 60 * 60 * 1000 //1 week in milliseconds
                now >= oneWeekLater
            }
            "Monthly", "Maandeliks" -> {
                val oneMonthInMillis = 30L * 24 * 60 * 60 * 1000 //30 days in milliseconds
                if (lastReset > Long.MAX_VALUE - oneMonthInMillis) return false //prevent overflow
                val oneMonthLater = lastReset + oneMonthInMillis
                now >= oneMonthLater
            }
            "Yearly", "Jaarliks" -> {
                val oneYearInMillis = 365L * 24 * 60 * 60 * 1000 //365 days in milliseconds
                if (lastReset > Long.MAX_VALUE - oneYearInMillis) return false //prevent overflow
                val oneYearLater = lastReset + oneYearInMillis
                now >= oneYearLater
            }
            else -> false //return false if no repeat category is selected
        }
    }

    private fun sendResetNotification(counterName: String, context: Context) {
        context?.let { safeContext ->
            NotificationService.showNotification(
                context = safeContext,
                notificationId = notificationCount++,
                title = "Counter Reset",
                message = "Your $counterName counter has been reset"
            )
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________