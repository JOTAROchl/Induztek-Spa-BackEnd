package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.ProtocoloDbHelper
import cl.duocuc.induztekspa.model.Protocolo

class ProtocoloRepository(context: Context) {
    private val dbHelper = ProtocoloDbHelper(context)

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

    fun obtenerProtocolos(correoUsuario: String, rol: String): List<Protocolo> {
        return dbHelper.obtenerProtocolos(correoUsuario, rol)
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        return dbHelper.obtenerProtocoloAnterior(equipo, protocoloActualId)
    }
}
