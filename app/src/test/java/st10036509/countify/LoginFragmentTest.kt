
import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import st10036509.countify.R
import st10036509.countify.user_interface.account.LoginFragment

class LoginFragmentTest {

    private lateinit var scenario: FragmentScenario<LoginFragment>

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Launch the fragment with a mocked context
        scenario = FragmentScenario.launchInContainer(LoginFragment::class.java)
        `when`(mockContext.getString(R.string.null_inputs_error)).thenReturn("Inputs cannot be null")

        // You can set the context in the fragment
        scenario.onFragment { fragment ->
            // Setting the context for the fragment to use
            fragment.requireActivity().setContentView(R.layout.fragment_login) // Replace with actual layout if needed
        }
    }

    @Test
    fun `areInputsValid returns false when inputs are null`() {
        val invalidInputs = LoginFragment.LoginInputs("", "") // Example of invalid inputs

        // Call the method
        scenario.onFragment { fragment ->
            val result = fragment.areInputsValid(invalidInputs)

            // Verify the result is false when inputs are null
            assertFalse(result)
        }
    }

    @Test
    fun `areInputsValid returns true when inputs are valid`() {
        val validInputs = LoginFragment.LoginInputs("username", "password") // Example of valid inputs

        // Call the method
        scenario.onFragment { fragment ->
            val result = fragment.areInputsValid(validInputs)

            // Verify the result is true when inputs are valid
            assertTrue(result)
        }
    }
}
