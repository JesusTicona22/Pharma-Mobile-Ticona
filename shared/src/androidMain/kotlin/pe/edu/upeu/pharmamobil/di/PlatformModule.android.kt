package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Koin usará OkHttp para procesar las peticiones en Android
    single<HttpClientEngine> { OkHttp.create() }
}