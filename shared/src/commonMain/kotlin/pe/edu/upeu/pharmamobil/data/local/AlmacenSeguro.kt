package pe.edu.upeu.pharmamobil.data.local

interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()

    // NUEVO: Persistir la preferencia del tema oscuro
    fun guardarModoOscuro(activo: Boolean) {}
    fun leerModoOscuro(): Boolean = false
}