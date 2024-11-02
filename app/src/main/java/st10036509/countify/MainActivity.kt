/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.LoginFragment

class MainActivity : AppCompatActivity() {

    private val REQUEST_NOTIFICATION_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("Main Activity:", "Application Started")

        Firebase.messaging.isAutoInitEnabled = true


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }

        // initialise singleton navigation service upon application start and provide the Fragment Manager
        NavigationService.initialise(supportFragmentManager)

        // add the LoginFragment when the activity is first created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________