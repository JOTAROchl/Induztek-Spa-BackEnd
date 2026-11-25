package cl.duocuc.induztekspa.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    val nominatimApi: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}
