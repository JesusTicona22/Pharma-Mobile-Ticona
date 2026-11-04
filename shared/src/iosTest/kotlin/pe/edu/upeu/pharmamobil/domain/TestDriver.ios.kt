package pe.edu.upeu.pharmamobil.domain

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

actual fun crearDriverEnMemoria(): SqlDriver {
    // En iOS usamos el driver nativo guardando en un archivo temporal de prueba
    return NativeSqliteDriver(PharmaDb.Schema, "test.db")
}