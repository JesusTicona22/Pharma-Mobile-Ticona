package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.repository.SesionException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerSesionGuardadaUseCase

class SesionViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val obtenerSesionGuardadaUseCase: ObtenerSesionGuardadaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        verificarSesionGuardada()
    }

    fun verificarSesionGuardada() {
        viewModelScope.launch {
            _uiState.update { it.copy(verificandoSesionInicial = true) }
            val guardada = obtenerSesionGuardadaUseCase()
            _uiState.update {
                it.copy(
                    sesion = guardada,
                    verificandoSesionInicial = false
                )
            }
        }
    }

    fun onUsuarioChange(nuevoUsuario: String) {
        _uiState.update { it.copy(usuario = nuevoUsuario, error = null) }
    }

    fun onClaveChange(nuevaClave: String) {
        _uiState.update { it.copy(clave = nuevaClave, error = null) }
    }

    fun iniciarSesion() {
        val usuarioActual = _uiState.value.usuario.trim()
        val claveActual = _uiState.value.clave

        if (usuarioActual.isBlank() || claveActual.isBlank()) {
            _uiState.update { it.copy(error = "Completa usuario y contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val nuevaSesion = iniciarSesionUseCase(usuarioActual, claveActual)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        clave = "",
                        error = null,
                        sesion = nuevaSesion
                    )
                }
            } catch (e: SesionException) {
                val mensaje = when (e.error) {
                    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos"
                    ErrorSesion.SesionExpirada -> "La sesión ha expirado. Inicia sesión nuevamente"
                    ErrorSesion.SinPermiso -> "No tiene permisos para realizar esta operación"
                    ErrorSesion.SinConexion -> "Error de conexión con el servidor PharmaSoft"
                }
                _uiState.update { it.copy(cargando = false, error = mensaje) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(cargando = false, error = e.message ?: "Datos inválidos") }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "Error inesperado al iniciar sesión") }
            }
        }
    }

    fun manejarErrorSesion(error: ErrorSesion) {
        viewModelScope.launch {
            when (error) {
                ErrorSesion.SesionExpirada, ErrorSesion.CredencialesInvalidas -> {
                    cerrarSesionUseCase()
                    _uiState.update {
                        SesionUiState(
                            verificandoSesionInicial = false,
                            error = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
                        )
                    }
                }
                ErrorSesion.SinPermiso -> {
                    // El 403 mantiene la sesión y muestra un mensaje distinto
                    _uiState.update {
                        it.copy(error = "No tienes permisos (403) para realizar esta operación.")
                    }
                }
                ErrorSesion.SinConexion -> {
                    _uiState.update {
                        it.copy(error = "Sin conexión con el servidor.")
                    }
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
            _uiState.update {
                SesionUiState(verificandoSesionInicial = false)
            }
        }
    }
}