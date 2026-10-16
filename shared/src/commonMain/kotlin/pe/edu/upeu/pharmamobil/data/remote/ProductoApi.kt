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

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long
)

class ProductoApi(private val client: HttpClient) {

    // Como defaultRequest ya tiene "http://10.0.2.2:8080/api/v1/", aquí solo ponemos "productos"
    suspend fun obtenerProductos(limite: Int = 10): List<ProductoDto> {
        val respuestaPaginada = client.get("productos") {
            parameter("tamanio", limite)
        }.body<PaginaProductoDto>()

        return respuestaPaginada.contenido
    }

    suspend fun crear(nombre: String, precio: Double, stock: Int): ProductoDto {
        val request = ProductoRequestDto(
            nombre = nombre,
            precio = precio,
            stock = stock,
            estado = true,
            categoriaId = 1L
        )

        return client.post("productos") {
            setBody(request)
        }.body()
    }
}