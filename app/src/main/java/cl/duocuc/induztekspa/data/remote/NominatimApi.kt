package cl.duocuc.induztekspa.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NominatimApi {
    @GET("reverse?format=json")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Header("User-Agent") userAgent: String = "InduztekSpA-AndroidApp"
    ): NominatimResponse

    @GET("search?format=json&limit=1")
    suspend fun forwardGeocode(
        @Query("q") query: String,
        @Header("User-Agent") userAgent: String = "InduztekSpA-AndroidApp"
    ): List<NominatimSearchResult>
}
