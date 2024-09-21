package st10036509.countify.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import st10036509.countify.user_interface.account.LoginFragment
import st10036509.countify.user_interface.account.RegisterFragment

fun Fragment.hideKeyboard() {

    val inputMethodManager =
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    val currentFocusView = activity?.currentFocus

    currentFocusView?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun RegisterFragment.RegisterInputs.toMutableListRegister(): MutableList<String> {
    return mutableListOf(firstName, lastName, phoneNumber, email, password, confirmPassword)
}

fun LoginFragment.LoginInputs.toMutableListLogin(): MutableList<String> {
    return mutableListOf(email, password)
}
