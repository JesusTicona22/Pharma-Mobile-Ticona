# Paso 6 — Firma y Archivado en iOS (Guía Práctica N.º 14)

**Proyecto:** PharmaMobil (KMP) sobre PharmaSoft  
**Rama:** `feature/builds-14`  
**Fecha:** 15 de noviembre de 2026

---

## 1. Configuración actual verificada en el proyecto iOS

El proyecto Xcode ubicado en `iosApp/iosApp.xcodeproj` y sus archivos de configuración ya se encuentran preparados con los metadatos de versión y permisos consistentes con la entrega Android:

| Archivo | Parámetro / Clave | Valor configurado |
|---|---|---|
| `iosApp/Configuration/Config.xcconfig` | `PRODUCT_NAME` | `PharmaMobil` |
| `iosApp/Configuration/Config.xcconfig` | `PRODUCT_BUNDLE_IDENTIFIER` | `pe.edu.upeu.pharmamobil.PharmaMobil$(TEAM_ID)` |
| `iosApp/Configuration/Config.xcconfig` | `CURRENT_PROJECT_VERSION` | `1` |
| `iosApp/Configuration/Config.xcconfig` | `MARKETING_VERSION` | `1.0.0` |
| `iosApp/Configuration/Config.xcconfig` | `TEAM_ID` | *(vacío hasta seleccionar equipo en Xcode)* |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `CODE_SIGN_IDENTITY` | `"Apple Development"` |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `CODE_SIGN_STYLE` | `Automatic` |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `DEVELOPMENT_TEAM` | `"${TEAM_ID}"` |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `IPHONEOS_DEPLOYMENT_TARGET` | `18.2` |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `INFOPLIST_FILE` | `iosApp/Info.plist` |
| `iosApp/iosApp/Info.plist` | `NSCameraUsageDescription` | Descripción configurada en español |
| `iosApp/iosApp/Info.plist` | `NSLocationWhenInUseUsageDescription` | Descripción configurada en español |
| `iosApp/iosApp/Info.plist` | `NSUserNotificationsUsageDescription` | Descripción configurada en español |

> Nota: `versionName` (`1.0.0`) y `versionCode` (`1`) de Android coinciden exactamente con `MARKETING_VERSION=1.0.0` y `CURRENT_PROJECT_VERSION=1` en iOS.

---

## 2. Procedimiento en Xcode para generar el archivo e `.ipa`

Cuando el proyecto se abra en un equipo con **macOS + Xcode**:

1. **Abrir el proyecto de iOS y seleccionar el equipo de firma en *Signing & Capabilities*:**
   - Completar `TEAM_ID` en `iosApp/Configuration/Config.xcconfig` o elegir el Apple ID / Team en el panel *Signing & Capabilities* de Xcode.
   - Comprobar que `PRODUCT_BUNDLE_IDENTIFIER` coincide con el registrado en Apple Developer y que no quedan avisos de firma.
2. **Elegir como destino `Any iOS Device` (arm64):**
   - Nunca seleccionar un simulador; al elegir `Any iOS Device`, la opción de archivar se habilita.
3. **Ejecutar `Product → Archive`:**
   - Verificar que la compilación Kotlin Multiplatform (`shared`) + SwiftUI termina sin errores y el `.xcarchive` aparece en el *Organizer* de Xcode.
4. **Distribuir el archivo (`ad hoc` o `development` según la cuenta):**
   - Con cuenta de pago (*Apple Developer Program*), usar perfil `Ad Hoc` o `Development` incluyendo los UDID de los dispositivos de prueba.
5. **Exportar el `.ipa` y renombrarlo con la nomenclatura de entrega:**
   - Comprobar que la versión (`1.0.0` / `1`) coincide con la declarada en la ficha de versión.

---

## 3. Declaración explícita de limitación de entorno (Criterio 10)

Con base en la sección **Paso 6 ("Si trabajas con una cuenta gratuita de Apple")** y **Criterio 10** de la Guía Práctica N.º 14:

- **Estado:** **Limitación documentada.**
- **Motivo técnico:**
  1. El entorno de desarrollo disponible durante la sesión es **Windows (`x86_64`)** sin acceso físico ni remoto a **macOS / Xcode**, herramienta indispensable de Apple para compilar binarios `arm64`, generar el archivo `.xcarchive`, firmar con perfil de aprovisionamiento y exportar el paquete `.ipa`.
  2. Tampoco se cuenta con una membresía de pago del *Apple Developer Program* (US\$ 99/año) requerida para distribución *Ad Hoc* y exportación formal de `.ipa`; con cuenta gratuita solo es posible instalar localmente mediante Xcode en dispositivos propios con perfil de 7 días de vigencia.
- **Qué haría falta para cerrarlo:**
  - Abrir este mismo repositorio en una máquina con **macOS y Xcode**, configurar `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`, ejecutar `Product → Archive` con destino `Any iOS Device`, capturar el *Organizer* y ejecutar en un iPhone físico (o exportar el `.ipa` si se dispone de cuenta de pago).
