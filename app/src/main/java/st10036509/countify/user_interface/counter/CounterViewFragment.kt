package st10036509.countify.user_interface.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import st10036509.countify.R
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.RegisterFragment

class CounterViewFragment : Fragment()
{

    private lateinit var addCounterButton: FloatingActionButton

    private lateinit var  settingsButton: ImageView

    // on fragment creation inflate the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_counter_view, container, false)
    }

    // when the view is created bind the register TextView to its object
    // reference and handle oncClick event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCounterButton = view.findViewById(R.id.fab_addItemButton)

        addCounterButton.setOnClickListener{
            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
        }

        settingsButton = view.findViewById(R.id.iv_settingsIcon)

        settingsButton.setOnClickListener{
            NavigationService.navigateToFragment(CounterViewFragment(), R.id.fragment_container)
        }
    }
}