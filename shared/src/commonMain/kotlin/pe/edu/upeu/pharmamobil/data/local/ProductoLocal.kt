package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList()
            .map { it.aDominio() }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        // Garantiza que los IDs creados sin red sean negativos (ej. -1, -2) para que no choquen con el servidor
        val local = producto.copy(id = minOf(menor, 0L) - 1L)

        consultas.insertar(
            local.id, local.nombre, local.precio,
            local.stock.toLong(), 1L // 1L = Pendiente por subir
        )
        return local
    }

    fun marcarComoSubido(producto: Producto) {
        consultas.insertar(
            producto.id,
            producto.nombre,
            producto.precio,
            producto.stock.toLong(),
            0L // Pasa a pendiente = 0 para ser reemplazado por el ID real del servidor
        )
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach {
                consultas.insertar(
                    it.id, it.nombre, it.precio,
                    it.stock.toLong(), 0L // 0L = Ya sincronizado con el servidor
                )
            }
        }
    }
}

// Función traductora de la fila generada por SQLDelight a tu modelo de dominio
private fun pe.edu.upeu.pharmamobil.db.Producto.aDominio(): Producto {
    return Producto(
        id = this.id,
        nombre = this.nombre,
        precio = this.precio,
        stock = this.stock.toInt()
    )
}