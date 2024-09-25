/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 25/09/2024
Last Modified: 25/09/2024
 */
package st10036509.countify.service

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricService(private val activity: FragmentActivity) {

    private val executor = ContextCompat.getMainExecutor(activity)

    fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Countify")
            .setSubtitle("Verify your identity")
            .setNegativeButtonText("Use account password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________