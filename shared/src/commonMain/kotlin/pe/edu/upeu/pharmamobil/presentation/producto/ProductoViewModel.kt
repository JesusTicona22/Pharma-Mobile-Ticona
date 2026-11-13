package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobil.platform.tiempoActualMillis

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val sincronizarProductos: SincronizarProductosUseCase,
    private val repository: ProductoRepository // Inyectado para observar pendientes/fallidas
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    private val _enCurso = MutableStateFlow(false)
    private val _ultimaSync = MutableStateFlow<Long?>(null)

    // Combinamos las consultas observables de la base local (Paso 4 - Sesión 13)
    val syncState: StateFlow<SincronizacionUiState> = combine(
        repository.observarPendientes(),
        repository.observarFallidas(),
        _enCurso,
        _ultimaSync
    ) { pend, fall, curso, ultima ->
        SincronizacionUiState(
            enCurso = curso,
            pendientes = pend,
            fallidas = fall,
            ultimaSync = ultima
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SincronizacionUiState())

    init {
        observarInventarioLocal()
        sincronizar()
    }

    private fun observarInventarioLocal() {
        viewModelScope.launch {
            try {
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        fase = ProductoUiState.Fase.Error(
                            e.message ?: "No se pudo cargar el inventario"
                        )
                    )
                }
            }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _enCurso.value = true
            _uiState.update { it.copy(sincronizando = true) }

            val resultado = sincronizarProductos()
            if (resultado.isSuccess) {
                _ultimaSync.value = tiempoActualMillis()
            }

            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let {
                        "Sin conexión: mostrando datos guardados"
                    }
                )
            }
            _enCurso.value = false
        }
    }

    fun reintentarFallidas() {
        viewModelScope.launch {
            repository.reintentarFallidas()
            sincronizar()
        }
    }

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