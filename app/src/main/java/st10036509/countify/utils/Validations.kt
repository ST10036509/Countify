package st10036509.countify.utils

fun areNullInputs(inputs: MutableList<String>): Boolean {

    var areNullInputs: Boolean = false;

    inputs.forEach { it ->
        if (it.isNullOrEmpty()) {
            areNullInputs = true
        }
    }

    return areNullInputs
}

fun stringsMatch(stringOne: String, stringTwo: String): Boolean {
    return (stringOne ==  stringTwo)
}

fun isPasswordStrong(password: String): Boolean {
    val passwordPattern =  "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&@#])[A-Za-z\\d@\$!%*?&\\#]{8,}\$"
    val regex = Regex(passwordPattern)

    return regex.matches(password)
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val phonePattern = "^0\\d{9}\$"
    val regex = Regex(phonePattern)

    return regex.matches(phoneNumber)
}