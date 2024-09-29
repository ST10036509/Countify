/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 19/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.user_interface.account

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import st10036509.countify.service.BiometricService
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.FirestoreService
import st10036509.countify.service.GoogleAccountService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import st10036509.countify.utils.areNullInputs
import st10036509.countify.utils.hideKeyboard
import st10036509.countify.utils.toMutableListLogin

class LoginFragment : Fragment() {

    // setup service instances
    private lateinit var toaster: Toaster // handle toasting message
    private lateinit var googleAccountService: GoogleAccountService // handle google sign-on
    private lateinit var biometricService: BiometricService // handle biometric re-login
    private val resultsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> // handle results of google sign-on request
        googleAccountService.handleSignInResult(result.resultCode, result.data, { user ->
            googleAccountService.checkIfUserExistsInFirestore(user, {
                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container) // navigate to CounterViewFragment
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
    private lateinit var loginButton: CardView
    private lateinit var registerButton: TextView
    private lateinit var  googleSSOButton: CardView

    //data structures
    data class LoginInputs(val email: String,
                           val password: String)

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        Log.i("Login Fragment:", "Login Fragment Inflated")
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // when the view is created establish service initialisation adn setup UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("Login Fragment:", "Initialise Services")
        // initialise the google sign-on service
        googleAccountService = GoogleAccountService(requireContext())
        // initialise the google sign-on options
        googleAccountService.setupGoogleSignIn(getString(R.string.default_web_client_id))
        //initialise biometric service
        biometricService = BiometricService(requireActivity())

        Log.i("Login Fragment:", "Setting Up UI Components")
        // initialise UI components
        setupUIComponents(view)
    }

    // run after fragment has been created
    override fun onResume() {
        super.onResume()

        Log.i("Login Fragment:", "Fetching Current User")
        // get the current user (if they are logged in)
        val currentUser = FirebaseAuthService.getCurrentUser()

        Log.i("Login Fragment:", "Checking if User Account Is Logged In with Valid Token")
        //check if current user exists
        if (currentUser != null) {
            Log.i("Login Fragment:", "User Exists")
            Log.i("Biometric Service:", "Starting Biometrics Prompt")
            // delay biometric prompt to ensure fragment transactions are complete
            Handler(Looper.getMainLooper()).postDelayed({
                biometricService.showBiometricPrompt( // show biometric prompt
                    onSuccess = { // on success of login
                        Log.i("Biometric Service:", "Biometric Login Successful")
                        Log.i("Biometric Service:", "Attempting to Fetch User Account Document")
                        FirestoreService.getUserDocument(currentUser.uid) { isDocumentRetrieved, documentErrorMessage ->
                            if (isDocumentRetrieved) {
                                Log.i("Biometric Service:", "User Account Document Retrieved")
                                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                                toaster.showToast(getString(R.string.login_successful))
                                hideKeyboard() //hide keyboard
                            } else {
                                FirebaseAuthService.logout() // force logout
                                Log.e("Biometric Service:", "Logged in But User document doesn't exist")
                            }
                        }
                        NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                    },
                    onFailure = {
                        toaster.showToast("Biometric Authentication failed")
                    },
                    onError = { error ->
                        Log.e("Biometric Service:", "Authentication error: $error")
                        FirebaseAuthService.logout()
                    }
                )
            }, 500)  // A delay of 500ms (adjust as needed)
        }
    }

    private fun setupUIComponents(view: View) {

        Log.i("Login Fragment:", "Initialise Login Fragment Components and Services")
        // bind reference objects to fragment xml components
        // ACTION BUTTONS:
        loginButton = view.findViewById(R.id.btn_Login)
        registerButton = view.findViewById(R.id.btn_Register)
        googleSSOButton = view.findViewById(R.id.btn_SSO)
        //INPUTS:
        val emailEditText = view.findViewById<EditText>(R.id.edt_Email)
        val passwordEditText = view.findViewById<EditText>(R.id.edt_Password)
        //services
        toaster =  Toaster(this)

        // handle onClick event for login
        loginButton.setOnClickListener{
            Log.i("Login Fragment:", "Login Button Clicked")

            // get input values
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            Log.i("Login Fragment:", "Inputs Captured and Passed to a Local Model")
            // pass inputs to a data class of the inputs
            val inputs = LoginInputs(email, password)

            //check if inputs are valid
            if (!areInputsValid(inputs)) { // inputs are invalid
                Log.i("Login Fragment:", "Login Inputs Invalid")
                toaster.showToast(getString(R.string.login_invalid_inputs))
            } else { // inputs are valid
                Log.i("Login Fragment:", "Login Inputs Invalid. Handling Email/Password Login")
                handleEmailPasswordLogin(inputs) // verify login credentials an handle success/failure
            }
        }

        // handle onClick event for google SSO button
        googleSSOButton.setOnClickListener {
            Log.i("Google SSO Process: ", "SSO Button Clicked. Launching Google SSO Prompt...")

            // launch google sign-on prompt
            googleAccountService.launchSignIn(resultsLauncher)
        }

        // handle onClick event for register
        registerButton.setOnClickListener {
            Log.i("Login Fragment:", "Register Button Clicked")

            // use the navigation service (singleton) to navigate to the RegisterFragment
            NavigationService.navigateToFragment(RegisterFragment(), R.id.fragment_container)
        }
    }

    // method to check if email and password are valid inputs
    public fun areInputsValid(inputs: LoginInputs): Boolean {

        Log.i("Login Fragment:", "Checking Input Validity")
        // pass inputs to mutable list
        val inputsList = inputs.toMutableListLogin()

        val context = requireContext() // ensure you're within a fragment

        // list of validation methods to run
        val validationChecks = listOf(

            !areNullInputs(inputsList) to { context.getString(R.string.null_inputs_error) }

        )

        // check if inputs are invalid by running list of inputs through each validation method
        for ((isValid, errorMessage) in validationChecks) {
            if (!isValid) {
                Log.i("Login Fragment:", "Inputs Are NOT Valid")
                toaster.showToast(errorMessage()) // show appropriate error message
                return false
            }
        }
        Log.i("Login Fragment:", "Inputs Are Valid")
        // all inputs valid
        return true
    }

    // method to handle email & password login authentication
    private fun handleEmailPasswordLogin(inputs: LoginInputs) {
        Log.i("Login Fragment:", "Handling Login Process")
        //run firebase login method
        FirebaseAuthService.loginUser(inputs.email, inputs.password) { isLoginSuccess, loginErrorMessage ->
            if (isLoginSuccess) { // if login is successful
                Log.i("Login Fragment:", "Login Successful. Attempting to Pull User Data...")
                // attempt to get the current user from the database
                val currentUser = FirebaseAuthService.getCurrentUser()?.uid ?: ""
                // check if a user exists
                if (currentUser.isNotEmpty()) {
                    //attempt to fetch the user document related to teh user account
                    FirestoreService.getUserDocument(currentUser) { isDocumentRetrieved, documentErrorMessage ->
                        if (isDocumentRetrieved) {
                            Log.i("Login Fragment:", "User Account Document Found in Firestore. Navigating to CounterViewFragment...")
                            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container) // navigate to CounterViewFragment
                            toaster.showToast(getString(R.string.login_successful)) // login success message
                            hideKeyboard() // hide keyboard
                        } else {
                            Log.i("Login Fragment:", "User Account Document Does Not Exist in Firestore")
                            FirebaseAuthService.logout() // force logout user
                            Log.e("Login:", "Error: $documentErrorMessage") // log error message if account exists but document does not
                        }
                    }
                }
            } else {
                Log.i("Login Fragment:", "Login Failed. Logging User Out...")
                toaster.showToast(loginErrorMessage) // failed login with appropriate message
            }
        }
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________