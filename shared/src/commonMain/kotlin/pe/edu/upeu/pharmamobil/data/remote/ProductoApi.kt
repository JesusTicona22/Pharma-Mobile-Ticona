package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import pe.edu.upeu.pharmamobil.data.remote.dto.CrearProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaProductoDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

class ProductoApi(private val client: HttpClient) {

    suspend fun obtenerProductos(limite: Int = 50): List<ProductoDto> {
        val respuestaPaginada = client.get("productos") {
            parameter("tamanio", limite)
        }.body<PaginaProductoDto>()

        return respuestaPaginada.contenido
    }

    /**
     * Paso 3 (Sesión 13): Traduce la respuesta o error de red a ResultadoEnvio
     * distinguiendo fallo Transitorio (reintentable) de Permanente (no reintentable).
     */
    suspend fun enviar(producto: Producto): ResultadoEnvio {
        return try {
            val respuesta = client.post("productos") {
                expectSuccess = false
                contentType(ContentType.Application.Json)
                setBody(
                    CrearProductoRequestDto(
                        nombre = producto.nombre,
                        precio = producto.precio,
                        stock = producto.stock,
                        estado = true,
                        categoriaId = 1L
                    )
                )
            }

            when {
                respuesta.status.isSuccess() -> {
                    val idServidor = runCatching { respuesta.body<ProductoDto>().id }
                        .getOrDefault(0L)
                    ResultadoEnvio.Exito(idServidor)
                }
                respuesta.status == HttpStatusCode.Unauthorized ->
                    ResultadoEnvio.Permanente("Sesión vencida")
                respuesta.status == HttpStatusCode.Forbidden ->
                    ResultadoEnvio.Permanente("Sin privilegios para registrar")
                respuesta.status == HttpStatusCode.Conflict ->
                    ResultadoEnvio.Permanente("Conflicto: ya existe un producto con ese nombre")
                respuesta.status == HttpStatusCode.BadRequest ||
                        respuesta.status == HttpStatusCode.UnprocessableEntity ->
                    ResultadoEnvio.Permanente("Datos inválidos rechazados por el servidor")
                respuesta.status == HttpStatusCode.NotFound ->
                    ResultadoEnvio.Permanente("Recurso o categoría no encontrada")
                respuesta.status.value in 500..599 ||
                        respuesta.status == HttpStatusCode.RequestTimeout ->
                    ResultadoEnvio.Transitorio("Servidor no disponible (HTTP ${respuesta.status.value})")
                else ->
                    ResultadoEnvio.Transitorio("Error temporal (HTTP ${respuesta.status.value})")
            }
        } catch (e: Exception) {
            ResultadoEnvio.Transitorio(e.message ?: "Sin conexión con el servidor")
        }
    }

    // Mantenemos registrar() delegando a enviar() por compatibilidad
    suspend fun registrar(producto: Producto): ResultadoEnvio = enviar(producto)
}