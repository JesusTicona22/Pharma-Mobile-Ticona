// androidMain/platform/Formato.android.kt
package pe.edu.upeu.pharmamobil.platform

import java.util.Locale

actual fun formatearSoles(valor: Double): String =
    String.format(Locale.US, "S/ %.2f", valor)

actual fun tiempoActualMillis(): Long = System.currentTimeMillis()