package st10036509.countify.service

import retrofit2.Call
import retrofit2.http.GET
import st10036509.countify.model.AdviceResponse

interface AdviceApiService {
    @GET("advice")
    fun getRandomAdvice(): Call<AdviceResponse>
}
