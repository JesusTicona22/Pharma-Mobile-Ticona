package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    @SerialName("categoria") val categoria: CategoriaDto? = null
)

@Serializable
data class CrearProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long = 1L
)

@Serializable
data class CategoriaDto(
    val id: Long,
    val nombre: String
)

@Serializable
data class PaginaProductoDto(
    val contenido: List<ProductoDto>
)