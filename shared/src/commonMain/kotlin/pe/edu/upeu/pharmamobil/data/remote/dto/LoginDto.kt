package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.domain.model.Sesion

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val token: String = "",
    val tipo: String = "Bearer",
    val expiraEnSegundos: Long = 3600,
    val username: String,
    val nombreCompleto: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
) {
    fun aSesion(tokenActual: String = token): Sesion = Sesion(
        token = tokenActual,
        usuario = username,
        nombreCompleto = nombreCompleto.ifBlank { username },
        permisos = permisos.toSet()
    )
}