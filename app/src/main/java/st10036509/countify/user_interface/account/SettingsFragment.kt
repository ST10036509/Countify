package st10036509.countify.user_interface.account
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
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
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.MainActivity
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import java.util.Locale

class SettingsFragment : Fragment() {

    // create object reference for components to handle events
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: CardView
    private lateinit var deleteButton: CardView
    private lateinit var notiSwitch: Switch
    private lateinit var themeSwitch: Switch
    private lateinit var langSwitch: Switch

    private lateinit var nameDisplay: TextView
    private lateinit var surnameDisplay: TextView
    private lateinit var emailDisplay: TextView

    // get the current user (if they are logged in)
    val currentUser = FirebaseAuthService.getCurrentUser()

//    val nameDisplay = view?.findViewById<TextView>(R.id.name_input_txt)
//    val surnameDisplay = view?.findViewById<TextView>(R.id.surname_input_txt)
//    val emailDisplay = view?.findViewById<TextView>(R.id.email_input_txt)
//
//    val notiDisplay = view?.findViewById<Switch>(R.id.switch_notifications)
//    val themeDisplay = view?.findViewById<Switch>(R.id.switch_theme)
//    val langDisplay = view?.findViewById<Switch>(R.id.switch_language)

    private lateinit var toaster: Toaster // handle toasting message

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        nameDisplay = view.findViewById(R.id.name_input_txt)
        surnameDisplay = view.findViewById(R.id.surname_input_txt)
        emailDisplay = view.findViewById(R.id.email_input_txt)

        val notiDisplay = view?.findViewById<Switch>(R.id.switch_notifications)
        val themeDisplay = view?.findViewById<Switch>(R.id.switch_theme)
        val langDisplay = view?.findViewById<Switch>(R.id.switch_language)

        toaster =  Toaster(this)
        toaster.showToast(UserManager.currentUser?.email.toString())

        if (currentUser != null) {
            val userId = currentUser.uid

            val firstName = UserManager.currentUser?.firstName ?: "N/A"
            val lastName = UserManager.currentUser?.lastName ?: "N/A"
            val email = UserManager.currentUser?.email ?: "N/A"

            val notiOption = UserManager.currentUser?.notificationsEnabled ?: false
            val themeOption = UserManager.currentUser?.darkModeEnabled ?: false
            val langOption = UserManager.currentUser?.language ?: "en"

            // Use the retrieved data
            nameDisplay?.text = firstName
            surnameDisplay?.text = lastName
            emailDisplay?.text = email


//            if (notiOption != null) {
//                notiDisplay?.isChecked = notiOption
//            }
//            if (themeOption != null) {
//                themeDisplay?.isChecked = themeOption
//            }
//            if (langOption == "0")
//            {
//                langDisplay?.isChecked = false
//            }

        }

        // inflate the layout for this fragment
        return view
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


        //--------Switch Handling-------//
        //notifications
        notiSwitch = view.findViewById(R.id.switch_notifications)
        notiSwitch.setOnClickListener {
            if (notiSwitch.isChecked) {
                toaster.showToast("Notifications: Turned On.")
            } else {
                toaster.showToast("Notifications: Turned Off.")
            }
        }
        //theme
        themeSwitch = view.findViewById(R.id.switch_theme)
        themeSwitch.setOnClickListener {
            if (themeSwitch.isChecked) {
                toaster.showToast("Theme: Light On.")
            } else {
                toaster.showToast("Theme: Dark Off.")
            }
        }
        //language
        langSwitch = view.findViewById(R.id.switch_language)
        langSwitch.setOnClickListener {
            if (langSwitch.isChecked) {
                // Set language to English
                toaster.showToast("Language: English.")
                setAppLocale("default", requireContext())
                requireActivity().recreate()

            } else {
                // Set language to Afrikaans
                toaster.showToast("Taal: Afrikaans.")
                setAppLocale("af", requireContext())
                requireActivity().recreate()
            }
        }
    }

    //Language Selector Method
    fun setAppLocale(language: String, context: Context) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}