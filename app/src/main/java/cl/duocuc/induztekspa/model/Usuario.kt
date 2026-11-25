package cl.duocuc.induztekspa.model

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String = "Técnico",
    val rut: String = "",
    val telefono: String = "",
    val cargo: String = "Técnico de Terreno",
    val estaAprobado: Boolean = false
)
