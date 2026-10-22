package pe.edu.upeu.pharmamobil.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline // <-- NUEVO IMPORT
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo se ensambla sin arrancar la aplicacion.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, platformModule)
    }.koin

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        // CAMBIO CLAVE: Ahora verificamos que resuelva a la versión Offline-First
        assertIs<ProductoRepositorioOffline>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioEnMemoria>(koin.get<ClienteRepository>())
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {

        val koin = grafoCompleto()

        assertSame(
            koin.get<ProductoRepository>(),
            koin.get<ProductoRepository>()
        )
        assertSame(
            koin.get<ClienteRepository>(),
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun resuelveLosCuatroCasosDeUsoConSusRepositorios() {

        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<RegistrarClienteUseCase>()
        koin.get<ListarClientesUseCase>()
    }

    @Test
    fun resuelveLosViewModelConSusCasosDeUso() {

        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
    }
}