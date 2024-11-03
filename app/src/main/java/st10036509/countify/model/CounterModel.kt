package st10036509.countify.model


data class CounterModel(
    val id: Long? = null,
    val userId: String = "",
    val counterId: String? = null,
    var name: String = "",
    var changeValue: Int = 1, // the value which the counter will change by on inc/dec
    var repetition: String = "none", // can be daily, weekly, monthly, yearly, or none
    val createdTimestamp: Long = System.currentTimeMillis(), // date the counter was created (handle the notifications)
    val lastReset: Long = System.currentTimeMillis(),
    var count: Int = 0, // current value of the counter with default value
    var startValue: Int = 0,
    var synced: Boolean
)
