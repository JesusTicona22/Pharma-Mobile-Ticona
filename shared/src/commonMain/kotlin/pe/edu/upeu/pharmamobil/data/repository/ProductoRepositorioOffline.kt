package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarPendientes(): Flow<Int> = local.observarPendientes(MAX_INTENTOS)

    override fun observarFallidas(): Flow<Int> = local.observarFallidas(MAX_INTENTOS)

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar(): ResumenSync {
        var enviadas = 0
        var fallidas = 0
        var huboFalloTransitorio = false

        local.pendientesConEstado(MAX_INTENTOS).forEach { fila ->
            when (val r = remoto.enviar(fila.producto)) {
                is ResultadoEnvio.Exito -> {
                    if (r.idServidor > 0L) {
                        local.confirmar(fila.producto.id, r.idServidor)
                    } else {
                        local.marcarComoSubido(fila.producto)
                    }
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.producto.id, r.motivo, MAX_INTENTOS)
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.producto.id, r.motivo)
                    huboFalloTransitorio = true
                    delay(espera(fila.intentos))
                }
            }
        }

        // Solo después de subir se baja: invertirlo borra lo pendiente.
        val remotos = remoto.obtenerProductos().map { it.toDomain() }
        local.reemplazarSincronizados(remotos)

        if (huboFalloTransitorio) {
            throw IllegalStateException("Fallo transitorio al sincronizar pendientes")
        }

        return ResumenSync(
            enviadas = enviadas,
            fallidas = fallidas,
            pendientes = local.contarPendientes(MAX_INTENTOS)
        )
    }

    private fun espera(intentos: Int): Long =
        (1000L shl intentos.coerceAtMost(4)) // 1s, 2s, 4s, 8s, 16s

    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (_: Exception) {
            // Queda pendiente; se reintentará al sincronizar.
        }
    }

    companion object {
        const val MAX_INTENTOS = 5
    }
}