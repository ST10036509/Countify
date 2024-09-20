package st10036509.countify.user_interface.account
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.counter.CounterViewFragment

class SettingsFragment : Fragment() {

    // create object reference for components to handle events
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: Button
    private lateinit var deleteButton: Button

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

    }
}