package pe.edu.upeu.pharmamobil.permiso

import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class GestorPermisosIos : GestorPermisos {

    private val solicitados = mutableSetOf<Permiso>()

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        return if (permiso in solicitados) {
            EstadoPermiso.Denegado
        } else {
            EstadoPermiso.Denegado
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        solicitados.add(permiso)
        return EstadoPermiso.Concedido
    }

    override fun abrirAjustes() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
