/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 03/11/2024
 */

package st10036509.countify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import st10036509.countify.service.NavigationService
import st10036509.countify.service.NotificationService
import st10036509.countify.service.ResetCountersWorker
import st10036509.countify.user_interface.account.LoginFragment
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 1001 // You can choose any integer value
    }

    // entry point to the application
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("Main Activity:", "Application Started")

        //check if the build version is sdk >= 33 and ask for permissions if below
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }

        //initialise singleton navigation service upon application start and provide the Fragment Manager
        NavigationService.initialise(supportFragmentManager)

        //initialise singleton notification service upon application start
        NotificationService.createNotificationChannel(this)

        //schedule the reset counters worker
        scheduleResetCountersWorker()

        // add the LoginFragment when the activity is first created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    // method to schedule the reset counter worker
    private fun scheduleResetCountersWorker() {

        Log.e("Worker", "Scheduling Worker")

        //create a periodic work request for the ResetCountersWorker
        val resetRequest = PeriodicWorkRequestBuilder<ResetCountersWorker>(15, TimeUnit.MINUTES)
            .build()

        //enqueue the work request
        WorkManager.getInstance(this).enqueue(resetRequest)

        Log.d("Main Activity:", "ResetCountersWorker scheduled to run every 15 minutes.")
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________