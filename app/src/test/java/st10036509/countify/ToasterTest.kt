package st10036509.countify

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import st10036509.countify.service.Toaster

class ToasterTest {

    private lateinit var toaster: Toaster
    private val mockFragment = mock(Fragment::class.java)
    private val mockContext = mock(Context::class.java)

    @Before
    fun setUp() {

        `when`(mockFragment.requireContext()).thenReturn(mockContext)

        toaster = Toaster(mockFragment)
    }

    @Test
    fun `test showToast with non-null message`() {
        // Mock the Toast.makeText behavior to avoid calling Android system
        val mockToast = mock(Toast::class.java)
        `when`(Toast.makeText(mockContext, "Hello World", Toast.LENGTH_SHORT)).thenReturn(mockToast)

        // Call the showToast method with a specific message
        toaster.showToast("Hello World")

        // Verify that Toast.makeText was called with the correct parameters
        verify(mockFragment).requireContext()
        verify(mockToast).show() // Verify that the toast was shown
    }
}
