package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaProductoDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto

// 1. Creamos el DTO exacto que exige el profesor para enviar[cite: 7]
@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long
)

class ProductoApi(private val client: HttpClient) {

    // Función para LEER la lista (la que ya tenías)
    suspend fun obtenerProductos(limite: Int = 10): List<ProductoDto> {
        val respuestaPaginada = client.get("api/v1/productos") {
            parameter("tamanio", limite)
        }.body<PaginaProductoDto>()

        return respuestaPaginada.contenido
    }

    // 2. NUEVA Función para GUARDAR el producto[cite: 7]
    suspend fun crear(nombre: String, precio: Double, stock: Int): ProductoDto {
        // Armamos el paquete de envío fijando la categoría 1[cite: 7]
        val request = ProductoRequestDto(
            nombre = nombre,
            precio = precio,
            stock = stock,
            estado = true,
            categoriaId = 1L
        )

        // Lo enviamos por POST a tu Spring Boot[cite: 7]
        return client.post("api/v1/productos") {
            setBody(request)
        }.body()
    }
}