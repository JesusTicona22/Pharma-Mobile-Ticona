package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync

interface ProductoRepository {
    fun observar(): Flow<List<Producto>>
    fun observarPendientes(): Flow<Int>
    fun observarFallidas(): Flow<Int>
    suspend fun registrar(producto: Producto): Producto
    suspend fun sincronizar(): ResumenSync
}