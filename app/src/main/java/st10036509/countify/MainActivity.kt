/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import st10036509.countify.service.NavigationService
import st10036509.countify.service.ResetCountersWorker
import st10036509.countify.user_interface.account.LoginFragment
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("Main Activity:", "Application Started")

        // initialise singleton navigation service upon application start and provide the Fragment Manager
        NavigationService.initialise(supportFragmentManager)

        scheduleResetCountersWorker();

        // add the LoginFragment when the activity is first created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun scheduleResetCountersWorker() {

        Log.e("Worker", "Scheduling Worker");


        //create a periodic work request for the ResetCountersWorker
        val resetRequest = PeriodicWorkRequestBuilder<ResetCountersWorker>(15, TimeUnit.MINUTES)
            .build()

        //enqueue the work request
        WorkManager.getInstance(this).enqueue(resetRequest)

        Log.d("Main Activity:", "ResetCountersWorker scheduled to run every 15 minutes.")
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________