// androidMain/di/PlatformModule.android.kt
package pe.edu.upeu.pharmamobil.di

// Cambiamos el import de android por el de okhttp
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid

actual val platformModule: Module = module {
    // Usamos OkHttp.create() en lugar de Android.create()
    single { OkHttp.create() }

    single<Compartidor> { CompartidorAndroid(androidContext()) }

    // NUEVO: Almacén seguro para Android inyectando el Context
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
}