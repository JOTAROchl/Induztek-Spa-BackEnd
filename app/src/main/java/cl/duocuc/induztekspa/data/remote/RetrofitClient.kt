package cl.duocuc.induztekspa.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val ROLES_BASE_URL = "https://7r0clr98-8081.brs.devtunnels.ms/"
    private const val USERS_BASE_URL = "https://7r0clr98-8082.brs.devtunnels.ms/"
    private const val PROTOCOLS_BASE_URL = "https://7r0clr98-8083.brs.devtunnels.ms/"

    val nominatimApi: NominatimApi by lazy {
        retrofitFor(NOMINATIM_BASE_URL).create(NominatimApi::class.java)
    }

    val usuarioApi: UsuarioApi by lazy {
        retrofitFor(USERS_BASE_URL).create(UsuarioApi::class.java)
    }

    val protocoloApi: ProtocoloApi by lazy {
        retrofitFor(PROTOCOLS_BASE_URL).create(ProtocoloApi::class.java)
    }

    val rolApi: RolApi by lazy {
        retrofitFor(ROLES_BASE_URL).create(RolApi::class.java)
    }

    private fun retrofitFor(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
