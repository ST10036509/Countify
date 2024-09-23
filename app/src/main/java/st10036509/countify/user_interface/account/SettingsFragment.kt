package st10036509.countify.user_interface.account
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment

class SettingsFragment : Fragment() {

    // create object reference for components to handle events
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: CardView
    private lateinit var deleteButton: CardView
    private lateinit var notiSwitch: Switch
    private lateinit var themeSwitch: Switch
    private lateinit var langSwitch: Switch

    // get the current user (if they are logged in)
    val currentUser = FirebaseAuthService.getCurrentUser()

    val nameDisplay = view?.findViewById<EditText>(R.id.name_input_txt)
    val surnameDisplay = view?.findViewById<EditText>(R.id.surname_input_txt)
    val emailDisplay = view?.findViewById<EditText>(R.id.email_input_txt)
    private lateinit var toaster: Toaster // handle toasting message

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        toaster =  Toaster(this)

        if (currentUser != null) {
            val userId = currentUser.uid

            // Get Firestore instance
            val db = FirebaseFirestore.getInstance()

            // Query Firestore for the document that matches the current user's ID
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Retrieve data from the document
                        val firstName = document.getString("firstname")
                        val lastName = document.getString("lastname")
                        val email = document.getString("email")

                        // Use the retrieved data
                        nameDisplay?.setText(firstName)
                        surnameDisplay?.setText(lastName)
                        emailDisplay?.setText(email)

                    } else {
                        FirebaseAuthService.logout()
                        toaster.showToast("Error: No user found")
                    }
                }
                .addOnFailureListener { exception ->
                    FirebaseAuthService.logout()
                    toaster.showToast("Error: No user found")
                }
        } else {
            FirebaseAuthService.logout()
            toaster.showToast("Error: No user found")
        }



        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    // when the view is created bind the register TextView to its object
    // reference and handle oncClick event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backButton = view.findViewById(R.id.iv_backBtn)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        //bind reference object to fragment xml component
        logoutButton = view.findViewById(R.id.btn_logout)
        // handle onClick event
        logoutButton.setOnClickListener {
            // show a confirmation dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { dialog, which ->
                    // if the user confirms, navigate to the LoginFragment
                    FirebaseAuthService.logout()
                    NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
                }
                .setNegativeButton("No") { dialog, which ->
                    // if the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        //bind reference object to fragment xml component
        deleteButton = view.findViewById(R.id.btn_delete_account)
        // handle onClick event
        deleteButton.setOnClickListener {
            // show a confirmation dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes") { dialog, which ->
                    // if the user confirms, navigate to the LoginFragment
                    NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
                }
                .setNegativeButton("No") { dialog, which ->
                    // if the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }


        notiSwitch = view.findViewById(R.id.switch_notifications)
        notiSwitch.setOnClickListener {
            if (notiSwitch.isChecked)
            {

            }
            else
            {

            }
        }

        themeSwitch = view.findViewById(R.id.switch_theme)
        themeSwitch.setOnClickListener {

        }

        langSwitch = view.findViewById(R.id.switch_language)
        langSwitch.setOnClickListener {

        }

    }

}