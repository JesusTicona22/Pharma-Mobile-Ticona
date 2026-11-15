# Guía de Instalación, Ficha de Versión/Firma y Checklist de Entrega (Sesión 14)

**Proyecto:** PharmaMobil (KMP) sobre PharmaSoft  
**Repositorio Frontend:** `https://github.com/JesusTicona22/Pharma-Mobile-Ticona.git`  
**Repositorio Backend:** `https://github.com/JesusTicona22/Pharma-Mobile-Backend.git`  
**Rama de trabajo:** `feature/builds-14`

---

## 1. Ficha de versionado y certificado de firma (Nomenclatura E3)

| Dato | Valor de esta entrega |
|---|---|
| `versionCode` | `1` |
| `versionName` | `1.0.0` |
| `applicationId` (Android) | `pe.edu.upeu.pharmamobil` |
| `PRODUCT_BUNDLE_IDENTIFIER` (iOS) | `pe.edu.upeu.pharmamobil.PharmaMobil$(TEAM_ID)` |
| `CURRENT_PROJECT_VERSION` / `MARKETING_VERSION` (iOS) | `1` / `1.0.0` |
| Etiqueta de Git (aplicar solo tras fusionar en rama principal) | `v1.0-final` |
| Nombre de archivo sugerido para entrega (.aab) | `Equipo<N>_ProyectoFinal_Android.aab` *(renombrar copia de `androidApp-release.aab`)* |

### Huella digital del certificado (`apksigner verify --print-certs`)

> Las contraseñas del almacén (`storePassword` y `keyPassword`) **no se transcriben en este documento ni en el repositorio**.

- **Alias del almacén:** `pharmamobil`
- **Titular (`DN`):** `CN=Henry Jesus Ticona, OU=EP Ingenieria de Sistemas, O=UPeU, L=Juliaca, ST=Puno, C=PE`
- **Validez:** `Sun Nov 15 11:26:13 PET 2026` hasta `Thu Apr 02 11:26:13 PET 2054`
- **SHA-256:** `46:9A:97:6A:74:0B:91:C5:38:F8:E4:FB:ED:2A:2B:C3:61:F3:CF:8E:FE:99:47:49:4B:0C:22:E4:81:C9:C2:99`
- **SHA-1:** `AB:B0:DA:87:CA:90:E6:AF:96:00:7A:B2:F7:55:78:86:F3:05:16:23`
- **MD5:** `dd8b3d230ea788122fd9bb6e6db58fdf`

---

## 2. Guía de instalación y ejecución para evaluación

### 2.1 Levantar el backend (`pharmaSoft`)

1. Ubicar la carpeta del backend (`pharmaSoft`).
2. Ejecutar con Docker Desktop activo:
   ```powershell
   docker compose up -d
   ```
3. Verificar que la API responde en el puerto `8080` (`http://localhost:8080/api/v1/`).

### 2.2 Instalar el APK firmado en Android

- **Artefacto instalable:** `androidApp/build/outputs/apk/release/androidApp-release.apk`
- **Paquete para tienda (E3):** `androidApp/build/outputs/bundle/release/androidApp-release.aab` (copiar y renombrar a `Equipo<N>_ProyectoFinal_Android.aab` con el número de equipo correspondiente).

**Opción A — Por línea de comandos (ADB):**
```powershell
adb install -r androidApp/build/outputs/apk/release/androidApp-release.apk
```

**Opción B — Transferencia directa al teléfono:**
1. Copiar `androidApp-release.apk` al almacenamiento del dispositivo Android.
2. Abrir el archivo desde el gestor de archivos y autorizar *"Instalar aplicaciones desconocidas"* si Android lo solicita.
3. Iniciar **PharmaMobil**.

### 2.3 Compilar el proyecto desde cero (sin `keystore.properties`)

Un evaluador que clone el repositorio no necesita el archivo `keystore.properties` para abrir el proyecto ni generar builds de depuración:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

Para generar un nuevo build `release` firmado localmente, crear `keystore.properties` en la raíz según la plantilla indicada en `README.md`.

---

## 3. Reglas ProGuard / R8 aplicadas en `release`

Se mantuvo **`isMinifyEnabled = true`** en `androidApp/build.gradle.kts` (no se desactivó la minificación). Para asegurar el funcionamiento correcto de serialización, inyección de dependencias y reporte de fallos se configuraron reglas en `androidApp/proguard-rules.pro`:
- Conservación de atributos de línea y fuente para Firebase Crashlytics (`LineNumberTable`, `SourceFile`, `Renumbering`).
- Conservación de metadatos de anotaciones y firmas genéricas (`*Annotation*`, `Signature`, `InnerClasses`, `EnclosingMethod`).
- Conservación explícita de DTOs (`pe.edu.upeu.pharmamobil.data.remote.dto.**`) y serializadores de `kotlinx.serialization`.
- Conservación de `ViewModel` (`pe.edu.upeu.pharmamobil.presentation.**`) para evitar eliminación/renombrado por Koin.
- Regla `-dontwarn org.slf4j.impl.StaticLoggerBinder` requerida por R8 al minificar dependencias de Ktor/SLF4J.

---

## 4. Lista de cotejo (Guía Práctica N.º 14)

| N.º | Criterio observable | Estado | Observación / Evidencia |
|---|---|---|---|
| 1 | La ficha de integración de la sesión 13 está cerrada y el alcance congelado por escrito. | **Sí** | `docs/PASO-01-FICHA-INTEGRACION-S14.md` |
| 2 | El almacén de claves del equipo existe y hay una copia fuera del equipo de un solo integrante. | **Parcial (accion usuario)** | `C:\Users\MSI PRO\claves\pharmamobil.jks` creado fuera del repo; falta respaldar copia externa en Drive/USB/otro equipo |
| 3 | Ni el almacén ni sus contraseñas están en el repositorio, y el `.gitignore` lo impide. | **Sí** | `.gitignore` ignora `keystore.properties`, `*.jks`, `*.keystore`; `README.md` solo muestra `********` |
| 4 | El proyecto sigue abriendo y compilando para quien no tiene el archivo de propiedades. | **Sí** | `androidApp/build.gradle.kts` valida `if (f.exists())` y `:androidApp:assembleDebug` compiló sin `keystore.properties` |
| 5 | El paquete de aplicación firmado se generó sin errores. | **Sí** | `androidApp/build/outputs/bundle/release/androidApp-release.aab` generado con `:androidApp:bundleRelease` |
| 6 | El instalable de entrega se generó y se instaló en un dispositivo real. | **Parcial (emulador)** | `androidApp-release.apk` generado e instalado en `emulator-5554`; pendiente instalar en teléfono físico |
| 7 | La verificación de la firma muestra el titular y la huella del certificado del equipo. | **Sí** | Verificado con `apksigner verify --print-certs` (Sección 1) |
| 8 | La versión de entrega se recorrió completa y la serialización y la inyección funcionan. | **Parcial** | Arranque release verificado en `emulator-5554`; recorrido completo end-to-end contra backend queda para prueba en dispositivo real |
| 9 | Si la minificación quedó desactivada, la decisión está escrita con su motivo. | **Sí (N/A)** | La minificación **quedó activada** (`isMinifyEnabled = true`) con reglas ProGuard en `androidApp/proguard-rules.pro` |
| 10 | El proyecto de iOS tiene la firma configurada y el archivo generado, o su límite documentado. | **Sí** | Configuración revisada (`1.0.0`) y limitación Windows/no-macOS documentada en `docs/PASO-06-IOS-FIRMA-Y-LIMITACION-S14.md` |
| 11 | El producto se probó en al menos dos dispositivos reales distintos durante la clase. | **Pendiente (usuario)** | Plantilla lista en `docs/PASO-07-MATRIZ-PRUEBAS-DISPOSITIVOS-S14.md` |
| 12 | Cada defecto encontrado tiene identificador, pasos, severidad y estado. | **Pendiente (usuario)** | Tabla de defectos lista en `docs/PASO-07-MATRIZ-PRUEBAS-DISPOSITIVOS-S14.md` |
| 13 | El `versionCode`, el `versionName` y el identificador están declarados y coinciden con el archivo. | **Sí** | `versionCode=1`, `versionName="1.0.0"`, `applicationId="pe.edu.upeu.pharmamobil"` |
| 14 | Los commits siguen la convención y están distribuidos a lo largo de la sesión. | **Sí** | Commits por paso en rama `feature/builds-14` |
