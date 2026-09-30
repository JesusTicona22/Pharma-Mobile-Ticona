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

    // El repositorio en memoria tiene este método que la interfaz te exige:
    override suspend fun registrar(producto: Producto): Producto {
        // Como la Sesión 7 es solo consumo GET, dejamos el post simulado
        return producto
    }
}