package pe.edu.upeu.pharmamobil.presentation.detalle

import androidx.lifecycle.ViewModel
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoUi

class DetalleProductoViewModel(
    private val compartidor: Compartidor
) : ViewModel() {

    fun compartir(producto: ProductoUi) {
        // Aprovechamos que ProductoUi ya tiene el precio con "S/" y el stock con "u."
        val textoParaCompartir = "¡Mira este producto! ${producto.nombre} a solo ${producto.precio}. Tenemos ${producto.stock}"
        compartidor.compartir(textoParaCompartir)
    }
}