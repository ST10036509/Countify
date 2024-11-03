package st10036509.countify.user_interface.counter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.system.Os
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import st10036509.countify.R
import st10036509.countify.model.CounterModel
import st10036509.countify.service.CounterDatabaseHelper
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.FirestoreService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import st10036509.countify.user_interface.account.SettingsFragment
import st10036509.countify.utils.isMoreThanOne
import st10036509.countify.utils.isMoreThanZero
import st10036509.countify.utils.isTitleNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CounterCreationFragment : Fragment() {

    private lateinit var settingsButton: ImageView
    private lateinit var titleInput: EditText
    private lateinit var repeatDropdown: Spinner
    private lateinit var startValue: TextView
    private lateinit var incrementValue: TextView

    private lateinit var toaster: Toaster // handle toasting message
    private var title: String = ""
    private var repeat: String = ""
    private var increment: Int = 1
    private var start: Int = 0
    private var timeStamp: Long = 0

    // ActivityResultLauncher for Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_counter_creation, container, false)
        toaster = Toaster(this)

        val counterViewFragment = parentFragmentManager.findFragmentByTag("CounterViewFragmentTag") as? CounterViewFragment
        counterViewFragment?.syncUnsyncedCounters()

        // Initialize input fields
        titleInput = view.findViewById(R.id.title_input)
        repeatDropdown = view.findViewById(R.id.repeat_dropdown)
        startValue = view.findViewById(R.id.start_value)
        incrementValue = view.findViewById(R.id.increment_value)

        // Populate the Spinner with options from string array
        val repeatOptions = resources.getStringArray(R.array.repeat_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, repeatOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatDropdown.adapter = adapter

        // Check authentication status
        if (FirebaseAuthService.getCurrentUser() == null) {
            // Prompt for login
            Toast.makeText(requireContext(), "Please log in to create a counter.", Toast.LENGTH_SHORT).show()
            //FirebaseAuthService.googleSignIn(requireActivity(), googleSignInLauncher)
        }

        // Setup UI components
        settingsButton = view.findViewById(R.id.iv_settingsIcon)

        // Set up the Create button
        val createButton: CardView = view.findViewById(R.id.btn_Create)
        createButton.setOnClickListener {
            if (validateUserTitleInput()) {
                setCounterDetails()
                addCounterToDatabase()
            }
        }

        // Set up the Return button
        val returnButton: CardView = view.findViewById(R.id.btn_return)
        returnButton.setOnClickListener {
            requireActivity().onBackPressed()
        }


        // Settings Icon click
        settingsButton.setOnClickListener {
            NavigationService.navigateToFragment(SettingsFragment(), R.id.fragment_container)
        }

        // Increment/Decrement for Start Value
        view.findViewById<Button>(R.id.increment_start_value).setOnClickListener {
            incrementStartValue()
        }

        view.findViewById<Button>(R.id.decrement_start_value).setOnClickListener {
            decrementStartValue()
        }

        // Increment/Decrement for Increment Value
        view.findViewById<Button>(R.id.increment_increment_value_button).setOnClickListener {
            incrementIncrementValue()
        }

        view.findViewById<Button>(R.id.decrement_increment_value_button).setOnClickListener {
            decrementIncrementValue()
        }
        Log.i("onCreate","View createds")

        return view
    }

    private fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun incrementStartValue() {
        start++
        startValue.text = start.toString()
    }

    fun decrementStartValue() {
        if (isMoreThanZero(start)) {
            start--
            startValue.text = start.toString()
        }
    }

    private fun incrementIncrementValue() {
        increment++
        incrementValue.text = increment.toString()
    }

    private fun decrementIncrementValue() {
        if (isMoreThanOne(increment)) {
            increment--
            incrementValue.text = increment.toString()
        }
    }

    fun validateUserTitleInput(): Boolean {
        return try {
            title = titleInput.text.toString()
            if (isTitleNull(title)) {
                throw IllegalArgumentException("Title cannot be empty.")
            }
            Log.i("validateUserTitleInput","All inputs are valid")
            true
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun setCounterDetails() {
        repeat = repeatDropdown.selectedItem.toString()
        start = startValue.text.toString().toIntOrNull() ?: 0
        increment = incrementValue.text.toString().toIntOrNull() ?: 0
        timeStamp = getCurrentTimestamp()  // Capture the current timestamp
        Log.i("setCounterDetails","Counter details are set locally in class")
    }

    private fun addCounterToDatabase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val currentTime = getCurrentTimestamp();

        // Create the CounterModel instance
        val counter = CounterModel(
            userId = userId,
            name = title,
            startValue = start,
            count = start,
            changeValue = increment,
            repetition = repeat,
            createdTimestamp = currentTime,
            lastReset = currentTime,
            synced = false  // Initially set as unsynced
        )

        // Save counter locally in SQLite
        val dbHelper = CounterDatabaseHelper(requireContext())
        dbHelper.insertCounter(counter)

        // Try to sync with Firestore if connected
        if (isConnected()) {
            // Retrieve the CounterViewFragment by tag
            val counterViewFragment = parentFragmentManager.findFragmentByTag("CounterViewFragmentTag") as? CounterViewFragment

            // Call the sync method if the fragment is found
            counterViewFragment?.syncUnsyncedCounters()
        } else {
            Toast.makeText(requireContext(), "Counter saved locally. Will sync when online.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isConnected(): Boolean {
        // Check network connectivity status
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

}
