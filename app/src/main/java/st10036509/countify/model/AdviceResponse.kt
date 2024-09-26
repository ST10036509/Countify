package st10036509.countify.model

data class Slip(
    val id: Int,
    val advice: String
)

data class AdviceResponse(
    val slip: Slip
)
