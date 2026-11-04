package pe.edu.upeu.pharmamobil.domain.permiso

enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso // Se puede volver a pedir
    data object DenegadoParaSiempre : EstadoPermiso // Solo queda ir a Ajustes
}

interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}