package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
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

class ProductoApi(private val client: HttpClient) {

    suspend fun obtenerProductos(limite: Int = 50): List<ProductoDto> {
        val respuestaPaginada = client.get("productos") {
            parameter("tamanio", limite)
        }.body<PaginaProductoDto>()

        return respuestaPaginada.contenido
    }

    suspend fun registrar(producto: Producto) {
        try {
            val respuesta = client.post("productos") {
                // Desactivamos expectSuccess aquí para que 409 Conflict no lance excepción automática
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
            val sincronizadoConExito =
                respuesta.status.isSuccess() || respuesta.status == HttpStatusCode.Conflict

            if (!sincronizadoConExito) {
                throw IllegalStateException("Error al sincronizar: HTTP ${respuesta.status.value}")
            }
        } catch (e: ClientRequestException) {
            // Por si algún validador global intercepta el 409 antes:
            if (e.response.status == HttpStatusCode.Conflict) {
                return // Ya existe en el servidor, lo damos por sincronizado
            }
            throw e
        }
    }
}