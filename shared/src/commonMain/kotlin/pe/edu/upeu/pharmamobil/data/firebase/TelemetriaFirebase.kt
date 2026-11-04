package pe.edu.upeu.pharmamobil.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

class TelemetriaFirebase : Telemetria {

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        contexto.forEach { (clave, valor) ->
            Firebase.crashlytics.setCustomKey(clave, valor)
        }
        Firebase.crashlytics.recordException(e)
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        Firebase.crashlytics.log("EVENTO: $nombre - DATOS: $datos")
    }
}