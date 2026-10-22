package pe.edu.upeu.pharmamobil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ObtenerSesionGuardadaUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.detalle.DetalleProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarProductosUseCase

val dataModule = module {
    single { crearHttpClient(motor = get(), almacen = get()) }
    single { ProductoApi(get()) }

    // 1. Base de datos SQLDelight (Sesión 11)
    single { PharmaDb(get<CreadorDeDriver>().crear()) }

    // 2. Capa local que traduce SQLite a Dominio
    single { ProductoLocal(base = get()) }

    // 3. CAMBIO CLAVE: Ahora inyectamos el repositorio Offline-First
    single<ProductoRepository> {
        ProductoRepositorioOffline(local = get(), remoto = get())
    }

    single<ClienteRepository> { ClienteRepositorioEnMemoria() }

    // Repositorio de Autenticación JWT (Sesión 10)
    single<AuthRepository> { AuthRepositorioRest(cliente = get(), almacen = get()) }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }

    // Casos de uso de Sesión (Sesión 10)
    factory { IniciarSesionUseCase(get()) }
    factory { ObtenerSesionGuardadaUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }

    factory { SincronizarProductosUseCase(get()) }
}

val presentationModule = module {
    viewModelOf(::ProductoViewModel)
    viewModelOf(::ClienteViewModel)
    viewModelOf(::DetalleProductoViewModel)

    // ViewModel de Sesión (Sesión 10)
    viewModelOf(::SesionViewModel)
}

expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}