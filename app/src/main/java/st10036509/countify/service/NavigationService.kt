package st10036509.countify.service

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

// Singleton object to handle applicaiton navigation
object NavigationService {

    // instance object of fragment manager
    private var fragmentManager: FragmentManager? = null

    // method to initialise the navigation service upon application startup
    fun initialise(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    // method to handle fragment navigation based on inputted Fragment class
    fun navigateToFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean = true) {
        // fragment transaction to replace current fragment with specified one
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(containerId, fragment)
        if (addToBackStack) {
            transaction?.addToBackStack(null)
        }
        transaction?.commit()
    }
}