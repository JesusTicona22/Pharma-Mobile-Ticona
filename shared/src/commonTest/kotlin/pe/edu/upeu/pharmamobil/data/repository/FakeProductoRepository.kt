package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class FakeProductoRepository : ProductoRepository {
    var deberiaFallar = false
    private val productos = mutableListOf<Producto>()
    private var nextId = 1L

    override fun observar(): Flow<List<Producto>> = flowOf(productos)

    override fun observarPendientes(): Flow<Int> = flowOf(0)

    override fun observarFallidas(): Flow<Int> = flowOf(0)

    override suspend fun registrar(producto: Producto): Producto {
        if (deberiaFallar) throw Exception("Error forzado al registrar")
        val guardado = producto.copy(id = nextId++)
        productos.add(guardado)
        return guardado
    }

    override suspend fun sincronizar(): ResumenSync {
        if (deberiaFallar) throw Exception("Error forzado al sincronizar")
        return ResumenSync(enviadas = 0, fallidas = 0, pendientes = 0)
    }
}