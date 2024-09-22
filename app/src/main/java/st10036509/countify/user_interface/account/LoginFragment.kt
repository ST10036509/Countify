package st10036509.countify.user_interface.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.FirestoreService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import st10036509.countify.utils.areNullInputs
import st10036509.countify.utils.hideKeyboard
import st10036509.countify.utils.toMutableListLogin

class LoginFragment : Fragment() {

    // setup service instances
    private lateinit var toaster: Toaster // handle toasting message
    private lateinit var resultsLauncher: ActivityResultLauncher<Intent> //start google sign-in and handle result

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
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // when the view is created bind the register TextView to its object
    // reference and handle oncClick event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get the current user (if they are logged in)
        val currentUser = FirebaseAuthService.getCurrentUser()

        // check if a user account is logged in and navigate to the home page
        if (currentUser != null) {
            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
            return
        }

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
        val emailEditText = view.findViewById<EditText>(R.id.edt_Email)
        val passwordEditText = view.findViewById<EditText>(R.id.edt_Password)
        //services
        toaster =  Toaster(this)

        //initialiseActivityResultsLauncher()

        // handle onClick event
        loginButton.setOnClickListener{

            // get input values
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            val inputs = LoginInputs(email, password)

            //check if inputs are valid
            if (!areInputsValid(inputs)) { // inputs are invalid
                toaster.showToast(getString(R.string.login_invalid_inputs))
            } else { // inputs are valid
                handleEmailPasswordLogin(inputs) // verify login credentials an handle success/failure
            }
        }

        googleSSOButton.setOnClickListener{
            FirebaseAuthService.googleSignIn(requireActivity(), resultsLauncher)
        }

        //handle onClick event
        registerButton.setOnClickListener {
            // use the navigation service (singleton) to navigate to the RegisterFragment
            NavigationService.navigateToFragment(RegisterFragment(), R.id.fragment_container)
        }
    }

    // method to initialise the activityLauncher for google sign on
    /*private fun initialiseActivityResultsLauncher() {

        resultsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            toaster.showToast(result.resultCode.toString())
            toaster.showToast(Activity.RESULT_OK.toString())
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try{
                    val account = task .getResult(ApiException::class.java)
                    val idToken = account?.idToken

                    toaster.showToast("ID Token: $idToken")

                    if (idToken != null) {
                        FirebaseAuthService.firebaseAuthWithGoogle(idToken) { isSuccess, errorMessage ->
                            if (isSuccess) {
                                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                                toaster.showToast(getString(R.string.login_successful))
                            } else {
                                toaster.showToast(errorMessage)
                            }
                        }
                    } else {
                        toaster.showToast("ID Token is null")
                    }
                } catch (e: ApiException) {
                    toaster.showToast("Google Sign-In failed: ${e.message}")
                }
            }
        }
    }*/

    // method to check if email and password are valid inputs
    private fun areInputsValid(inputs: LoginInputs): Boolean {

        val inputsList = inputs.toMutableListLogin()

        val context = requireContext() // ensure you're within a fragment

        val validationChecks = listOf(

            !areNullInputs(inputsList) to { context.getString(R.string.null_inputs_error) }

        )

        for ((isValid, errorMessage) in validationChecks) {
            if (!isValid) {
                toaster.showToast(errorMessage())
                return false
            }
        }

        // all inputs valid
        return true
    }

    // method to handle email & password login authentication
    private fun handleEmailPasswordLogin(inputs: LoginInputs) {
        FirebaseAuthService.loginUser(inputs.email, inputs.password) { isLoginSuccess, loginErrorMessage ->
            if (isLoginSuccess) {
                val currentUser = FirebaseAuthService.getCurrentUser()?.uid ?: ""
                if (currentUser.isNotEmpty()) {
                    FirestoreService.getUserDocument(currentUser) { isDocumentRetrieved, documentErrorMessage ->
                        if (isDocumentRetrieved) {
                            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                            toaster.showToast(getString(R.string.login_successful))
                            hideKeyboard()
                        } else {
                            FirebaseAuthService.logout()
                            toaster.showToast("Error: $documentErrorMessage")
                        }
                    }
                }
            } else {
                toaster.showToast(loginErrorMessage) // Failed login
            }
        }
    }
}