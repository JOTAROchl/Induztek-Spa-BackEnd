package cl.duocuc.induztekspa.model

data class Protocolo(
    val id: Int = 0,
    val codigo: String,
    val equipo: String,
    val tipoEquipo: String = "Transformador",
    val instalacion: String = "Planta Lampa",
    val direccion: String = "",
    val fechaHora: String,
    val estado: String = "Borrador",
    val observaciones: String = "",
    val correoUsuario: String,
    val temperatura: String = "",
    val humedad: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val medicionValor: Double = 0.0
)
