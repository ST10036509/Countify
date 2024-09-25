/*
Author: Ethan Schoonbee
Student Number: ST10036509
Date Created: 25/09/2024
Last Modified: 25/09/2024
 */

package st10036509.countify.service

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// class to handle biometric authentication
class BiometricService(private val activity: FragmentActivity) {

    // executor to run biometric prompt actions on the main thread
    private val executor = ContextCompat.getMainExecutor(activity)

    // function to show the biometric prompt and handle authentication callbacks
    fun showBiometricPrompt(
        onSuccess: () -> Unit,     // called when authentication succeeds
        onFailure: () -> Unit,     // called when authentication fails
        onError: (String) -> Unit  // called when there is an error during authentication
    ) {
        Log.i("Biometric Service:", "Creating a Biometric Prompt")

        // creating a biometric prompt with authentication callback handlers
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                // handle authentication error
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.i("Biometric Service:", "Biometric Authentication Error - ${errString.toString()}")
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                // handle successful authentication
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.i("Biometric Service:", "Biometric Authentication Successful")
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                // handle failed authentication
                override fun onAuthenticationFailed() {
                    Log.i("Biometric Service:", "Biometric Authentication Failed")
                    super.onAuthenticationFailed()
                    onFailure()
                }
            })

        Log.i("Biometric Service:", "Biometric Prompt Settings Being Established...")
        // setting up biometric prompt details
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("biometric login for countify")        // title for the prompt
            .setSubtitle("verify your identity")             // subtitle for the prompt
            .setNegativeButtonText("use account password")   // text for negative button
            .build()

        Log.i("Biometric Service:", "Showing Biometric Prompt")
        // showing the biometric prompt to the user
        biometricPrompt.authenticate(promptInfo)
    }
}
//__________________________....oooOO0_END_OF_FILE_0OOooo....__________________________