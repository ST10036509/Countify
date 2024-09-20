package st10036509.countify.user_interface.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import st10036509.countify.R
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment

class LoginFragment : Fragment() {

    // setup service instances
    private lateinit var toaster: Toaster // handle toasting message
    private lateinit var resultsLauncher: ActivityResultLauncher<Intent> //start google sign-in and handle result

    // create object reference for components to handle events
    private lateinit var registerButton: TextView
    private lateinit var loginButton: CardView
    private lateinit var  googleSSOButton: CardView

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

        InitaliseActivityResultsLauncher()

        // handle onClick event
        loginButton.setOnClickListener{

            // get input values
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            //check if inputs are valid
            if (!areInputsValid(email, password)) { // inputs are invalid
                toaster.showToast(getString(R.string.login_invalid_inputs))
            } else { // inputs are valid
                handleEmailPasswordLogin(email, password) // verify login credentials an handle success/failure
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
    private fun InitaliseActivityResultsLauncher() {

        resultsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try{
                    val account = task .getResult(ApiException::class.java)
                    val idToken = account?.idToken

                    if (idToken != null) {
                        FirebaseAuthService.firebaseAuthWithGoogle(idToken) { isSuccess, errorMessage ->
                            if (isSuccess) {
                                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                                toaster.showToast(getString(R.string.login_successful))
                            } else {
                                toaster.showToast(errorMessage)
                            }
                        }
                    }
                } catch (e: ApiException) {
                    toaster.showToast("Google Sign-In failed: ${e.message}")
                }
            }
        }
    }

    // method to check if email and password are valid inputs
    private fun areInputsValid(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // method to handle email & password login authentication
    private fun handleEmailPasswordLogin(email: String, password: String) {
        FirebaseAuthService.loginUser(email, password) { isSuccess, errorMessage ->
            if (isSuccess) { // login successful
                NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                toaster.showToast(getString(R.string.login_successful))
            } else {
                toaster.showToast(errorMessage) // failed login
            }
        }
    }
}