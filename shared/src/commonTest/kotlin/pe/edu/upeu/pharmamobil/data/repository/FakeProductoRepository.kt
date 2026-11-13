package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class FakeProductoRepository(
    productosIniciales: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlSincronizar: Throwable? = null
    var deberiaFallar: Boolean = false

    private val productos = productosIniciales.toMutableList()
    private val productosFlow = MutableStateFlow(productos.toList())
    private var nextId = (productos.maxOfOrNull { it.id } ?: 0L).coerceAtLeast(0L) + 1L

    override fun observar(): Flow<List<Producto>> = flow {
        fallaAlListar?.let { throw it }
        emitAll(productosFlow)
    }

    override fun observarPendientes(): Flow<Int> =
        productosFlow.map { lista -> lista.count { it.pendiente && !it.fallida } }

    override fun observarFallidas(): Flow<Int> =
        productosFlow.map { lista -> lista.count { it.fallida } }

    override suspend fun registrar(producto: Producto): Producto {
        fallaAlRegistrar?.let { throw it }
        if (deberiaFallar) throw Exception("Error forzado al registrar")

        val idAsignado = if (producto.id > 0L) producto.id else nextId++
        val guardado = producto.copy(id = idAsignado, pendiente = false)
        productos.add(guardado)
        productosFlow.value = productos.toList()
        return guardado
    }

    override suspend fun sincronizar(): ResumenSync {
        fallaAlSincronizar?.let { throw it }
        if (deberiaFallar) throw Exception("Error forzado al sincronizar")

        return ResumenSync(
            enviadas = 0,
            fallidas = productos.count { it.fallida },
            pendientes = productos.count { it.pendiente && !it.fallida }
        )
    }

    override suspend fun reintentarFallidas() {
        val actualizados = productos.map {
            if (it.fallida) it.copy(fallida = false, ultimoError = null) else it
        }
        productos.clear()
        productos.addAll(actualizados)
        productosFlow.value = productos.toList()
    }
}
