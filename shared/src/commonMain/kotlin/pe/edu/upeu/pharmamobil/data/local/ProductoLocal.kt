package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

data class FilaPendienteProducto(
    val producto: Producto,
    val intentos: Int,
    val ultimoError: String?
)

class ProductoLocal(
    private val base: PharmaDb
) {
    private val consultas get() = base.productoQueries

    fun observar(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): Flow<List<Producto>> =
        consultas.todos { id, nombre, precio, stock, pendiente, intentos, ultimoError ->
            val esPendiente = pendiente == 1L
            Producto(
                id = id,
                nombre = nombre,
                precio = precio,
                stock = stock.toInt(),
                pendiente = esPendiente,
                fallida = esPendiente && intentos >= maxIntentos,
                ultimoError = ultimoError
            )
        }.asFlow().mapToList(Dispatchers.Default)

    fun observarPendientes(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): Flow<Int> =
        consultas.contarPendientes(maxIntentos.toLong())
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toInt() }

    fun observarFallidas(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): Flow<Int> =
        consultas.contarFallidas(maxIntentos.toLong())
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toInt() }

    fun contarPendientes(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): Int =
        consultas.contarPendientes(maxIntentos.toLong()).executeAsOne().toInt()

    fun contarFallidas(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): Int =
        consultas.contarFallidas(maxIntentos.toLong()).executeAsOne().toInt()

    fun porId(id: Long, maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): FilaPendienteProducto? =
        consultas.porId(id) { prodId, nombre, precio, stock, pendiente, intentos, ultimoError ->
            val esPendiente = pendiente == 1L
            FilaPendienteProducto(
                producto = Producto(
                    id = prodId,
                    nombre = nombre,
                    precio = precio,
                    stock = stock.toInt(),
                    pendiente = esPendiente,
                    fallida = esPendiente && intentos >= maxIntentos,
                    ultimoError = ultimoError
                ),
                intentos = intentos.toInt(),
                ultimoError = ultimoError
            )
        }.executeAsOneOrNull()

    fun pendientes(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): List<Producto> =
        consultas.pendientesPorEnviar(maxIntentos.toLong()) { id, nombre, precio, stock, _, _, _ ->
            Producto(id = id, nombre = nombre, precio = precio, stock = stock.toInt())
        }.executeAsList()

    fun pendientesConEstado(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO): List<FilaPendienteProducto> =
        consultas.pendientesPorEnviar(maxIntentos.toLong()) { id, nombre, precio, stock, _, intentos, ultimoError ->
            FilaPendienteProducto(
                producto = Producto(id = id, nombre = nombre, precio = precio, stock = stock.toInt()),
                intentos = intentos.toInt(),
                ultimoError = ultimoError
            )
        }.executeAsList()

    fun guardarPendiente(producto: Producto): Producto =
        base.transactionWithResult {
            val minimoActual = consultas.menorId().executeAsOne()
            val idTemporal = if (minimoActual <= 0L) minimoActual - 1L else -1L
            consultas.insertar(
                idTemporal,
                producto.nombre,
                producto.precio,
                producto.stock.toLong(),
                1L,
                0L,
                null
            )
            producto.copy(id = idTemporal)
        }

    fun confirmar(idLocal: Long, idServidor: Long) {
        base.transaction {
            val fila = consultas.porId(idLocal).executeAsOneOrNull() ?: return@transaction
            consultas.borrarPorId(idLocal)
            consultas.insertar(
                idServidor,
                fila.nombre,
                fila.precio,
                fila.stock,
                0L,
                0L,
                null
            )
        }
    }

    fun marcarComoSubido(producto: Producto) {
        consultas.insertar(
            producto.id,
            producto.nombre,
            producto.precio,
            producto.stock.toLong(),
            0L,
            0L,
            null
        )
    }

    fun sumarIntento(id: Long, motivo: String) {
        consultas.sumarIntento(motivo, id)
    }

    fun marcarFallida(id: Long, motivo: String, maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO) {
        consultas.marcarFallida(maxIntentos.toLong(), motivo, id)
    }

    fun reintentarFallidas(maxIntentos: Int = MAX_INTENTOS_POR_DEFECTO) {
        consultas.reintentarFallidas(maxIntentos.toLong())
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        base.transaction {
            consultas.borrarSincronizados()
            productos.forEach { p ->
                consultas.insertar(p.id, p.nombre, p.precio, p.stock.toLong(), 0L, 0L, null)
            }
        }
    }

    companion object {
        const val MAX_INTENTOS_POR_DEFECTO = 5
    }
}