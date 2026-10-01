package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val api: ProductoApi
) : ProductoRepository {

    override suspend fun listar(): List<Producto> =
        api.obtenerProductos().map { it.toDomain() }

    override suspend fun registrar(producto: Producto): Producto {
        // Enlaza tu modelo de dominio con la llamada HTTP hacia Spring Boot
        val productoGuardadoDto = api.crear(
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock
        )
        // Convierte el DTO que devuelve Ktor nuevamente a tu modelo de dominio
        return productoGuardadoDto.toDomain()
    }
}