package st10036509.countify.user_interface.account
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
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
import st10036509.countify.service.AdviceApiService
import st10036509.countify.model.AdviceResponse

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"

    // UI components
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

    // Firebase and user data
    private val currentUser = FirebaseAuthService.getCurrentUser()
    private lateinit var toaster: Toaster

    // Inflate the fragment layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: inflating fragment layout")
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        nameDisplay = view.findViewById(R.id.name_input_txt)
        surnameDisplay = view.findViewById(R.id.surname_input_txt)
        emailDisplay = view.findViewById(R.id.email_input_txt)

        val notiDisplay = view.findViewById<Switch>(R.id.switch_notifications)
        val themeDisplay = view.findViewById<Switch>(R.id.switch_theme)
        val langDisplay = view.findViewById<Switch>(R.id.switch_language)

        toaster = Toaster(this)
        toaster.showToast(UserManager.currentUser?.email.toString())
        Log.d(TAG, "onCreateView: Toaster initialized and toast displayed")

        // Populate user data if available
        if (currentUser != null) {
            Log.d(TAG, "onCreateView: Current user found, populating data")
            val firstName = UserManager.currentUser?.firstName ?: "N/A"
            val lastName = UserManager.currentUser?.lastName ?: "N/A"
            val email = UserManager.currentUser?.email ?: "N/A"

            val notiOption = UserManager.currentUser?.notificationsEnabled ?: false
            val themeOption = UserManager.currentUser?.darkModeEnabled ?: false
            val langOption = UserManager.currentUser?.language ?: "en"

            // Set user data to views
            nameDisplay.text = firstName
            surnameDisplay.text = lastName
            emailDisplay.text = email

            notiDisplay?.isChecked = notiOption
            themeDisplay?.isChecked = themeOption
            langDisplay?.isChecked = langOption == 0

            Log.d(TAG, "onCreateView: User data populated")
        } else {
            Log.d(TAG, "onCreateView: No current user found")
        }

        return view
    }

    // Bind and set up event handlers for the views
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: View created and initializing components")

        // Initialize Retrofit for fetching advice
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.adviceslip.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val adviceApiService = retrofit.create(AdviceApiService::class.java)
        fetchRandomAdvice(adviceApiService)

        // Bind views
        notiText = view.findViewById(R.id.noti_txt)
        themeText = view.findViewById(R.id.theme_text)
        langText = view.findViewById(R.id.lang_text)

        backButton = view.findViewById(R.id.iv_backBtn)
        backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked, navigating back")
            requireActivity().supportFragmentManager.popBackStack()
        }

        logoutButton = view.findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            Log.d(TAG, "Logout button clicked, showing confirmation dialog")
            showLogoutConfirmation()
        }

        deleteButton = view.findViewById(R.id.btn_delete_account)
        deleteButton.setOnClickListener {
            Log.d(TAG, "Delete button clicked, showing confirmation dialog")
            showDeleteConfirmation()
        }

        // Firestore and Firebase Auth setup
        val db = FirebaseFirestore.getInstance()
        val userId = UserManager.currentUser?.userId
        Log.d(TAG, "onViewCreated: Firebase Firestore and Auth initialized")

        // Set up switch listeners
        setupSwitchListeners(db, userId)
    }

    private fun setupSwitchListeners(db: FirebaseFirestore, userId: String?) {
        Log.d(TAG, "Setting up switch listeners")

        // Notifications switch
        notiSwitch = view?.findViewById(R.id.switch_notifications)!!
        notiSwitch.setOnClickListener {
            Log.d(TAG, "Notifications switch toggled")
            handleNotificationSwitch(db, userId)
        }

        // Theme switch
        themeSwitch = view?.findViewById(R.id.switch_theme)!!
        themeSwitch.setOnClickListener {
            Log.d(TAG, "Theme switch toggled")
            handleThemeSwitch(db, userId)
        }

        // Language switch
        langSwitch = view?.findViewById(R.id.switch_language)!!
        langSwitch.setOnClickListener {
            Log.d(TAG, "Language switch toggled")
            handleLanguageSwitch(db, userId)
        }
    }

    private fun handleNotificationSwitch(db: FirebaseFirestore, userId: String?) {
        val isChecked = notiSwitch.isChecked
        toaster.showToast(if (isChecked) "Notifications: Turned On." else "Notifications: Turned Off.")
        notiText.text = if (isChecked) getString(R.string.noti_on_lbl) else getString(R.string.noti_off_lbl)
        UserManager.currentUser?.notificationsEnabled = isChecked

        userId?.let { id ->
            db.collection("users").document(id).update("notificationsEnabled", isChecked)
            Log.d(TAG, "Notification setting updated to $isChecked in Firestore")
        }
    }

    private fun handleThemeSwitch(db: FirebaseFirestore, userId: String?) {
        val isChecked = themeSwitch.isChecked
        toaster.showToast(if (isChecked) "Theme: Dark Mode Coming Soon..." else "Theme: Light On.")
        themeText.text = if (isChecked) getString(R.string.theme_dark_lbl) else getString(R.string.theme_light_lbl)
        UserManager.currentUser?.darkModeEnabled = isChecked

        userId?.let { id ->
            db.collection("users").document(id).update("darkModeEnabled", isChecked)
            Log.d(TAG, "Theme setting updated to ${if (isChecked) "Dark Mode" else "Light Mode"} in Firestore")
        }
    }

    private fun handleLanguageSwitch(db: FirebaseFirestore, userId: String?) {
        val isChecked = langSwitch.isChecked
        val languageValue = if (isChecked) 0 else 1  // 0 for English, 1 for Afrikaans
        toaster.showToast(if (isChecked) "Language: English." else "Taal: Afrikaans.")

        langText.text = if (isChecked) getString(R.string.language_eng_lbl) else getString(R.string.language_afr_lbl)
        UserManager.currentUser?.language = languageValue
        setAppLocale(if (isChecked) "default" else "af", requireContext())
        requireActivity().recreate()

        userId?.let { id ->
            db.collection("users").document(id).update("language", languageValue)  // Save 0 or 1 in Firestore
            Log.d(TAG, "Language setting updated to ${if (isChecked) "English (0)" else "Afrikaans (1)"} in Firestore")
        }
    }


    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuthService.logout()
                NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
                Log.d(TAG, "User logged out and navigated to LoginFragment")
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Log.d(TAG, "Logout cancelled")
            }
            .show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
                Log.d(TAG, "Account deletion confirmed, navigating to LoginFragment")
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Log.d(TAG, "Account deletion cancelled")
            }
            .show()
    }

    // Set the app's language
    private fun setAppLocale(language: String, context: Context) {
        Log.d(TAG, "Setting app locale to $language")
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    // Fetch random advice from API
    private fun fetchRandomAdvice(adviceApiService: AdviceApiService) {
        Log.d(TAG, "Fetching random advice from API")
        adviceApiService.getRandomAdvice().enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                val adviceText = response.body()?.slip?.advice ?: "No advice available."
                view?.findViewById<TextView>(R.id.adviceTextView)?.text = adviceText
                Log.d(TAG, "Random advice fetched successfully: $adviceText")
            }

            override fun onFailure(call: Call<AdviceResponse>, t: Throwable) {
                view?.findViewById<TextView>(R.id.adviceTextView)?.text = "Failed to fetch advice. Check your connection."
                Log.e(TAG, "Failed to fetch random advice: ${t.message}")
            }
        })
    }
}
