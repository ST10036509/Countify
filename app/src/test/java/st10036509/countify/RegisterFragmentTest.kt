package st10036509.countify

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.account.RegisterFragment

@RunWith(AndroidJUnit4::class)
class RegisterFragmentTest {

    private lateinit var registerFragment: RegisterFragment
    private var toaster: Toaster? = null

    @Before
    fun setup() {
        registerFragment = RegisterFragment()

        // Mocking Toaster service
        toaster = mock(Toaster::class.java)
        registerFragment.toaster = toaster as Toaster
    }

    @Test
    fun `test valid inputs`() {
        // Given
        val validInputs = RegisterFragment.RegisterInputs(
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "0712345678",
            email = "john@example.com",
            password = "StrongPass1!",
            confirmPassword = "StrongPass1!"
        )

        // When
        val result = registerFragment.areInputsValid(validInputs)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test invalid phone number`() {
        // Given
        val invalidPhoneNumberInputs = RegisterFragment.RegisterInputs(
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "12345", // invalid
            email = "john@example.com",
            password = "StrongPass1!",
            confirmPassword = "StrongPass1!"
        )

        // When
        val result = registerFragment.areInputsValid(invalidPhoneNumberInputs)

        // Then
        assertFalse(result)
        verify(toaster)?.showToast(anyString())
    }

    @Test
    fun `test password mismatch`() {
        // Given
        val passwordMismatchInputs = RegisterFragment.RegisterInputs(
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "0712345678",
            email = "john@example.com",
            password = "StrongPass1!",
            confirmPassword = "WeakPass2!"
        )

        // When
        val result = registerFragment.areInputsValid(passwordMismatchInputs)

        // Then
        assertFalse(result)
        verify(toaster)?.showToast(anyString())
    }

    @Test
    fun `test weak password`() {
        // Given
        val weakPasswordInputs = RegisterFragment.RegisterInputs(
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "0712345678",
            email = "john@example.com",
            password = "12345", // weak password
            confirmPassword = "12345"
        )

        // When
        val result = registerFragment.areInputsValid(weakPasswordInputs)

        // Then
        assertFalse(result)
        verify(toaster)?.showToast(anyString())
    }

}
