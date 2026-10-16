package pe.edu.upeu.pharmamobil.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        contexto,
        "sesion_segura",
        llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) {
        prefs.edit().putString(CLAVE_TOKEN, token).apply()
    }

    override fun leerToken(): String? = prefs.getString(CLAVE_TOKEN, null)

    override fun borrarToken() {
        prefs.edit().remove(CLAVE_TOKEN).apply()
    }

    override fun guardarModoOscuro(activo: Boolean) {
        prefs.edit().putBoolean(CLAVE_TEMA, activo).apply()
    }

    override fun leerModoOscuro(): Boolean = prefs.getBoolean(CLAVE_TEMA, false)

    private companion object {
        const val CLAVE_TOKEN = "jwt"
        const val CLAVE_TEMA = "modo_oscuro"
    }
}