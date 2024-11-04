package st10036509.countify.user_interface.counter

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import st10036509.countify.R
import st10036509.countify.adapter.CounterAdapter
import st10036509.countify.model.CounterModel
import st10036509.countify.model.UserManager
import st10036509.countify.service.CounterDatabaseHelper
import st10036509.countify.service.FirebaseAuthService
import st10036509.countify.service.FirestoreService
import st10036509.countify.service.NavigationService
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.account.SettingsFragment
import java.util.Locale


class CounterViewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var counterAdapter: CounterAdapter
    private lateinit var addCounterButton: FloatingActionButton
    private lateinit var settingsButton: ImageView
    private lateinit var toaster: Toaster
    private val firestore = FirebaseFirestore.getInstance()
    private var counterList: MutableList<CounterModel> = mutableListOf()
    private var currentUser: FirebaseUser? = null
    private var registration: ListenerRegistration? = null

    // method to fetch all counters on page start
    override fun onStart() {
        super.onStart()
        fetchCountersFromLocalDatabase()
        fetchCountersFromFirestore() // fetch counters when the fragment starts
    }

    // method to remove the listener when the page is closed
    override fun onStop() {
        super.onStop()
        registration?.remove() // remove the listener to avoid leaks
    }
    override fun onResume() {
        super.onResume()
        val dbHelper = CounterDatabaseHelper(context ?: return)
        if (isConnected(context ?: return)) {
            syncUnsyncedCounters()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_counter_view, container, false)

        recyclerView = view.findViewById(R.id.rv_itemsView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        currentUser = FirebaseAuthService.getCurrentUser()

        //fetchCountersFromFirestore()
        setAppLocale(if (UserManager.currentUser?.language == 1) "af" else "default", requireContext())

        // Set up ItemTouchHelper for swipe-to-delete functionality
        setupSwipeToDelete()

        //initialize the counter list
        counterList = mutableListOf()

        //initialize the adapter
        counterAdapter = CounterAdapter(counterList, this)

        //set the adapter to your RecyclerView
        recyclerView.adapter = counterAdapter

        fetchCountersFromLocalDatabase()

        //initializing toaster
        toaster = Toaster(this)

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
        val userID = currentUser?.uid
        firestore.collection("counters")
            .whereEqualTo("userId", userID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    toaster.showToast(getString(R.string.counter_pull_failed))
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    counterList.clear()
                    for (document in snapshot.documents) {
                        val counter = CounterModel(
                            counterId = document.id,
                            name = document.getString("name") ?: "",
                            changeValue = document.getLong("changeValue")?.toInt() ?: 0,
                            count = document.getLong("count")?.toInt() ?: 1,
                            startValue =  document.getLong("startValue")?.toInt() ?: 1,
                            createdTimestamp = document.getLong("createdTimestamp") ?: 0L,
                            repetition = document.getString("repetition") ?: "",
                            userId = document.getString("userId") ?: "",
                            synced = (document.getBoolean("synced") ?: 0) as Boolean,
                            )
                        counterList.add(counter)
                    }
                    //notify the adapter that the data has changed
                    counterAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }


    fun syncUnsyncedCounters() {
        val dbHelper = CounterDatabaseHelper(context ?: return)
        val unsyncedCounters = dbHelper.getUnsyncedCounters()

        if (isConnected(context ?: return)) {
            for (counter in unsyncedCounters) {
                FirestoreService.addCounter(counter) { success, error ->
                    if (success) {
                        // Mark counter as synced in the local SQLite database
                        counter.synced = true
                        dbHelper.updateCounter(counter)

                        // Remove the synced counter from the SQLite database
                        dbHelper.deleteCounter(counter.counterId)

                        Toast.makeText(
                            context,
                            "Synced counter: ${counter.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(
                            "syncUnsyncedCounters",
                            "Failed to sync counter: ${counter.name} - $error"
                        )
                    }
                }
            }
        }
    }


    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun fetchCountersFromLocalDatabase() {
        val dbHelper = CounterDatabaseHelper(context ?: return) // Pass the context of your activity/fragment
        val userId = currentUser?.uid ?: return

        // Fetch counters from SQLite
        val counterList = dbHelper.getCountersByUserId(userId)

        // Update your adapter or UI with the retrieved counter list
        counterAdapter.updateData(counterList)
        counterAdapter.notifyDataSetChanged()
    }


    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    val counterToDelete = counterList[position]

                    //remove from Firestore
                    counterToDelete.counterId?.let {
                        firestore.collection("counters").document(it)
                            .delete()
                            .addOnSuccessListener {
                                //remove from the list only after Firestore delete is successful
                                //notify the adapter that the data has changed
                                counterAdapter.notifyItemRemoved(position)
                                toaster.showToast(getString(R.string.counter_delete_successful))
                            }
                            .addOnFailureListener { e ->
                                toaster.showToast(getString(R.string.counter_delete_failed))
                            }
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                // Draw background and icon while swiping
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_icon)
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + deleteIcon.intrinsicHeight

                val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                deleteIcon.draw(c)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
