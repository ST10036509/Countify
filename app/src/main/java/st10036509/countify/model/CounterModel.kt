package st10036509.countify.model

import com.google.firebase.auth.FirebaseAuth
import st10036509.countify.service.FirestoreService

data class CounterModel(
    val userId: String = "",
    val counterId: String? = null,
    var name: String = "",
    var startValue: Int = 0, // initial value of the counter
    var changeValue: Int = 1, // the value which the counter will change by on inc/dec
    var repetition: String = "none", // can be daily, weekly, monthly, yearly, or none
    val createdTimestamp: Long = System.currentTimeMillis(), // date the counter was created (handle the notifications)
    var currentValue: Int = 0 // current value of the counter with default value
)
