package st10036509.countify.model

data class UserModel(
    var userId: String = "", // users document uid
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var notificationsEnabled: Boolean = true, // true = receive notifications || false = don't resized notifications
    var darkModeEnabled: Boolean = false, // false = lightMode || true = darkMode
    var language: String = "English" // Afrikaans or English
)
