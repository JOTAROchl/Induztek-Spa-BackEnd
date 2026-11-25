package cl.duocuc.induztekspa.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.induztekspa.model.Usuario

class UsuarioDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "Induztek.db"

        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_CONTRASENA = "contrasena"
        const val COLUMN_ROL = "rol"
        const val COLUMN_RUT = "rut"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_CARGO = "cargo"
        const val COLUMN_ESTA_APROBADO = "esta_aprobado"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_USUARIOS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_NOMBRE TEXT,"
                + "$COLUMN_CORREO TEXT,"
                + "$COLUMN_CONTRASENA TEXT,"
                + "$COLUMN_ROL TEXT,"
                + "$COLUMN_RUT TEXT,"
                + "$COLUMN_TELEFONO TEXT,"
                + "$COLUMN_CARGO TEXT,"
                + "$COLUMN_ESTA_APROBADO INTEGER)")
        db.execSQL(createTable)

        val adminValues = ContentValues().apply {
            put(COLUMN_NOMBRE, "Administrador Principal")
            put(COLUMN_CORREO, "admin@induztek.cl")
            put(COLUMN_CONTRASENA, "Admin123!")
            put(COLUMN_ROL, "Jefe técnico")
            put(COLUMN_RUT, "13.871.905-7")
            put(COLUMN_TELEFONO, "+56 9 9876 5432")
            put(COLUMN_CARGO, "Jefe Técnico de Terreno")
            put(COLUMN_ESTA_APROBADO, 1)
        }
        db.insert(TABLE_USUARIOS, null, adminValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    fun insertarUsuario(usuario: Usuario): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, usuario.nombre)
            put(COLUMN_CORREO, usuario.correo)
            put(COLUMN_CONTRASENA, usuario.contrasena)
            put(COLUMN_ROL, usuario.rol)
            put(COLUMN_RUT, usuario.rut)
            put(COLUMN_TELEFONO, usuario.telefono)
            put(COLUMN_CARGO, usuario.cargo)
            put(COLUMN_ESTA_APROBADO, if (usuario.estaAprobado) 1 else 0)
        }
        val id = db.insert(TABLE_USUARIOS, null, values)
        db.close()
        return id
    }

    fun autenticarYObtenerUsuario(correo: String, contrasena: String): Usuario? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USUARIOS,
            null,
            "$COLUMN_CORREO = ? AND $COLUMN_CONTRASENA = ?",
            arrayOf(correo, contrasena),
            null, null, null
        )
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = mapearUsuario(cursor)
        }
        cursor.close()
        db.close()
        return usuario
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USUARIOS,
            null,
            "$COLUMN_CORREO = ?",
            arrayOf(correo),
            null, null, null
        )
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = mapearUsuario(cursor)
        }
        cursor.close()
        db.close()
        return usuario
    }

    fun recuperarContrasena(correo: String, nuevaContrasena: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_CONTRASENA, nuevaContrasena) }
        val filas = db.update(TABLE_USUARIOS, values, "$COLUMN_CORREO = ?", arrayOf(correo))
        db.close()
        return filas > 0
    }

    fun obtenerUsuarios(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USUARIOS ORDER BY $COLUMN_ESTA_APROBADO ASC, $COLUMN_ROL ASC", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(mapearUsuario(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun actualizarRol(id: Int, nuevoRol: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_ROL, nuevoRol) }
        val filasAfectadas = db.update(TABLE_USUARIOS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas > 0
    }

    fun aprobarUsuario(id: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_ESTA_APROBADO, 1) }
        val filasAfectadas = db.update(TABLE_USUARIOS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas > 0
    }

    fun actualizarPerfilExtendido(correoAntiguo: String, nuevoCorreo: String, rut: String, telefono: String, cargo: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CORREO, nuevoCorreo)
            put(COLUMN_RUT, rut)
            put(COLUMN_TELEFONO, telefono)
            put(COLUMN_CARGO, cargo)
        }
        val filasAfectadas = db.update(TABLE_USUARIOS, values, "$COLUMN_CORREO = ?", arrayOf(correoAntiguo))
        db.close()
        return filasAfectadas > 0
    }

    fun actualizarCorreo(correoAntiguo: String, nuevoCorreo: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_CORREO, nuevoCorreo) }
        val filasAfectadas = db.update(TABLE_USUARIOS, values, "$COLUMN_CORREO = ?", arrayOf(correoAntiguo))
        db.close()
        return filasAfectadas > 0
    }

    fun eliminarUsuario(id: Int): Boolean {
        val db = this.writableDatabase
        val filasAfectadas = db.delete(TABLE_USUARIOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas > 0
    }

    private fun mapearUsuario(cursor: android.database.Cursor): Usuario {
        val idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID)
        val nomIdx = cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)
        val corIdx = cursor.getColumnIndexOrThrow(COLUMN_CORREO)
        val pasIdx = cursor.getColumnIndexOrThrow(COLUMN_CONTRASENA)
        val rolIdx = cursor.getColumnIndexOrThrow(COLUMN_ROL)
        val rutIdx = cursor.getColumnIndex(COLUMN_RUT)
        val telIdx = cursor.getColumnIndex(COLUMN_TELEFONO)
        val carIdx = cursor.getColumnIndex(COLUMN_CARGO)
        val aprIdx = cursor.getColumnIndex(COLUMN_ESTA_APROBADO)

        return Usuario(
            id = cursor.getInt(idIdx),
            nombre = cursor.getString(nomIdx),
            correo = cursor.getString(corIdx),
            contrasena = cursor.getString(pasIdx),
            rol = cursor.getString(rolIdx),
            rut = if (rutIdx != -1) cursor.getString(rutIdx) ?: "" else "",
            telefono = if (telIdx != -1) cursor.getString(telIdx) ?: "" else "",
            cargo = if (carIdx != -1) cursor.getString(carIdx) ?: "" else "",
            estaAprobado = if (aprIdx != -1) cursor.getInt(aprIdx) == 1 else true
        )
    }
}
