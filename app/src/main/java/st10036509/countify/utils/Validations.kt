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

fun isTitleNull(title: String): Boolean {
    var isEmpty: Boolean = false
    if(title.isEmpty() || title.isBlank())
    {
        isEmpty = true
    }
    return isEmpty
}

fun isMoreThanZero(number: Int): Boolean{
    var isMoreThanZero: Boolean = false

    if (number > 0 ){
        isMoreThanZero = true
    }
    return isMoreThanZero
}

fun isMoreThanOne(number: Int): Boolean{
    var isMoreThanOne: Boolean = false

    if (number > 1){
        isMoreThanOne = true
    }
    return isMoreThanOne
}