package pe.edu.upeu.pharmamobil.domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

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
        caso()

        val filaLocal = local.pendientesConEstado(ProductoRepositorioOffline.MAX_INTENTOS)
            .first { it.producto.id == productoPendiente.id }

        assertEquals(1, filaLocal.intentos)
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val motor = MockEngine { respondError(HttpStatusCode.Conflict) }
        val local = crearBaseDatosEnMemoria()
        val caso = armarCasoDeUso(motor, local)

        val productoPendiente = local.guardarPendiente(productoPrueba)
        caso()

        val filaFallida = local.pendientesConEstado(100)
            .first { it.producto.id == productoPendiente.id }

        assertEquals(ProductoRepositorioOffline.MAX_INTENTOS, filaFallida.intentos)
    }
}