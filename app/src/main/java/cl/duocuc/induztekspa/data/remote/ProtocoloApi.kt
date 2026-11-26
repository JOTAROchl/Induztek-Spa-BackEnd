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
        @Query("correoUsuario") correoUsuario: String
    ): Response<List<ProtocoloDto>>

    @POST("api/protocolos")
    suspend fun crearProtocolo(@Body dto: ProtocoloDto): Response<ProtocoloDto>

    @PUT("api/protocolos/{id}")
    suspend fun actualizarProtocolo(
        @Path("id") id: Int,
        @Query("correoUsuario") correoUsuario: String,
        @Body dto: ProtocoloDto
    ): Response<ProtocoloDto>

    @DELETE("api/protocolos/{id}")
    suspend fun eliminarProtocolo(
        @Path("id") id: Int,
        @Query("correoUsuario") correoUsuario: String
    ): Response<Void>
}
