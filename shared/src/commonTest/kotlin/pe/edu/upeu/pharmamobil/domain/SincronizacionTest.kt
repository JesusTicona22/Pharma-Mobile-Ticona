package pe.edu.upeu.pharmamobil.domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SincronizacionTest {

    private fun crearBaseDatosEnMemoria(): ProductoLocal {
        // Llama al motor correcto dependiendo de si la prueba corre en PC, Mac o Celular
        val driver = crearDriverEnMemoria()
        return ProductoLocal(PharmaDb(driver))
    }

    private fun armarCasoDeUso(motor: MockEngine, local: ProductoLocal): SincronizarProductosUseCase {
        val httpClient = HttpClient(motor) {
            install(ContentNegotiation) { json() }
        }
        val remoto = ProductoApi(httpClient)
        val repositorio = ProductoRepositorioOffline(local, remoto)
        return SincronizarProductosUseCase(repositorio)
    }

    private val productoPrueba = Producto(id = -1L, nombre = "Ibuprofeno", precio = 15.5, stock = 10)

    @Test
    fun unFalloTransitorioSumaIntentoNoMarcaFallida() = runTest {
        val motor = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }
        val local = crearBaseDatosEnMemoria()
        val caso = armarCasoDeUso(motor, local)

        val productoPendiente = local.guardarPendiente(productoPrueba)
        val resultado = caso()

        assertTrue(resultado.isFailure)

        val filaLocal = local.porId(productoPendiente.id)!!
        assertEquals(1, filaLocal.intentos)
        assertTrue(filaLocal.producto.pendiente)
        assertFalse(filaLocal.producto.fallida)
        assertEquals(1, local.contarPendientes(ProductoRepositorioOffline.MAX_INTENTOS))
        assertEquals(0, local.contarFallidas(ProductoRepositorioOffline.MAX_INTENTOS))
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        var enviosPost = 0
        val motor = MockEngine { request ->
            if (request.method == HttpMethod.Post) {
                enviosPost++
            }
            respondError(HttpStatusCode.Conflict)
        }
        val local = crearBaseDatosEnMemoria()
        val caso = armarCasoDeUso(motor, local)

        val productoPendiente = local.guardarPendiente(productoPrueba)
        caso()
        caso() // Segunda sincronización no debe reintentar la fila permanente

        val filaFallida = local.porId(productoPendiente.id)!!
        assertEquals(ProductoRepositorioOffline.MAX_INTENTOS, filaFallida.intentos)
        assertTrue(filaFallida.producto.pendiente)
        assertTrue(filaFallida.producto.fallida)
        assertEquals(0, local.contarPendientes(ProductoRepositorioOffline.MAX_INTENTOS))
        assertEquals(1, local.contarFallidas(ProductoRepositorioOffline.MAX_INTENTOS))
        assertEquals(1, enviosPost)

        val observado = local.observar().first().first { it.id == productoPendiente.id }
        assertTrue(observado.fallida)
    }

    @Test
    fun gestorPermisosFalsoModelaLosTresEstadosSinDispositivo() = runTest {
        val fakePermisos = object : GestorPermisos {
            private val estados = mutableMapOf(
                Permiso.NOTIFICACIONES to EstadoPermiso.Denegado,
                Permiso.CAMARA to EstadoPermiso.DenegadoParaSiempre
            )
            var ajustesAbiertos = false

            override suspend fun estado(permiso: Permiso): EstadoPermiso =
                estados[permiso] ?: EstadoPermiso.Denegado

            override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
                val actual = estado(permiso)
                val siguiente = when (actual) {
                    EstadoPermiso.Concedido -> EstadoPermiso.Concedido
                    EstadoPermiso.Denegado -> EstadoPermiso.Concedido
                    EstadoPermiso.DenegadoParaSiempre -> EstadoPermiso.DenegadoParaSiempre
                }
                estados[permiso] = siguiente
                return siguiente
            }

            override fun abrirAjustes() {
                ajustesAbiertos = true
            }
        }

        assertEquals(EstadoPermiso.Denegado, fakePermisos.estado(Permiso.NOTIFICACIONES))
        assertEquals(EstadoPermiso.Concedido, fakePermisos.solicitar(Permiso.NOTIFICACIONES))
        assertEquals(EstadoPermiso.DenegadoParaSiempre, fakePermisos.estado(Permiso.CAMARA))

        fakePermisos.abrirAjustes()
        assertTrue(fakePermisos.ajustesAbiertos)
    }
}
