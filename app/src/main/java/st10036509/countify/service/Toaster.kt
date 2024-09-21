package st10036509.countify.service

import android.widget.Toast
import androidx.fragment.app.Fragment

class Toaster(private val fragment: Fragment) {

    // method to show toast messages on a specified fragment
    fun showToast(message: String?) {
        // Safely handle null message and default to an empty message if null
        val toastMessage = message ?: "Something went wrong"
        Toast.makeText(fragment.requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
    }
}