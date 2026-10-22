package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val productoRepository: ProductoRepository
) {
    // Ya no usamos suspend ni Result; devolvemos el Flow directo de la base de datos
    operator fun invoke(): Flow<List<Producto>> {
        return productoRepository.observar()
    }
}