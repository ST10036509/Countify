package st10036509.countify.user_interface.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import st10036509.countify.R
import st10036509.countify.service.NavigationService

class RegisterFragment: Fragment() {

    private lateinit var loginButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton = view.findViewById(R.id.btn_Login)
        loginButton.setOnClickListener {
            NavigationService.navigateToFragment(LoginFragment(), R.id.fragment_container)
        }
    }

}