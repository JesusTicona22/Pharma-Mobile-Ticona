package pe.edu.upeu.pharmamobil.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) {
        borrarToken()
        // Implementación básica Keychain de iOS
    }

    override fun leerToken(): String? {
        // Implementación básica Keychain de iOS
        return null
    }

    override fun borrarToken() {
        // Implementación básica Keychain de iOS
    }
}