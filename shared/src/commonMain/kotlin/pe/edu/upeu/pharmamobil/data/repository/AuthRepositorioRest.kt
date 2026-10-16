package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class SesionException(val error: ErrorSesion, message: String? = null) : Exception(message)

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    private fun limpiarCacheBearer() {
        cliente.plugin(Auth).providers
            .filterIsInstance<BearerAuthProvider>()
            .firstOrNull()
            ?.clearToken()
    }

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        try {
            val dto: LoginResponseDto = cliente.post("auth/login") {
                setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
            }.body()

            almacen.guardarToken(dto.token)
            limpiarCacheBearer()
            return dto.aSesion()
        } catch (e: ClientRequestException) {
            val textoError = runCatching { e.response.bodyAsText() }.getOrNull()
            throw SesionException(mapearErrorSesion(e.response.status, textoError), textoError)
        } catch (e: Exception) {
            throw SesionException(ErrorSesion.SinConexion, e.message)
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return try {
            val dto: LoginResponseDto = cliente.get("auth/me").body()
            dto.aSesion(tokenActual = token)
        } catch (e: Exception) {
            almacen.borrarToken()
            limpiarCacheBearer()
            null
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        limpiarCacheBearer()
    }
}

fun mapearErrorSesion(estado: HttpStatusCode, mensaje: String?): ErrorSesion =
    when (estado) {
        HttpStatusCode.Unauthorized ->
            if (mensaje?.contains("expirado", ignoreCase = true) == true)
                ErrorSesion.SesionExpirada
            else
                ErrorSesion.CredencialesInvalidas
        HttpStatusCode.Forbidden -> ErrorSesion.SinPermiso
        else -> ErrorSesion.SinConexion
    }