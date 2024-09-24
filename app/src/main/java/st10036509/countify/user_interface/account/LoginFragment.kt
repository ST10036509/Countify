package st10036509.countify.user_interface.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.model.UserModel
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
    private lateinit var googleSignInClient: GoogleSignInClient
    private val resultsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("Google SSO Process: ", "Launcher Started")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.i("Google SSO Process: ", "Launcher Results Success")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("Google API Error : ", e.message.toString())
            }
        }
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
        setupGoogleSignIn()
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

        googleSSOButton.setOnClickListener {
            Log.i("Google SSO Process: ", "SSO Button Clicked")

            val signInIntent = googleSignInClient.signInIntent
            resultsLauncher.launch(signInIntent)
        }

        //handle onClick event
        registerButton.setOnClickListener {
            // use the navigation service (singleton) to navigate to the RegisterFragment
            NavigationService.navigateToFragment(RegisterFragment(), R.id.fragment_container)
        }
    }

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

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.i("Google SSO Process: ", "Firebase Auth in Process")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        Log.i("Google SSO Process: ", "Token - $idToken")

        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                Log.i("Google SSO Process: ", "Sign In Process Complete")
                if (task.isSuccessful) {
                    Log.i("Google SSO Process: ", "Sign In Successful")
                    val user = FirebaseAuthService.getCurrentUser()
                    if (user != null) {
                        Log.i("Google SSO Process: ", "User doesn't exist")
                        checkIfUserExistsInFirestore(user)
                    }
                } else {
                    Log.i("Google SSO Process: ", "Sign In Failed")
                    //error
                }
            }
    }

    private fun checkIfUserExistsInFirestore(user: FirebaseUser) {
        Log.i("Google SSO Process: ", "Check If User Exists Started")

        val context = requireContext()

        FirestoreService.getUserDocument(user.uid) { isDocumentRetrieved, errorMessage ->
            if (!isDocumentRetrieved) {

                Log.i("Google SSO Process: ", "No User Document Exists")

                val account = GoogleSignIn.getLastSignedInAccount(requireActivity())

                Log.i("Google SSO Process: ", "Account Details" +
                        "\n - ${account?.id}" +
                        "\n - ${account?.givenName}" +
                        "\n - ${account?.familyName}" +
                        "\n - ${account?.email}")

                val newUser = UserModel(
                    userId = user.uid,
                    firstName = account?.givenName.toString(),
                    lastName = account?.familyName.toString(),
                    email = account?.email.toString(),
                    notificationsEnabled = true, // yes notifications
                    darkModeEnabled = false, // no darkMode
                    language = 0, // english
                    counters = emptyList() //empty list
                )

                Log.i("Google SSO Process: ", "Model Created")

                FirestoreService.addUserDocument(user.uid, newUser) { isDocumentAdded, documentError ->
                    if (isDocumentAdded) {
                        Log.i("Google SSO Process: ", "User Document Added Successfully")
                        UserManager.currentUser = newUser
                        toaster.showToast(context.getString(R.string.registration_successful))
                        NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
                    } else {
                        Log.i("Google SSO Process: ", "User Document Add Failed")
                        FirebaseAuthService.logout()
                        toaster.showToast(" ${context.getString(R.string.registration_failed_header)} $documentError")
                    }
                }
            }
        }
    }
}