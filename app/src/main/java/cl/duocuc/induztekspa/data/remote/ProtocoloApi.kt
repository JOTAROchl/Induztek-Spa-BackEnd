package cl.duocuc.induztekspa.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProtocoloApi {
    @GET("api/protocolos")
    suspend fun obtenerProtocolos(
        @Query("correo") correo: String? = null,
        @Query("rol") rol: String? = null
    ): Response<List<ProtocoloDto>>

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Body dto: ProtocoloDto): Response<ProtocoloDto>

    @PUT("api/protocolos/{id}")
    suspend fun actualizarProtocolo(@Path("id") id: Int, @Body dto: ProtocoloDto): Response<ProtocoloDto>

    @DELETE("api/protocolos/{id}")
    suspend fun eliminarProtocolo(@Path("id") id: Int): Response<Void>
}
