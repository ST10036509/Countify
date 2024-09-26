/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.user_interface.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.FirestoreService
import st10036509.countify.service.GoogleAccountService
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
    private lateinit var googleAccountService: GoogleAccountService // handle google sign-on
    private val resultsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> // handle results of google sign-on request
        googleAccountService.handleSignInResult(result.resultCode, result.data, { user ->
            googleAccountService.checkIfUserExistsInFirestore(user, {
                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                toaster.showToast(getString(R.string.login_successful))
            }, { error ->
                toaster.showToast("Error: $error")
                FirebaseAuthService.logout() // force log user out
            })
        }, { error ->
            toaster.showToast(error) // error in google sign-on
        })
    }

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
        // inflate the layout for this fragment
        Log.i("Register Fragment:", "Register Fragment Inflated")
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("Register Fragment:", "Initialise Services")
        // initialise the google sign-on service
        googleAccountService = GoogleAccountService(requireContext())
        // initialise the google sign-on options
        googleAccountService.setupGoogleSignIn(getString(R.string.default_web_client_id))

        Log.i("Register Fragment:", "Setting Up UI Components")
        // initialise UI components
        setupUIComponents(view)
    }

    private fun setupUIComponents(view: View) {

        Log.i("Register Fragment:", "Initialise Register Fragment Components and Services")
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

        // handle onClick event for register
        registerButton.setOnClickListener {
            Log.i("Register Fragment:", "Register Button Clicked")

            registerUser() // register the user
        }

        // handle onClick event for google SSO
        googleSSOButton.setOnClickListener(){
            Log.i("Register Fragment: ", "SSO Button Clicked")

            googleAccountService.launchSignIn(resultsLauncher)
        }

        // handle onClick event for login
        loginButton.setOnClickListener {
            Log.i("Register Fragment:", "Login Button Clicked")

            NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
        }
    }

    private fun registerUser() {

        Log.i("Register Fragment:", "Registration Started...")

        // capture inputs
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // create a local RegisterInputs model
        val inputs = RegisterInputs(firstName, lastName, phoneNumber, email,
            password, confirmPassword)

        // validate inputs
        if (areInputsValid(inputs)) {
            handleEmailPasswordRegister(inputs) // register user account
        }
    }

    private fun areInputsValid(inputs: RegisterInputs): Boolean {

        Log.i("Register Fragment:", "Checking if Register Inputs Are Valid")

        // convert model to mutable list of inputs
        val inputsList = inputs.toMutableListRegister()

        val context = requireContext() // ensure you're within a fragment

        // generate a list of validation check methods to run
        val validationChecks = listOf(

            !areNullInputs(inputsList) to { context.getString(R.string.null_inputs_error) },

            isValidPhoneNumber(inputs.phoneNumber) to { context.getString(R.string.invalid_phone_number_error) },

            stringsMatch(inputs.password, inputs.confirmPassword) to { context.getString(R.string.password_mismatch_error) },

            isPasswordStrong(inputs.password) to { context.getString(R.string.weak_password_error) }
        )

        // check each value against the validation checks specified
        for ((isValid, errorMessage) in validationChecks) {
            if (!isValid) {
                Log.e("Register Fragment:", "Register Inputs Are NOT Valid")
                toaster.showToast(errorMessage())
                return false
            }
        }

        Log.i("Register Fragment:", "Register Inputs Are Valid")
        // All inputs valid
        return true
    }

    private fun handleEmailPasswordRegister(inputs: RegisterInputs) {

        Log.e("Register Fragment:", "Registering the User...")

        val context = requireContext() // Ensure you're within a fragment

        FirebaseAuthService.registerUser(inputs.email, inputs.password) { isSuccess, errorMessage ->
            if (isSuccess) {
                val userId = FirebaseAuthService.getCurrentUser()?.uid ?: ""

                Log.i("Register Fragment:", "Account Registered Successfully") // Check userId

                val newUser = UserModel(
                    userId = userId, // new userId
                    firstName = inputs.firstName,
                    lastName = inputs.lastName,
                    email = inputs.email,
                    notificationsEnabled = true, // yes notifications
                    darkModeEnabled = false, // no darkMode
                    language = 0, // english
                    counters = emptyList() //empty list
                )
                Log.i("Register Fragment:", "Account Details Model Created")
                Log.i("Register Fragment:", "Creating User Account Details Document")

                // Call addUserDocument with a completion handler
                FirestoreService.addUserDocument(userId, newUser) { isDocumentAdded, documentError ->
                    if (isDocumentAdded) {
                        Log.i("Register Fragment:", "Account Details Document Successfully Created.\n" +
                                "Updated UserManager Pointer to New User Model.\n" +
                                "Navigating to CounterViewFragment.\n")
                        UserManager.currentUser = newUser
                        toaster.showToast(context.getString(R.string.registration_successful))
                        NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                    } else {
                        Log.i("Register Fragment:", "Account Details Document Successfully Created. Logging User Out...")
                        FirebaseAuthService.logout()
                        toaster.showToast(" ${context.getString(R.string.registration_failed_header)} $documentError")
                    }
                }
                hideKeyboard() // hide keyboard inputs if it is still open
            } else {
                toaster.showToast(" ${context.getString(R.string.registration_failed_header)} $errorMessage") // error out
            }
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________