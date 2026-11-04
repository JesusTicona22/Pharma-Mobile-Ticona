package pe.edu.upeu.pharmamobil.di

import android.content.Context
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

// Imports de la Sesión 9 (Compartir)
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

// Imports de la Sesión 10 (Almacén Seguro)
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguroAndroid

// Import de la Sesión 11 (SQLDelight - Offline First)
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver

// NUEVOS Imports de la Sesión 13 (Gestor de Permisos)
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.permiso.GestorPermisosAndroid

actual val platformModule: Module = module {
    // Motor de red para Ktor
    single { OkHttp.create() }

    // Capacidad nativa: Compartir producto
    single<Compartidor> { CompartidorAndroid(androidContext()) }

    // Capacidad nativa: Almacén seguro para el token JWT
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }

    // Capacidad nativa: Driver de SQLite para la base de datos
    single { CreadorDeDriver(contexto = androidContext()) }

    // NUEVO: Instanciar SharedPreferences y GestorPermisos (Sesión 13)
    single {
        androidContext().getSharedPreferences("pharma_prefs", Context.MODE_PRIVATE)
    }
    single<GestorPermisos> {
        GestorPermisosAndroid(androidContext(), get())
    }
}