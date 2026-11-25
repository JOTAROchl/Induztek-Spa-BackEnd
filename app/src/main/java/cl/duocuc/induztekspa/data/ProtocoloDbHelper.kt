package cl.duocuc.induztekspa.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.induztekspa.model.Protocolo

class ProtocoloDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "ProtocolosInduztek.db"

        const val TABLE_PROTOCOLOS = "protocolos"
        const val COLUMN_ID = "id"
        const val COLUMN_CODIGO = "codigo"
        const val COLUMN_EQUIPO = "equipo"
        const val COLUMN_TIPO_EQUIPO = "tipo_equipo"
        const val COLUMN_INSTALACION = "instalacion"
        const val COLUMN_DIRECCION = "direccion"
        const val COLUMN_FECHA_HORA = "fecha_hora"
        const val COLUMN_ESTADO = "estado"
        const val COLUMN_OBSERVACIONES = "observaciones"
        const val COLUMN_CORREO_USUARIO = "correo_usuario"
        const val COLUMN_TEMPERATURA = "temperatura"
        const val COLUMN_HUMEDAD = "humedad"
        const val COLUMN_LATITUD = "latitud"
        const val COLUMN_LONGITUD = "longitud"
        const val COLUMN_MEDICION_VALOR = "medicion_valor"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_PROTOCOLOS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_CODIGO TEXT,"
                + "$COLUMN_EQUIPO TEXT,"
                + "$COLUMN_TIPO_EQUIPO TEXT,"
                + "$COLUMN_INSTALACION TEXT,"
                + "$COLUMN_DIRECCION TEXT,"
                + "$COLUMN_FECHA_HORA TEXT,"
                + "$COLUMN_ESTADO TEXT,"
                + "$COLUMN_OBSERVACIONES TEXT,"
                + "$COLUMN_CORREO_USUARIO TEXT,"
                + "$COLUMN_TEMPERATURA TEXT,"
                + "$COLUMN_HUMEDAD TEXT,"
                + "$COLUMN_LATITUD TEXT,"
                + "$COLUMN_LONGITUD TEXT,"
                + "$COLUMN_MEDICION_VALOR REAL)")
        db.execSQL(createTable)

        val p1 = ContentValues().apply {
            put(COLUMN_CODIGO, "PT-2025-0288")
            put(COLUMN_EQUIPO, "TR-01")
            put(COLUMN_TIPO_EQUIPO, "Transformador")
            put(COLUMN_INSTALACION, "Planta Lampa")
            put(COLUMN_DIRECCION, "Av. La Montaña 1234")
            put(COLUMN_FECHA_HORA, "13/09/2025 10:30")
            put(COLUMN_ESTADO, "Formalizado")
            put(COLUMN_OBSERVACIONES, "Prueba histórica anterior")
            put(COLUMN_CORREO_USUARIO, "admin@induztek.cl")
            put(COLUMN_TEMPERATURA, "21 °C")
            put(COLUMN_HUMEDAD, "48 %")
            put(COLUMN_LATITUD, "-33.2842")
            put(COLUMN_LONGITUD, "-70.7301")
            put(COLUMN_MEDICION_VALOR, 9800.0)
        }
        db.insert(TABLE_PROTOCOLOS, null, p1)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROTOCOLOS")
        onCreate(db)
    }

    fun insertarProtocolo(protocolo: Protocolo): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODIGO, protocolo.codigo)
            put(COLUMN_EQUIPO, protocolo.equipo)
            put(COLUMN_TIPO_EQUIPO, protocolo.tipoEquipo)
            put(COLUMN_INSTALACION, protocolo.instalacion)
            put(COLUMN_DIRECCION, protocolo.direccion)
            put(COLUMN_FECHA_HORA, protocolo.fechaHora)
            put(COLUMN_ESTADO, protocolo.estado)
            put(COLUMN_OBSERVACIONES, protocolo.observaciones)
            put(COLUMN_CORREO_USUARIO, protocolo.correoUsuario)
            put(COLUMN_TEMPERATURA, protocolo.temperatura)
            put(COLUMN_HUMEDAD, protocolo.humedad)
            put(COLUMN_LATITUD, protocolo.latitud)
            put(COLUMN_LONGITUD, protocolo.longitud)
            put(COLUMN_MEDICION_VALOR, protocolo.medicionValor)
        }
        val id = db.insert(TABLE_PROTOCOLOS, null, values)
        db.close()
        return id
    }

    fun actualizarProtocolo(protocolo: Protocolo): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODIGO, protocolo.codigo)
            put(COLUMN_EQUIPO, protocolo.equipo)
            put(COLUMN_TIPO_EQUIPO, protocolo.tipoEquipo)
            put(COLUMN_INSTALACION, protocolo.instalacion)
            put(COLUMN_DIRECCION, protocolo.direccion)
            put(COLUMN_FECHA_HORA, protocolo.fechaHora)
            put(COLUMN_ESTADO, protocolo.estado)
            put(COLUMN_OBSERVACIONES, protocolo.observaciones)
            put(COLUMN_TEMPERATURA, protocolo.temperatura)
            put(COLUMN_HUMEDAD, protocolo.humedad)
            put(COLUMN_LATITUD, protocolo.latitud)
            put(COLUMN_LONGITUD, protocolo.longitud)
            put(COLUMN_MEDICION_VALOR, protocolo.medicionValor)
        }
        val filas = db.update(TABLE_PROTOCOLOS, values, "$COLUMN_ID = ?", arrayOf(protocolo.id.toString()))
        db.close()
        return filas > 0
    }

    fun actualizarCorreoUsuario(correoAntiguo: String, nuevoCorreo: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_CORREO_USUARIO, nuevoCorreo) }
        db.update(TABLE_PROTOCOLOS, values, "$COLUMN_CORREO_USUARIO = ?", arrayOf(correoAntiguo))
        db.close()
    }

    fun eliminarProtocolo(id: Int): Boolean {
        val db = this.writableDatabase
        val filas = db.delete(TABLE_PROTOCOLOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return filas > 0
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PROTOCOLOS WHERE $COLUMN_EQUIPO = ? AND $COLUMN_ID != ? ORDER BY $COLUMN_ID DESC LIMIT 1",
            arrayOf(equipo, protocoloActualId.toString())
        )
        var anterior: Protocolo? = null
        if (cursor.moveToFirst()) {
            anterior = mapearProtocolo(cursor)
        }
        cursor.close()
        db.close()
        return anterior
    }

    fun obtenerProtocolos(correoUsuario: String, rol: String): List<Protocolo> {
        val listaProtocolos = mutableListOf<Protocolo>()
        val db = this.readableDatabase
        
        val cursor = if (rol == "Jefe técnico" || rol == "Administrador" || rol == "Ingeniero") {
            db.rawQuery("SELECT * FROM $TABLE_PROTOCOLOS ORDER BY $COLUMN_ID DESC", null)
        } else {
            db.rawQuery("SELECT * FROM $TABLE_PROTOCOLOS WHERE $COLUMN_CORREO_USUARIO = ? ORDER BY $COLUMN_ID DESC", arrayOf(correoUsuario))
        }

        if (cursor.moveToFirst()) {
            do {
                listaProtocolos.add(mapearProtocolo(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaProtocolos
    }

    private fun mapearProtocolo(cursor: android.database.Cursor): Protocolo {
        fun getStringSafely(colName: String): String {
            val idx = cursor.getColumnIndex(colName)
            return if (idx != -1) cursor.getString(idx) ?: "" else ""
        }

        val valIdx = cursor.getColumnIndex(COLUMN_MEDICION_VALOR)
        val medicionVal = if (valIdx != -1) cursor.getDouble(valIdx) else 0.0

        return Protocolo(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            codigo = getStringSafely(COLUMN_CODIGO).ifBlank { "PT-2026-000" },
            equipo = getStringSafely(COLUMN_EQUIPO),
            tipoEquipo = getStringSafely(COLUMN_TIPO_EQUIPO).ifBlank { "Transformador" },
            instalacion = getStringSafely(COLUMN_INSTALACION).ifBlank { "Planta Lampa" },
            direccion = getStringSafely(COLUMN_DIRECCION),
            fechaHora = getStringSafely(COLUMN_FECHA_HORA),
            estado = getStringSafely(COLUMN_ESTADO).ifBlank { "Borrador" },
            observaciones = getStringSafely(COLUMN_OBSERVACIONES),
            correoUsuario = getStringSafely(COLUMN_CORREO_USUARIO),
            temperatura = getStringSafely(COLUMN_TEMPERATURA),
            humedad = getStringSafely(COLUMN_HUMEDAD),
            latitud = getStringSafely(COLUMN_LATITUD),
            longitud = getStringSafely(COLUMN_LONGITUD),
            medicionValor = medicionVal
        )
    }
}
