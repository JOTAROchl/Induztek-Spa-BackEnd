package cl.duocuc.induztekspa.data.remote

import retrofit2.http.GET

interface RolApi {
    @GET("api/roles")
    suspend fun obtenerRoles(): List<String>
}
