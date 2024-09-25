package st10036509.countify.user_interface.counter


import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import st10036509.countify.R
import st10036509.countify.model.UserManager
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.RegisterFragment
import st10036509.countify.user_interface.account.SettingsFragment
import java.util.Locale

class CounterViewFragment : Fragment()
{

    private lateinit var addCounterButton: FloatingActionButton

    private lateinit var  settingsButton: ImageView

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setAppLocale(if (UserManager.currentUser?.language == 1) "af" else "default", requireContext())
        //requireActivity().recreate()

        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_counter_view, container, false)
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

    // when the view is created bind the register TextView to its object
    // reference and handle oncClick event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCounterButton = view.findViewById(R.id.fab_addItemButton)

        addCounterButton.setOnClickListener{
            NavigationService.navigateToFragment(CounterCreationFragment(), R.id.fragment_container)
        }

        settingsButton = view.findViewById(R.id.iv_settingsButton)

        settingsButton.setOnClickListener{
            NavigationService.navigateToFragment(SettingsFragment(), R.id.fragment_container)
        }
    }
}