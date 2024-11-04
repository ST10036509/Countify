package st10036509.countify.model


data class CounterModel(
    val id: Int? = null,
    val userId: String = "",
    var counterId: String? = null,
    var name: String = "",
    var changeValue: Int = 1, // the value which the counter will change by on inc/dec
    var repetition: String = "none", // can be daily, weekly, monthly, yearly, or none
    val createdTimestamp: Long = System.currentTimeMillis(), // date the counter was created (handle the notifications)
    val lastReset: Long = System.currentTimeMillis(),
    var startValue: Int = 0,
    var count: Int = 0, // current value of the counter with default value
    var synced: Boolean
)
