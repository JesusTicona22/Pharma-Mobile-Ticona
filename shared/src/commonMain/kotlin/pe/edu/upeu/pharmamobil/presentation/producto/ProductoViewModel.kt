package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val sincronizarProductos: SincronizarProductosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        observarInventarioLocal()
        sincronizar()
    }

    private fun observarInventarioLocal() {
        viewModelScope.launch {
            listarProductos().collect { productos ->
                val fase = if (productos.isEmpty()) {
                    ProductoUiState.Fase.SinProductos
                } else {
                    ProductoUiState.Fase.ConProductos(productos.map { it.aUi() })
                }
                _uiState.update {
                    it.copy(fase = fase)
                }
            }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            val resultado = sincronizarProductos()
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let {
                        "Sin conexión: mostrando datos guardados"
                    }
                )
            }
        }
    }

    // Mantenemos este alias por si ProductoScreen o algún test llama a cargarProductos()
    fun cargarProductos() = sincronizar()

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nombre, nombreError = null),
                mensajeExito = null
            )
        }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(precio = precio, precioError = null),
                mensajeExito = null
            )
        }
    }

    fun onStockChange(stock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(stock = stock, stockError = null),
                mensajeExito = null
            )
        }
    }

    fun registrar() {
        if (_uiState.value.registrando) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(registrando = true, mensajeExito = null)
            }

            val formulario = _uiState.value.formulario

            registrarProducto(
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock
            ).fold(
                onSuccess = { producto ->
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            formulario = FormularioProducto(),
                            mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
                        )
                    }
                    // Intentamos actualizar el estado del aviso de red tras registrar
                    sincronizar()
                },
                onFailure = { fallo ->
                    when (fallo) {
                        is ProductoInvalidoException -> _uiState.update {
                            it.copy(
                                registrando = false,
                                formulario = it.formulario.copy(
                                    nombreError = fallo.errores.nombre,
                                    precioError = fallo.errores.precio,
                                    stockError = fallo.errores.stock
                                )
                            )
                        }

                        else -> _uiState.update {
                            it.copy(
                                registrando = false,
                                fase = ProductoUiState.Fase.Error(
                                    fallo.message ?: "No se pudo registrar el producto"
                                )
                            )
                        }
                    }
                }
            )
        }
    }

    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
}