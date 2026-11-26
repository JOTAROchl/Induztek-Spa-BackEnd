package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.ProtocoloDbHelper
import cl.duocuc.induztekspa.data.remote.ProtocoloDto
import cl.duocuc.induztekspa.data.remote.RetrofitClient
import cl.duocuc.induztekspa.model.Protocolo

class ProtocoloRepository(context: Context) {
    private val dbHelper = ProtocoloDbHelper(context)

    suspend fun obtenerProtocolosRemote(correoUsuario: String, rol: String): Result<List<Protocolo>> {
        return try {
            val resp = RetrofitClient.protocoloApi.obtenerProtocolos(correoUsuario, rol)
            if (resp.isSuccessful && resp.body() != null) {
                val lista = resp.body()!!.map { dto ->
                    Protocolo(
                        id = dto.id,
                        codigo = "PT-2026-${dto.id}",
                        equipo = dto.equipo,
                        fechaHora = dto.fecha,
                        estado = dto.estado,
                        observaciones = dto.observaciones,
                        correoUsuario = dto.correoUsuario,
                        temperatura = dto.temperatura,
                        humedad = dto.humedad
                    )
                }
                Result.success(lista)
            } else {
                Result.success(dbHelper.obtenerProtocolos(correoUsuario, rol))
            }
        } catch (_: Exception) {
            Result.success(dbHelper.obtenerProtocolos(correoUsuario, rol))
        }
    }

    suspend fun crearProtocoloRemote(protocolo: Protocolo): Result<Boolean> {
        val dto = ProtocoloDto(
            id = protocolo.id,
            equipo = protocolo.equipo,
            fecha = protocolo.fechaHora,
            estado = protocolo.estado,
            observaciones = protocolo.observaciones,
            correoUsuario = protocolo.correoUsuario,
            temperatura = protocolo.temperatura,
            humedad = protocolo.humedad
        )
        return try {
            val resp = RetrofitClient.protocoloApi.crearProtocolo(dto)
            dbHelper.insertarProtocolo(protocolo)
            Result.success(resp.isSuccessful)
        } catch (_: Exception) {
            val id = dbHelper.insertarProtocolo(protocolo)
            if (id > -1) Result.success(true) else Result.failure(Exception("Error al guardar protocolo."))
        }
    }

    suspend fun actualizarProtocoloRemote(protocolo: Protocolo): Result<Boolean> {
        val dto = ProtocoloDto(
            id = protocolo.id,
            equipo = protocolo.equipo,
            fecha = protocolo.fechaHora,
            estado = protocolo.estado,
            observaciones = protocolo.observaciones,
            correoUsuario = protocolo.correoUsuario,
            temperatura = protocolo.temperatura,
            humedad = protocolo.humedad
        )
        return try {
            val resp = RetrofitClient.protocoloApi.actualizarProtocolo(protocolo.id, dto)
            dbHelper.actualizarProtocolo(protocolo)
            Result.success(resp.isSuccessful)
        } catch (_: Exception) {
            dbHelper.actualizarProtocolo(protocolo)
            Result.success(true)
        }
    }

    suspend fun eliminarProtocoloRemote(id: Int): Result<Boolean> {
        return try {
            val resp = RetrofitClient.protocoloApi.eliminarProtocolo(id)
            dbHelper.eliminarProtocolo(id)
            Result.success(resp.isSuccessful)
        } catch (_: Exception) {
            dbHelper.eliminarProtocolo(id)
            Result.success(true)
        }
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        return dbHelper.obtenerProtocoloAnterior(equipo, protocoloActualId)
    }

    fun actualizarCorreoUsuario(correoAntiguo: String, nuevoCorreo: String) {
        dbHelper.actualizarCorreoUsuario(correoAntiguo, nuevoCorreo)
    }
}
