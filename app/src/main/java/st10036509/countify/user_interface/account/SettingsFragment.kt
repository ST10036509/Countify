package st10036509.countify.user_interface.account
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import st10036509.countify.AdviceApiService
import st10036509.countify.model.AdviceResponse

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
    private lateinit var notiText: TextView
    private lateinit var themeText: TextView
    private lateinit var langText: TextView

    // get the current user (if they are logged in)
    val currentUser = FirebaseAuthService.getCurrentUser()
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
            val firstName = UserManager.currentUser?.firstName ?: "N/A"
            val lastName = UserManager.currentUser?.lastName ?: "N/A"
            val email = UserManager.currentUser?.email ?: "N/A"

            val notiOption = UserManager.currentUser?.notificationsEnabled ?: false
            val themeOption = UserManager.currentUser?.darkModeEnabled ?: false
            val langOption = UserManager.currentUser?.language ?: "en"

            // Use the retrieved data
            nameDisplay.text = firstName
            surnameDisplay.text = lastName
            emailDisplay.text = email

            notiDisplay?.isChecked = notiOption
            themeDisplay?.isChecked = themeOption
            if (langOption == 0)
            {
                langDisplay?.isChecked = true
            }
            else
            {
                langDisplay?.isChecked = false
            }
        }
        // inflate the layout for this fragment
        return view
    }

    // when the view is created bind the register TextView to its object
    // reference and handle oncClick event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize Retrofit for the API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.adviceslip.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val adviceApiService = retrofit.create(AdviceApiService::class.java)

        // Fetch the random advice when fragment is opened
        fetchRandomAdvice(adviceApiService)


        val userId = UserManager.currentUser?.userId

        notiText = view.findViewById(R.id.noti_txt)
        themeText = view.findViewById(R.id.theme_text)
        langText = view.findViewById(R.id.lang_text)

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

        // Set up Firestore and FirebaseAuth instances
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        //--------Switch Handling-------//
        //notifications
        notiSwitch = view.findViewById(R.id.switch_notifications)
        notiSwitch.setOnClickListener {
            if (notiSwitch.isChecked) {
                toaster.showToast("Notifications: Turned On.")
                notiText.text = getString(R.string.noti_on_lbl)
                UserManager.currentUser?.notificationsEnabled = true
                val userRef = userId?.let { it1 -> db.collection("users").document(it1) }
                if (userRef != null) {
                    userRef.update("notificationsEnabled", true)
                }
            } else {
                toaster.showToast("Notifications: Turned Off.")
                notiText.text = getString(R.string.noti_off_lbl)
                UserManager.currentUser?.notificationsEnabled = false
                val userRef = userId?.let { it1 -> db.collection("users").document(it1) }
                if (userRef != null) {
                    userRef.update("notificationsEnabled", false)
                }
            }
        }
        //theme
        themeSwitch = view.findViewById(R.id.switch_theme)
        themeSwitch.setOnClickListener {
            if (themeSwitch.isChecked) {
                toaster.showToast("Theme: Dark Mode Coming Soon...")
                themeText.text = getString(R.string.theme_dark_lbl)
                UserManager.currentUser?.darkModeEnabled = true
            } else {
                toaster.showToast("Theme: Light On.")
                themeText.text = getString(R.string.theme_light_lbl)
                UserManager.currentUser?.darkModeEnabled = false
            }
        }
        //language
        langSwitch = view.findViewById(R.id.switch_language)
        langSwitch.setOnClickListener {
            if (langSwitch.isChecked) {
                // Set language to English
                toaster.showToast("Language: English.")
                langText.text = getString(R.string.language_eng_lbl)
                UserManager.currentUser?.language = 0
                setAppLocale("default", requireContext())
                requireActivity().recreate()
            } else {
                // Set language to Afrikaans
                toaster.showToast("Taal: Afrikaans.")
                langText.text = getString(R.string.language_afr_lbl)
                UserManager.currentUser?.language = 1
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

    private fun fetchRandomAdvice(adviceApiService: AdviceApiService) {
        adviceApiService.getRandomAdvice().enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                if (response.isSuccessful) {
                    val advice = response.body()?.slip?.advice
                    view?.findViewById<TextView>(R.id.adviceTextView)?.text = advice
                } else {
                    view?.findViewById<TextView>(R.id.adviceTextView)?.text = "If they're old enough to pee,, they're old enough for me. You have no internet connection by the way."
                }
            }

            override fun onFailure(call: Call<AdviceResponse>, t: Throwable) {
                view?.findViewById<TextView>(R.id.adviceTextView)?.text = "If they're old enough to pee, they're old enough for me. \nYou have no internet connection by the way."
            }
        })
    }
}