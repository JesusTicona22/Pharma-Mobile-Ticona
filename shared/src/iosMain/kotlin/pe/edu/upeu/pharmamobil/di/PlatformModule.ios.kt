package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

// Imports de la Sesión 9 (Compartir)
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorIos

// Imports de la Sesión 10 (Almacén Seguro)
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroIos

// Import de la Sesión 11 (SQLDelight - Offline First)
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver

actual val platformModule: Module = module {
    // Motor de red para Ktor en iOS
    single { Darwin.create() }

    // Capacidad nativa: Compartir producto
    single<Compartidor> { CompartidorIos() }

    // Capacidad nativa: Almacén seguro para el token JWT
    single<AlmacenSeguro> { AlmacenSeguroIos() }

    // Capacidad nativa: Driver de SQLite para la base de datos
    single { CreadorDeDriver() }
}