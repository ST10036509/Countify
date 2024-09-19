package st10036509.countify.user_interface.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.counter.CounterViewFragment

class LoginFragment : Fragment() {

    // create object reference for components to handle events
    private lateinit var registerButton: TextView

    private lateinit var loginButton: CardView

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
        loginButton = view.findViewById(R.id.loginButton)

        loginButton.setOnClickListener{
            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
        }

        //bind reference object to fragment xml component
        registerButton = view.findViewById(R.id.createAccountButton)
        //handle onClick event
        registerButton.setOnClickListener {
            // use the navigation service (singleton) to navigate to the RegisterFragment
            NavigationService.navigateToFragment(RegisterFragment(), R.id.fragment_container)
        }

    }
}