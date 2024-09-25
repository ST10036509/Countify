/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 20/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

// singleton object to handle application navigation
object NavigationService {

    // holds an instance of fragment manager
    private var fragmentManager: FragmentManager? = null

    // method to initialize the navigation service when the app starts
    fun initialise(fragmentManager: FragmentManager) {
        Log.i("Navigation Service:", "Navigation Service Initialised")
        this.fragmentManager = fragmentManager
    }

    // method to navigate to a new fragment
    // takes a fragment, container id, and optional flag to add to the back stack
    fun navigateToFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean = true) {

        Log.i("Navigation Service:", "Navigating to Fragment - ${fragment.id}")

        // begins a fragment transaction to replace the current fragment
        val transaction = fragmentManager?.beginTransaction()
        // replaces the fragment in the specified container
        transaction?.replace(containerId, fragment)
        // if true, adds the transaction to the back stack
        if (addToBackStack) {
            Log.i("Navigation Service:", "Fragment Added to Back Stack")

            transaction?.addToBackStack(null)
        }

        Log.i("Navigation Service:", "Navigation Complete")
        // commits the transaction to apply changes
        transaction?.commit()
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________