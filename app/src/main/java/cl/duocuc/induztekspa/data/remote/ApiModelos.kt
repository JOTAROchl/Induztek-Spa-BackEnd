package cl.duocuc.induztekspa.data.remote

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("address") val address: NominatimAddress?
)

data class NominatimAddress(
    val road: String?,
    val suburb: String?,
    val city: String?,
    val town: String?,
    val state: String?,
    val country: String?
)

data class NominatimSearchResult(
    @SerializedName("lat") val lat: String?,
    @SerializedName("lon") val lon: String?,
    @SerializedName("display_name") val displayName: String?
)

data class ProtocoloDto(
    val id: Int = 0,
    val equipo: String,
    val fecha: String,
    val estado: String,
    val observaciones: String = "",
    val correoUsuario: String,
    val temperatura: String = "",
    val humedad: String = ""
)

data class RolDto(
    val id: Int,
    val nombre: String
)
