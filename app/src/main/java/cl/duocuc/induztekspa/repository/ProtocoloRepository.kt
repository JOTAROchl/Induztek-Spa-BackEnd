package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.ProtocoloDbHelper
import cl.duocuc.induztekspa.data.remote.RetrofitClient
import cl.duocuc.induztekspa.model.Protocolo

class ProtocoloRepository(context: Context) {
    private val dbHelper = ProtocoloDbHelper(context)

    suspend fun insertarProtocoloRemote(protocolo: Protocolo): Boolean {
        return try {
            val resp = RetrofitClient.protocoloApi.crearProtocolo(protocolo)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    fun insertarProtocolo(protocolo: Protocolo): Long {
        return dbHelper.insertarProtocolo(protocolo)
    }

    fun actualizarProtocolo(protocolo: Protocolo): Boolean {
        return dbHelper.actualizarProtocolo(protocolo)
    }

    fun actualizarCorreoUsuario(correoAntiguo: String, nuevoCorreo: String) {
        dbHelper.actualizarCorreoUsuario(correoAntiguo, nuevoCorreo)
    }

    fun eliminarProtocolo(id: Int): Boolean {
        return dbHelper.eliminarProtocolo(id)
    }

    suspend fun obtenerProtocolosRemote(correoUsuario: String, rol: String): List<Protocolo> {
        return try {
            RetrofitClient.protocoloApi.obtenerProtocolos(correoUsuario, rol)
        } catch (_: Exception) {
            dbHelper.obtenerProtocolos(correoUsuario, rol)
        }
    }

    fun obtenerProtocolos(correoUsuario: String, rol: String): List<Protocolo> {
        return dbHelper.obtenerProtocolos(correoUsuario, rol)
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        return dbHelper.obtenerProtocoloAnterior(equipo, protocoloActualId)
    }
}
