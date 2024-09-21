package st10036509.countify.user_interface.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import st10036509.countify.utils.areNullInputs
import st10036509.countify.utils.hideKeyboard
import st10036509.countify.utils.isPasswordStrong
import st10036509.countify.utils.isValidPhoneNumber
import st10036509.countify.utils.stringsMatch
import st10036509.countify.utils.toMutableListRegister

class RegisterFragment: Fragment() {
    // setup service instances
    private lateinit var toaster: Toaster // handle toasting message

    // create object reference for components to handle events
    private lateinit var loginButton: TextView
    private lateinit var registerButton: CardView
    private lateinit var googleSSOButton: CardView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText

    //data structures
    data class RegisterInputs(val firstName: String,
                              val lastName: String,
                              val phoneNumber: String,
                              val email: String,
                              val password: String,
                              val confirmPassword: String)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialise UI components
        setupUIComponents(view)
    }

    private fun setupUIComponents(view: View) {
        // bind reference objects to fragment xml components
        // ACTION BUTTONS:
        loginButton = view.findViewById(R.id.btn_Login)
        registerButton = view.findViewById(R.id.btn_Register)
        googleSSOButton = view.findViewById(R.id.btn_SSO)
        //INPUTS:
        firstNameEditText = view.findViewById<EditText>(R.id.edt_FirstName)
        lastNameEditText = view.findViewById<EditText>(R.id.edt_LastName)
        phoneNumberEditText = view.findViewById<EditText>(R.id.edt_PhoneNumber)
        emailEditText = view.findViewById<EditText>(R.id.edt_Email)
        passwordEditText = view.findViewById<EditText>(R.id.edt_Password)
        confirmPasswordEditText = view.findViewById<EditText>(R.id.edt_ConfirmPassword)
        //services
        toaster =  Toaster(this)

        // Set click listener for register button
        registerButton.setOnClickListener {
            registerUser()
        }

        loginButton.setOnClickListener {
            NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
        }
    }

    private fun registerUser() {

        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        val inputs = RegisterInputs(firstName, lastName, phoneNumber, email,
            password, confirmPassword)

        // validate inputs
        if (areInputsValid(inputs)) {
            handleEmailPasswordRegister(inputs)//register user account
        }
    }

    private fun areInputsValid(inputs: RegisterInputs): Boolean {

        val inputsList = inputs.toMutableListRegister()

        val context = requireContext() // ensure you're within a fragment

        val validationChecks = listOf(

            !areNullInputs(inputsList) to { context.getString(R.string.null_inputs_error) },

            isValidPhoneNumber(inputs.phoneNumber) to { context.getString(R.string.invalid_phone_number_error) },

            stringsMatch(inputs.password, inputs.confirmPassword) to { context.getString(R.string.password_mismatch_error) },

            isPasswordStrong(inputs.password) to { context.getString(R.string.weak_password_error) }
        )

        for ((isValid, errorMessage) in validationChecks) {
            if (!isValid) {
                toaster.showToast(errorMessage())
                return false
            }
        }

        // All inputs valid
        return true
    }

    private fun handleEmailPasswordRegister(inputs: RegisterInputs) {

        val context = requireContext() // ensure you're within a fragment

        FirebaseAuthService.registerUser(inputs.email, inputs.password) { isSuccess, errorMessage ->
            if (isSuccess) {

                UserManager.currentUser = UserModel(
                    userId = FirebaseAuthService.getCurrentUser()?.uid ?: "",
                    firstName = inputs.firstName,
                    lastName = inputs.lastName,
                    email = inputs.email,
                    phoneNumber = inputs.phoneNumber
                )

                toaster.showToast( context.getString(R.string.registration_successful) )
                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                hideKeyboard()
            } else {
                toaster.showToast(" ${context.getString(R.string.registration_failed_header)} $errorMessage")
            }
        }
    }
}