/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

// class to handle showing toast messages on a fragment
class Toaster(private val fragment: Fragment) {

    // method to display a toast message on the specified fragment
    fun showToast(message: String?) {
        Log.i("Toaster", "Showing Toast Message")
        // if the message is null, default to "Something went wrong"
        val toastMessage = message ?: "Something went wrong"
        // show the toast message in the fragment's context
        Toast.makeText(fragment.requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________