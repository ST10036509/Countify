package st10036509.countify.service

import android.widget.Toast
import androidx.fragment.app.Fragment

class Toaster(private val fragment: Fragment) {

    // method to show toast messages on a specified fragment
    fun showToast(message: String?) {
        Toast.makeText(fragment.requireContext(),
        message,
        Toast.LENGTH_SHORT).show()
    }
}