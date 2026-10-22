package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar() {
        subirPendientes()
        // Llamamos a obtenerProductos() y usamos el mapper para convertir los DTOs al Dominio
        local.reemplazarSincronizados(remoto.obtenerProductos().map { it.toDomain() })
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            remoto.registrar(pendiente)
            local.marcarComoSubido(pendiente)
        }
    }

    /** Un fallo de red no debe tumbar la operación local. */
    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (e: Exception) {
            // Atrapamos cualquier error de red (Timeout, Sin conexión, etc.)
            // El producto queda guardado localmente como pendiente; se reintentará al sincronizar.
        }
    }
}