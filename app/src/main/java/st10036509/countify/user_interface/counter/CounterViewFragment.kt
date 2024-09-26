package st10036509.countify.user_interface.counter


import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.adapter.CounterAdapter
import st10036509.countify.model.CounterModel
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.model.UserManager
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.SettingsFragment
import java.util.Locale

class CounterViewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var counterAdapter: CounterAdapter
    private lateinit var addCounterButton: FloatingActionButton
    private lateinit var  settingsButton: ImageView
    private val firestore = FirebaseFirestore.getInstance()
    private var counterList: MutableList<CounterModel> = mutableListOf()
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_counter_view, container, false)

        recyclerView = view.findViewById(R.id.rv_itemsView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        currentUser = FirebaseAuthService.getCurrentUser()

        fetchCountersFromFirestore()
        setAppLocale(if (UserManager.currentUser?.language == 1) "af" else "default", requireContext())
        return view
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCounterButton = view.findViewById(R.id.fab_addItemButton)
        settingsButton = view.findViewById(R.id.iv_settingsButton)

        addCounterButton.setOnClickListener{
            NavigationService.navigateToFragment(CounterCreationFragment(), R.id.fragment_container)
        }

        settingsButton.setOnClickListener{
            NavigationService.navigateToFragment(SettingsFragment(), R.id.fragment_container)
        }
    }

    private fun fetchCountersFromFirestore() {

        val userID = currentUser?.uid;
        firestore.collection("counters_tests")
            .whereEqualTo("userId", userID)
            .get()
            .addOnSuccessListener { result ->
                counterList.clear()
                for (document in result) {
                    val counter = CounterModel(
                        counterId = document.id,  // Assign the document ID
                        name = document.getString("name") ?: "",
                        currentValue = document.getLong("currentValue")?.toInt() ?: 0,
                        changeValue = document.getLong("incrementValue")?.toInt() ?: 1,
                        startValue = document.getLong("startValue")?.toInt() ?: 0,
                        createdTimestamp = document.getLong("createdTimestamp") ?: 0L,
                        repetition = document.getString("repetition") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    counterList.add(counter)
                }
                // Setup the adapter with the fetched counters
                counterAdapter = CounterAdapter(counterList)
                recyclerView.adapter = counterAdapter
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }
}
