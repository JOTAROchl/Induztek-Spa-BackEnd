package cl.duocuc.induztekspa.data.remote

import cl.duocuc.induztekspa.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioApi {
    @GET("api/usuarios")
    suspend fun obtenerUsuarios(): Response<List<Usuario>>

    @POST("api/usuarios")
    suspend fun registrarUsuario(@Body usuario: Usuario): Response<Usuario>

    @POST("api/usuarios/login")
    suspend fun login(@Body credenciales: Map<String, String>): Response<Usuario>

    @PUT("api/usuarios/{id}/aprobar")
    suspend fun aprobarUsuario(@Path("id") id: Int): Response<Void>

    @PUT("api/usuarios/{id}/rol")
    suspend fun actualizarRol(@Path("id") id: Int, @Body payload: Map<String, Any>): Response<Void>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<Void>
}
