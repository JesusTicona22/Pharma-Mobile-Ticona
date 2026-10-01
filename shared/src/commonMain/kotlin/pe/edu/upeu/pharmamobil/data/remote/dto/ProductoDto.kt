package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean, // El backend lo mapea como Boolean
    @SerialName("categoria") val categoria: CategoriaDto? = null
)

@Serializable
data class CategoriaDto(
    val id: Long,
    val nombre: String
)

@Serializable
data class PaginaProductoDto(
    val contenido: List<ProductoDto>
    // Ignoramos pagina, tamanio, etc., porque Ktor lo permite automáticamente
)