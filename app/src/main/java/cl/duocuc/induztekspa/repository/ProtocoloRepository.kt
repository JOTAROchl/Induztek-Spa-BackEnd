package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.ProtocoloDbHelper
import cl.duocuc.induztekspa.data.remote.ProtocoloDto
import cl.duocuc.induztekspa.data.remote.RetrofitClient
import cl.duocuc.induztekspa.model.Protocolo

class ProtocoloRepository(context: Context) {
    private val dbHelper = ProtocoloDbHelper(context)

    suspend fun obtenerProtocolosRemote(correoUsuario: String, rol: String): Result<List<Protocolo>> {
        val correoLimpio = correoUsuario.ifBlank { "admin@induztek.cl" }
        return try {
            val resp = RetrofitClient.protocoloApi.obtenerProtocolos(correoLimpio)
            if (resp.isSuccessful && resp.body() != null) {
                val lista = resp.body()!!.map { dto ->
                    Protocolo(
                        id = dto.id,
                        codigo = "PT-2026-${dto.id}",
                        equipo = dto.equipo,
                        fechaHora = dto.fecha,
                        estado = dto.estado,
                        observaciones = dto.observaciones ?: "",
                        correoUsuario = dto.correoUsuario,
                        temperatura = dto.temperatura ?: "",
                        humedad = dto.humedad ?: ""
                    )
                }
                Result.success(lista)
            } else {
                Result.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.localizedMessage}"))
        }
    }

    suspend fun crearProtocoloRemote(protocolo: Protocolo): Result<Boolean> {
        val fechaLimpia = protocolo.fechaHora.split(" ").firstOrNull() ?: protocolo.fechaHora
        val dto = ProtocoloDto(
            id = 0,
            equipo = protocolo.equipo,
            fecha = fechaLimpia,
            estado = protocolo.estado,
            observaciones = protocolo.observaciones,
            correoUsuario = protocolo.correoUsuario,
            temperatura = protocolo.temperatura,
            humedad = protocolo.humedad
        )
        return try {
            val resp = RetrofitClient.protocoloApi.crearProtocolo(dto)
            if (resp.isSuccessful) {
                dbHelper.insertarProtocolo(protocolo)
                Result.success(true)
            } else {
                val errMsj = resp.errorBody()?.string() ?: resp.message()
                Result.failure(Exception("Servidor HTTP ${resp.code()}: $errMsj"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    suspend fun actualizarProtocoloRemote(protocolo: Protocolo): Result<Boolean> {
        val fechaLimpia = protocolo.fechaHora.split(" ").firstOrNull() ?: protocolo.fechaHora
        val dto = ProtocoloDto(
            id = protocolo.id,
            equipo = protocolo.equipo,
            fecha = fechaLimpia,
            estado = protocolo.estado,
            observaciones = protocolo.observaciones,
            correoUsuario = protocolo.correoUsuario,
            temperatura = protocolo.temperatura,
            humedad = protocolo.humedad
        )
        return try {
            val resp = RetrofitClient.protocoloApi.actualizarProtocolo(protocolo.id, protocolo.correoUsuario, dto)
            if (resp.isSuccessful) {
                dbHelper.actualizarProtocolo(protocolo)
                Result.success(true)
            } else {
                Result.failure(Exception("Servidor HTTP ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    suspend fun eliminarProtocoloRemote(id: Int): Result<Boolean> {
        return try {
            val resp = RetrofitClient.protocoloApi.eliminarProtocolo(id, "admin@induztek.cl")
            if (resp.isSuccessful) {
                dbHelper.eliminarProtocolo(id)
                Result.success(true)
            } else {
                Result.failure(Exception("Servidor HTTP ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        return dbHelper.obtenerProtocoloAnterior(equipo, protocoloActualId)
    }

    fun actualizarCorreoUsuario(correoAntiguo: String, nuevoCorreo: String) {
        dbHelper.actualizarCorreoUsuario(correoAntiguo, nuevoCorreo)
    }
}
