package st10036509.countify.user_interface.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import st10036509.countify.R
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.SettingsFragment
import st10036509.countify.utils.isMoreThanOne
import st10036509.countify.utils.isMoreThanZero
import st10036509.countify.utils.isTitleNull

class CounterCreationFragment : Fragment() {

    private lateinit var  settingsButton: ImageView
    private lateinit var titleInput: EditText
    private lateinit var repeatDropdown: Spinner
    private lateinit var startValue: TextView
    private lateinit var incrementValue: TextView

    private var title: String = ""
    private var repeat: String = ""
    private var increment: Int = 1
    private var start: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_counter_creation, container, false)

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

        // Settings Icon
        settingsButton = view.findViewById(R.id.iv_settingsIcon)


        // Events
        // Set up the Create button
        val createButton: Button = view.findViewById(R.id.btn_create)
        createButton.setOnClickListener {
            if(validateUserTitleInput()){
                setCounterDetails()
                addCounterToDatabase()
            }
        }

        // Set up the Return button
        val returnButton: Button = view.findViewById(R.id.btn_return)
        returnButton.setOnClickListener {
            // You can navigate back or perform other actions here
            requireActivity().onBackPressed()
        }

        // Set up the settings button
        settingsButton.setOnClickListener{
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

        return view
    }

    private fun incrementStartValue(){
        start++
        startValue.text = start.toString()
    }

    private fun decrementStartValue(){
        if (isMoreThanZero(start)) { // Prevent negative values
            start--
            startValue.text = start.toString()
        }
    }

    private fun incrementIncrementValue(){
        increment++
        incrementValue.text = increment.toString()
    }

    private fun decrementIncrementValue(){
        if (isMoreThanOne(increment)) {
            increment--
            incrementValue.text = increment.toString()
        }
    }

    private fun validateUserTitleInput(): Boolean {
        return try {
            // Capture the input field values
            val title = titleInput.text.toString()


            // Attempt null or empty check
            if (isTitleNull(title)) {
                throw IllegalArgumentException("Title cannot be empty.")
            }

            Toast.makeText(requireContext(), "All inputs are valid.", Toast.LENGTH_SHORT).show()
            true // Validation succeeded

        } catch (e: IllegalArgumentException) {
            // Handle invalid input
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            false // Validation failed
        }
    }

    private fun setCounterDetails(){
        repeat= repeatDropdown.selectedItem.toString()
        start = startValue.text.toString().toIntOrNull()?: 0
        increment = incrementValue.text.toString().toIntOrNull()?: 0
    }

    private fun addCounterToDatabase(){
        // Firebase connection and write to database
        Toast.makeText(requireContext(), "I want to write to database", Toast.LENGTH_SHORT).show()
    }

}
