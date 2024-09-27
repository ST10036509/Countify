package st10036509.countify

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import st10036509.countify.user_interface.counter.CounterCreationFragment
import st10036509.countify.service.FirebaseAuthService

class CounterCreationFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<CounterCreationFragment>

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fragmentScenario = FragmentScenario.launchInContainer(CounterCreationFragment::class.java)

        // Mock Firebase Auth behavior
        Mockito.`when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        Mockito.`when`(mockFirebaseUser.uid).thenReturn("testUserId")

        // You can set the mocked FirebaseAuth as the instance in your FirebaseAuthService if needed
        // FirebaseAuthService.setAuthInstance(mockFirebaseAuth)
    }

    @Test
    fun testValidateUserTitleInput_WithEmptyTitle_ShouldShowToast() {
        fragmentScenario.onFragment { fragment ->
            val titleInput: EditText = fragment.view!!.findViewById(R.id.title_input)
            titleInput.setText("")  // Set empty title

            val isValid = fragment.validateUserTitleInput()

            assert(!isValid)  // Expect validation to fail
            // Here you can add further checks to ensure the correct Toast message was shown
        }
    }

    @Test
    fun testIncrementStartValue() {
        fragmentScenario.onFragment { fragment ->
            val startValue: TextView = fragment.view!!.findViewById(R.id.start_value)
            fragment.incrementStartValue()  // Call the method
            assert(startValue.text.toString().toInt() == 1)  // Expect value to be incremented to 1
        }
    }

    @Test
    fun testDecrementStartValue_WhenGreaterThanZero() {
        fragmentScenario.onFragment { fragment ->
            val startValue: TextView = fragment.view!!.findViewById(R.id.start_value)
            fragment.incrementStartValue()  // Start at 1
            fragment.decrementStartValue()  // Call the method
            assert(startValue.text.toString().toInt() == 0)  // Expect value to be decremented to 0
        }
    }

    @Test
    fun testDecrementStartValue_WhenZero_ShouldNotChange() {
        fragmentScenario.onFragment { fragment ->
            val startValue: TextView = fragment.view!!.findViewById(R.id.start_value)
            fragment.decrementStartValue()  // Call the method
            assert(startValue.text.toString().toInt() == 0)  // Expect value to remain 0
        }
    }

    // More tests can be added here, for example for incrementing values and adding counters to the database

}
