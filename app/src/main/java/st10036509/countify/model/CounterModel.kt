package st10036509.countify.model

data class CounterModel(
    val counterId: String = "", // counter uid
    var name: String = "",
    var startValue: Int = 0, // initial value of the counter
    var changeValue: Int = 1, // the value which the counter will change by on inc/dec
    var repetition: String = "none", // can be daily, weekly, monthly, yearly, or none
    val createdTimestamp: Long = System.currentTimeMillis() // date the counter was created (handle the notifications)
)
