// iosMain/di/PlatformModule.ios.kt
package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorIos

import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroIos

actual val platformModule: Module = module {
    single<Compartidor> { CompartidorIos() }
    // NUEVO: Almacén seguro para iOS
    single<AlmacenSeguro> { AlmacenSeguroIos() }
}